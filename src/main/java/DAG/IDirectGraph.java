package DAG;

import java.util.List;

public interface IDirectGraph<V> {

    /**
     * 新增顶点
     * @param v 顶点
     */
    void addVertex(final V v);

    /**
     * 删除顶点
     * @param v 顶点
     * @return 是否删除成功
     */
    boolean removeVertex(final V v);

    /**
     * 获取顶点
     * @param index 下标
     * @return 返回顶点信息
     */
    V getVertex(final int index);

    /**
     * 新增边
     * @param edge 边
     */
    void addEdge(final Edge<V> edge);

    /**
     * 移除边
     * @param edge 边信息
     */
    boolean removeEdge(final Edge<V> edge);

    /**
     * 获取边信息
     * @param from 开始节点
     * @param to 结束节点
     */
    Edge<V> getEdge(final int from, final int to);

    List<V> dfs(V root);

    List<V> getSucceed(final V v);  // 获取顶点的所有后继节点

}
