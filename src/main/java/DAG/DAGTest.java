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

        System.out.println(directGraph.getPredecessor("10"));
    }
}
