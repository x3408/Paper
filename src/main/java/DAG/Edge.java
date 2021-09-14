package DAG;

import java.math.BigDecimal;

public class Edge<V> {

    /**
     * 开始节点
     * @since 0.0.2
     */
    private V from;

    /**
     * 结束节点
     * @since 0.0.2
     */
    private V to;

    /**
     * 权重
     * @since 0.0.2
     */
    private BigDecimal weight;

    public Edge(V from, V to, BigDecimal weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public V getFrom() {
        return from;
    }

    public void setFrom(V from) {
        this.from = from;
    }

    public V getTo() {
        return to;
    }

    public void setTo(V to) {
        this.to = to;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "from=" + from +
                ", to=" + to +
                ", weight=" + weight +
                '}';
    }
}
