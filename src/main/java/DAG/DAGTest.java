package DAG;

public class DAGTest {
    public static void main(String[] args) {
        IDirectGraph<String> directGraph = new ListDirectGraph<>();
        //1. 初始化顶点
        directGraph.addVertex("1");
        directGraph.addVertex("2");
        directGraph.addVertex("3");
        directGraph.addVertex("4");
        directGraph.addVertex("5");
        directGraph.addVertex("6");
        directGraph.addVertex("7");
        directGraph.addVertex("8");
        directGraph.addVertex("9");
        directGraph.addVertex("10");

        //2. 初始化边
        directGraph.addEdge(new Edge<>("1", "2",1));
        directGraph.addEdge(new Edge<>("1", "3",1));
        directGraph.addEdge(new Edge<>("1", "4",1));
        directGraph.addEdge(new Edge<>("1", "5",1));
        directGraph.addEdge(new Edge<>("1", "6",1));
        directGraph.addEdge(new Edge<>("2", "8",1));
        directGraph.addEdge(new Edge<>("2", "9",1));
        directGraph.addEdge(new Edge<>("3", "7",1));
        directGraph.addEdge(new Edge<>("4", "8",1));
        directGraph.addEdge(new Edge<>("4", "9",1));
        directGraph.addEdge(new Edge<>("5", "9",1));
        directGraph.addEdge(new Edge<>("6", "8",1));
        directGraph.addEdge(new Edge<>("7", "10",1));
        directGraph.addEdge(new Edge<>("8", "10",1));
        directGraph.addEdge(new Edge<>("9", "10",1));

        System.out.println(directGraph.getPredecessor("10"));
    }
}
