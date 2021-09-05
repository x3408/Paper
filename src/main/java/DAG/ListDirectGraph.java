package DAG;

import java.util.*;

public class ListDirectGraph<V> implements IDirectGraph<V> {

    /**
     * 节点链表
     */
    private List<GraphNode<V>> nodeList;

    /**
     * 初始化有向图
     */
    public ListDirectGraph() {
        this.nodeList = new ArrayList<>();
    }

    @Override
    public void addVertex(V v) {
        GraphNode<V> node = new GraphNode<>(v);

        // 直接加入到集合中
        this.nodeList.add(node);
    }

    @Override
    public boolean removeVertex(V v) {
        //1. 移除一个顶点
        //2. 所有和这个顶点关联的边也要被移除
        Iterator<GraphNode<V>> iterator = nodeList.iterator();
        while (iterator.hasNext()) {
            GraphNode<V> graphNode = iterator.next();

            if(v.equals(graphNode.getVertex())) {
                iterator.remove();
            }
        }

        return true;
    }

    @Override
    public V getVertex(int index) {
        return nodeList.get(index).getVertex();
    }

    @Override
    public void addEdge(Edge<V> edge) {
        //1. 新增一条边，直接遍历列表。
        // 如果存在这条的起始节点，则将这条边加入。
        // 如果不存在，则直接报错即可。

        for(GraphNode<V> graphNode : nodeList) {
            V from = edge.getFrom();
            V vertex = graphNode.getVertex();

            // 起始节点在开头
            if(from.equals(vertex)) {
                graphNode.getEdgeSet().add(edge);
            }
        }
    }

    @Override
    public boolean removeEdge(Edge<V> edge) {
        // 直接从列表中对应的节点，移除即可
        GraphNode<V> node = getGraphNode(edge);
        if(null != node) {
            // 移除目标为 to 的边
            node.remove(edge.getTo());
        }

        return true;
    }

    @Override
    public Edge<V> getEdge(int from, int to) {
        // 获取开始和结束的顶点
        GraphNode<V> fromNode = nodeList.get(from);
        V toVertex = getVertex(to);

        // 获取对应结束顶点的边
        return fromNode.get(toVertex);
    }

    /**
     * 获取图节点
     * @param edge 边
     * @return 图节点
     */
    private GraphNode<V> getGraphNode(final Edge<V> edge) {
        final V from = edge.getFrom();
        for(GraphNode<V> node : nodeList) {
            if(node.getVertex().equals(from)) {
                return node;
            }
        }

        return null;
    }

    /**
     * 获取对应的图节点
     * @param vertex 顶点
     * @return  图节点
     */
    private GraphNode<V> getGraphNode(final V vertex) {
        for(GraphNode<V> node : nodeList) {
            if(vertex.equals(node.getVertex())) {
                return node;
            }
        }
        return null;
    }

    public List<V> dfs(V root) {
        List<V> visitedList = new ArrayList<>();
        Stack<V> stack = new Stack<>();
        // 顶点首先压入堆栈
        stack.push(root);
        // 获取一个边的节点
        while (!stack.isEmpty()) {
            V visitingVertex = stack.pop();
            GraphNode<V> graphNode = getGraphNode(visitingVertex);
            if(null != graphNode) {
                Set<Edge<V>> edgeSet = graphNode.getEdgeSet();
                for(Edge<V> edge : edgeSet) {
                    V to = edge.getTo();
                    if(!visitedList.contains(to) && !stack.contains(to)) {
                        // 寻找到下一个临接点
                        stack.push(to);
                    }
                }
                visitedList.add(visitingVertex);
            }
        }
        return visitedList;
    }

    @Override
    public List<V> getSucceed(V v) {
        ArrayList<V> res = new ArrayList<>();
        GraphNode<V> node = getGraphNode(v);
        if (null == node) return null;

        for (Edge<V> edge : node.getEdgeSet()) {
            res.add(edge.getTo());
        }
        return res;
    }

    /**
     * 获取以目标节点的前任节点
     */
    @Override
    public Set<V> getPredecessor(V v) {
        GraphNode<V> node = getGraphNode(v);
        if (null == node) return null;

        V root = nodeList.get(0).getVertex();
        // 通过广度遍历节点 判断其后继节点是否为目标节点并添加到集合中
        return getPredecessor(root, v);
    }


    private Set<V> getPredecessor(V root, V v) {
        HashSet<V> set = new HashSet<>();
        LinkedList<V> que = new LinkedList<>();
        que.offer(root);
        while (que.size()!=0) {
            int count = que.size();
            for (int i=1; i<=count; i++) {
                V node = que.poll();
                for (V succeed : getSucceed(node)) {
                    if (succeed.equals(v)) set.add(node);
                    que.add(succeed);
                }
            }
        }
        return set;
    }
}