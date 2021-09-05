import DAG.Edge;
import DAG.IDirectGraph;
import DAG.ListDirectGraph;
import KMeans.KMeans;
import entity.Node;
import entity.Task;
import utils.Energy;

import java.util.*;

public class Main {
    public static final double APPLICATION_ENERGY_CONSTRAINT=80.995;

    public static final int TASK_NUM = 10;
    public static final int NODE_NUM = 3;
    public static final int WDAG = 10;      // 任务全局平均执行时长(目前是随便设定的)
    public static final double β = 0.5;     // β的取值为0.1,0.25,0.5,0.75,1.0

    private static final Map<Integer, Task> taskMap = new LinkedHashMap<>(16, 0.75f, true);  // 任务需要有序
    private static final Map<Integer, Node> nodeMap = new HashMap<>();
    private static final IDirectGraph<Task> directGraph = new ListDirectGraph<>();  // 按任务id-1作为下标存放

    public static void main(String[] args) {
        // 初始化
        initTaskAndDAG();
        initVirtualMachine();
        // 将任务排序
        OrderTasks();
        // 将任务分类
        new KMeans(taskMap, 3).doKMeans();
        // 计算子任务能耗约束
        Energy energy = new Energy();
        energy.calculateTaskEnergyConstraint(APPLICATION_ENERGY_CONSTRAINT, taskMap, nodeMap);
        // 计算任务在不同处理器的不同频率上的执行能耗，满足条件后再计算EFT
        Node node = new Node();
        node.getSuitableNode(taskMap, nodeMap, directGraph);
        // 计算调度时间SL(G)、E(G)
    }

    /**
     * 根据优先级得分降序排序任务
     */
    public static void OrderTasks() {
        List<Double> scoreRes = new ArrayList<>();

        // 为每个任务设置平均执行时间
        for (int i = TASK_NUM; i>=1; i--) {
            Task task = taskMap.get(i);
            double rankScore = calculatePriority(task);
            scoreRes.add(rankScore);
        }
        Collections.reverse(scoreRes);
        System.out.println("-------------------- 任务得分(按下标输出） -----------------------");
        scoreRes.forEach(score -> System.out.print(String.format("%.2f", score)+"\t"));
        System.out.println();

        // 重新排序
        List<Map.Entry<Integer, Task>> entries = new ArrayList<>(taskMap.entrySet());
        entries.sort((left, right) -> {
            double res =  left.getValue().getRankScore() - right.getValue().getRankScore();
            if (res < 0) return 1;
            else if (res > 0) return -1;
            else return 0;
        });
        taskMap.clear();
        for(Map.Entry<Integer, Task> e : entries) {
            taskMap.put(e.getKey(), e.getValue());
        }
        System.out.println("-------------------- 输出最终任务排序结果 ------------------------");
        taskMap.forEach((taskId, task) -> System.out.print(task.getId() + "\t"));
        System.out.println();
    }

    /**
     * 计算task优先级得分
     * rank(n)= w + Math.max(c + rank(j))
     * @param task
     */
    private static double calculatePriority(Task task) {
        // exit任务的分数为平均执行时间
        if (task.getId()==TASK_NUM) {
            task.setRankScore(task.getAverageExecutionTime());
            return task.getRankScore();
        }

        // 平均执行时间+前驱节点中得分加通讯分数最大的
        // 1.获得后继节点的得分与通讯时间
        double succeedScore=0;
        for (Task succeed : directGraph.getSucceed(task)) {
            double weight = directGraph.getEdge(task.getId()-1, succeed.getId()-1).getWeight();
            succeedScore = Math.max(succeedScore, succeed.getRankScore()+weight);
        }
        double finalScore = task.getAverageExecutionTime() + succeedScore;
        task.setRankScore(finalScore);
        return finalScore;
    }

    /**
     * 初始化任务并构造DAG，该版本需要手动添加边
      */
    private static void initTaskAndDAG() {
        // 使用随机初始化方法
//        Random random = new Random();
//        Task.EXECUTION_TIME = new double[TASK_NUM+1][NODE_NUM+1];
//        for (int i = 1; i <= TASK_NUM; i++) {
//            Task task = Task.builder().id(i).cpu(random.nextDouble()*100).memory(random.nextDouble()*100).build();
//            task.setAverageExecutionTime(random.nextDouble() * WDAG * 2);
//            // 为每个任务生成随机执行时间
//            for (int j = 1; j <= NODE_NUM; j++) {
//                double down = task.getAverageExecutionTime() * (1 - β / 2);
//                double up = task.getAverageExecutionTime() * (1 + β / 2);
//                Task.EXECUTION_TIME[i][j] = down + (up - down) * random.nextDouble();
//            }
//            taskMap.put(i, task);
//            directGraph.addVertex(task);
//        }
        // 使用论文中的demo
        Random random = new Random();
        Task.EXECUTION_TIME = new double[TASK_NUM+1][NODE_NUM+1];
        for (int i=1; i<=TASK_NUM; i++) {
            Task task = Task.builder().id(i).cpu(random.nextDouble()*100).memory(random.nextDouble()*100).build();
            taskMap.put(i, task);
            directGraph.addVertex(task);
        }
        taskMap.get(1).setAverageExecutionTime(13);
        taskMap.get(2).setAverageExecutionTime(50f/3);
        taskMap.get(3).setAverageExecutionTime(43f/3);
        taskMap.get(4).setAverageExecutionTime(38f/3);
        taskMap.get(5).setAverageExecutionTime(35f/3);
        taskMap.get(6).setAverageExecutionTime(38f/3);
        taskMap.get(7).setAverageExecutionTime(11);
        taskMap.get(8).setAverageExecutionTime(10);
        taskMap.get(9).setAverageExecutionTime(50f/3);
        taskMap.get(10).setAverageExecutionTime(44f/3);

        Task.EXECUTION_TIME[1][1] = 14;
        Task.EXECUTION_TIME[1][2] = 16;
        Task.EXECUTION_TIME[1][3] = 9;
        Task.EXECUTION_TIME[2][1] = 13;
        Task.EXECUTION_TIME[2][2] = 19;
        Task.EXECUTION_TIME[2][3] = 18;
        Task.EXECUTION_TIME[3][1] = 11;
        Task.EXECUTION_TIME[3][2] = 13;
        Task.EXECUTION_TIME[3][3] = 19;
        Task.EXECUTION_TIME[4][1] = 13;
        Task.EXECUTION_TIME[4][2] = 8;
        Task.EXECUTION_TIME[4][3] = 17;
        Task.EXECUTION_TIME[5][1] = 12;
        Task.EXECUTION_TIME[5][2] = 13;
        Task.EXECUTION_TIME[5][3] = 10;
        Task.EXECUTION_TIME[6][1] = 13;
        Task.EXECUTION_TIME[6][2] = 16;
        Task.EXECUTION_TIME[6][3] = 9;
        Task.EXECUTION_TIME[7][1] = 7;
        Task.EXECUTION_TIME[7][2] = 15;
        Task.EXECUTION_TIME[7][3] = 11;
        Task.EXECUTION_TIME[8][1] = 5;
        Task.EXECUTION_TIME[8][2] = 11;
        Task.EXECUTION_TIME[8][3] = 14;
        Task.EXECUTION_TIME[9][1] = 18;
        Task.EXECUTION_TIME[9][2] = 12;
        Task.EXECUTION_TIME[9][3] = 20;
        Task.EXECUTION_TIME[10][1] = 21;
        Task.EXECUTION_TIME[10][2] = 7;
        Task.EXECUTION_TIME[10][3] = 16;

        directGraph.addEdge(new Edge<>(taskMap.get(1), taskMap.get(2),18));
        directGraph.addEdge(new Edge<>(taskMap.get(1), taskMap.get(3),12));
        directGraph.addEdge(new Edge<>(taskMap.get(1), taskMap.get(4),9));
        directGraph.addEdge(new Edge<>(taskMap.get(1), taskMap.get(5),11));
        directGraph.addEdge(new Edge<>(taskMap.get(1), taskMap.get(6),14));
        directGraph.addEdge(new Edge<>(taskMap.get(2), taskMap.get(8),19));
        directGraph.addEdge(new Edge<>(taskMap.get(2), taskMap.get(9),16));
        directGraph.addEdge(new Edge<>(taskMap.get(3), taskMap.get(7),23));
        directGraph.addEdge(new Edge<>(taskMap.get(4), taskMap.get(8),27));
        directGraph.addEdge(new Edge<>(taskMap.get(4), taskMap.get(9),23));
        directGraph.addEdge(new Edge<>(taskMap.get(5), taskMap.get(9),13));
        directGraph.addEdge(new Edge<>(taskMap.get(6), taskMap.get(8),15));
        directGraph.addEdge(new Edge<>(taskMap.get(7), taskMap.get(10),17));
        directGraph.addEdge(new Edge<>(taskMap.get(8), taskMap.get(10),11));
        directGraph.addEdge(new Edge<>(taskMap.get(9), taskMap.get(10),13));
    }


    private static void initVirtualMachine() {
        // 初始化虚拟机性能
//        for (int i = 1; i <= NODE_NUM; i++) {
//            // 这里测试同构集群
//            Node node = new Node(i, 0.26, 1, 0.26, 0.01, 0, 0.03, 0.8, 2.9);
//            // 添加到DAG中获得优先级
//            nodeMap.put(i, node);
//        }

        Node node1 = new Node(1, 0.26, 1, 0.26, 0.01, 0, 0.03, 0.8, 2.9);
        Node node2 = new Node(2, 0.26, 1, 0.26, 0.01, 0, 0.04, 0.8, 2.5);
        Node node3 = new Node(3, 0.29, 1, 0.29, 0.01, 0, 0.07, 1.0, 2.5);
        nodeMap.put(1, node1);
        nodeMap.put(2, node2);
        nodeMap.put(3, node3);
    }
}
