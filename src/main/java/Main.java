import DAG.Edge;
import DAG.IDirectGraph;
import DAG.ListDirectGraph;
import KMeans.KMeans;
import entity.Node;
import entity.Task;
import utils.Energy;
import utils.ScheduleLength;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class Main {
    public static final BigDecimal APPLICATION_ENERGY_CONSTRAINT= new BigDecimal("80.995");

    public static final int TASK_NUM = 10;
    public static final int NODE_NUM = 3;
    public static final int WDAG = 10;      // 任务全局平均执行时长(目前是随便设定的)
    public static final double β = 0.5;     // β的取值为0.1,0.25,0.5,0.75,1.0

    private static final Map<Integer, Task> taskMap = new LinkedHashMap<>(16, 0.75f, true);  // 任务需要有序
    private static final Map<Integer, Node> nodeMap = new HashMap<>();
    private static final IDirectGraph<Task> directGraph = new ListDirectGraph<>();  // 按任务id-1作为下标存放

    public static BigDecimal applicationEnergy = new BigDecimal("0");
    public static BigDecimal applicationScheduleLength = new BigDecimal("0");
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
//        energy.calculateTaskEnergyConstraint(APPLICATION_ENERGY_CONSTRAINT, taskMap, nodeMap);
        energy.calculateTaskPreEnergy(taskMap, nodeMap);
        // 计算任务在不同处理器的不同频率上的执行能耗，满足条件后再计算EFT
        Node node = new Node();
//        node.getSuitableNode(taskMap, nodeMap, directGraph);
        node.getSuitableNodeforTask(APPLICATION_ENERGY_CONSTRAINT, taskMap, nodeMap, directGraph);
        // 计算调度时间SL(G)、E(G)
        calculateApplicationEnergyConsumption();
        calculateApplicationScheduleLength();

        compareWithOrigin();
        calculateApplicationEnergyConsumption();
        calculateApplicationScheduleLength();

        compareWithRandom();

    }

    private static void compareWithRandom() {
        for (Map.Entry<Integer, Task> taskEntry : taskMap.entrySet()) {
            Task task = taskEntry.getValue();
            // 遍历节点尝试找到最小能耗的节点与频率
            for (Map.Entry<Integer, Node> nodeEntry : nodeMap.entrySet()) {
                Node node = nodeEntry.getValue();
                if (node.getType()!=task.getType()) continue;
                task.setExecuteNode(node);
                double finalEnergy = Energy.calculateTaskEnergy(task, node, node.getMinFrequency());
                BigDecimal EFT = ScheduleLength.calculateEarlierFinishTime(task, node, node.getMinFrequency(), directGraph);
                node.setAvailableTime(EFT);
                task.setEFT(EFT);
                task.setFinalEnergy(BigDecimal.valueOf(finalEnergy));
            }
        }
    }

    private static void compareWithOrigin() {
        for (Map.Entry<Integer, Task> taskEntry : taskMap.entrySet()) {
            Task task = taskEntry.getValue();
            // 遍历节点尝试找到最小能耗的节点与频率
            for (Map.Entry<Integer, Node> nodeEntry : nodeMap.entrySet()) {
                Node node = nodeEntry.getValue();
                if (node.getType()!=task.getType()) continue;
                task.setExecuteNode(node);
                double finalEnergy = Energy.calculateTaskEnergy(task, node, node.getMinFrequency());
                BigDecimal EFT = ScheduleLength.calculateEarlierFinishTime(task, node, node.getMinFrequency(), directGraph);
                node.setAvailableTime(EFT);
                task.setEFT(EFT);
                task.setFinalEnergy(BigDecimal.valueOf(finalEnergy));
            }
        }
    }

    private static void calculateApplicationScheduleLength() {
        System.out.println("------------------------ 全局调度长度 ---------------------------");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        applicationScheduleLength = taskMap.get(taskMap.size()).getEFT();
        taskMap.forEach((taskId, task) -> System.out.print(decimalFormat.format(task.getEFT()) + "  "));
        System.out.println();
        System.out.println("应用最终调度长度: " + applicationScheduleLength);
    }

    private static void calculateApplicationEnergyConsumption() {
        System.out.println();
        System.out.println("------------------------ 全局能耗 ------------------------------");
        System.out.print("能耗：");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        taskMap.forEach((taskId, task) -> {
            System.out.print(task.getExecuteNode().getId() + ": " + decimalFormat.format(task.getFinalEnergy()) + "  ");
            applicationEnergy = applicationEnergy.add(task.getFinalEnergy());
        });
        System.out.println();
        System.out.print("频率：");
        taskMap.forEach((taskId, task) -> {
            System.out.print(taskId + ": " + task.getFrequency().setScale(2, RoundingMode.HALF_UP) + "  ");
        });
        System.out.println();
        System.out.println("能耗上限: "+ APPLICATION_ENERGY_CONSTRAINT+ "\t应用最终能耗: " + applicationEnergy);
    }

    /**
     * 根据优先级得分降序排序任务
     */
    public static void OrderTasks() {
        List<BigDecimal> scoreRes = new ArrayList<>();

        // 为每个任务设置平均执行时间
        for (int i = TASK_NUM; i>=1; i--) {
            Task task = taskMap.get(i);
            BigDecimal rankScore = calculatePriority(task);
            scoreRes.add(rankScore);
        }
        Collections.reverse(scoreRes);
        System.out.println("-------------------- 任务得分(按下标输出） -----------------------");
        scoreRes.forEach(score -> System.out.print(score.setScale(1, RoundingMode.HALF_UP)+"\t"));
        System.out.println();

        // 重新排序
        List<Map.Entry<Integer, Task>> entries = new ArrayList<>(taskMap.entrySet());
        entries.sort((left, right) -> {
            BigDecimal res =  right.getValue().getRankScore().subtract(left.getValue().getRankScore());
            if (res.intValue()==0) return -1;
            return res.intValue();
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
    private static BigDecimal calculatePriority(Task task) {
        // exit任务的分数为平均执行时间
        if (task.getId()==TASK_NUM) {
            task.setRankScore(task.getAverageExecutionTime());
            return task.getRankScore();
        }

        // 平均执行时间+前驱节点中得分加通讯分数最大的
        // 1.获得后继节点的得分与通讯时间
        BigDecimal succeedScore= new BigDecimal("0");
        for (Task succeed : directGraph.getSucceed(task)) {
            BigDecimal weight = directGraph.getEdge(task.getId()-1, succeed.getId()-1).getWeight();
            succeedScore = succeedScore.max(succeed.getRankScore().add(weight));
        }
        BigDecimal finalScore = task.getAverageExecutionTime().add(succeedScore);
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
        Task.EXECUTION_TIME = new BigDecimal[TASK_NUM+1][NODE_NUM+1];
        for (int i=1; i<=TASK_NUM; i++) {
            Task task = Task.builder().id(i).cpu(BigDecimal.valueOf(random.nextDouble()*100)).memory(BigDecimal.valueOf(random.nextDouble()*100)).build();
            taskMap.put(i, task);
            directGraph.addVertex(task);
        }
        taskMap.get(1).setAverageExecutionTime(BigDecimal.valueOf(13));
        taskMap.get(2).setAverageExecutionTime(BigDecimal.valueOf(50f/3));
        taskMap.get(3).setAverageExecutionTime(BigDecimal.valueOf(43f/3));
        taskMap.get(4).setAverageExecutionTime(BigDecimal.valueOf(38f/3));
        taskMap.get(5).setAverageExecutionTime(BigDecimal.valueOf(35f/3));
        taskMap.get(6).setAverageExecutionTime(BigDecimal.valueOf(38f/3));
        taskMap.get(7).setAverageExecutionTime(BigDecimal.valueOf(11));
        taskMap.get(8).setAverageExecutionTime(BigDecimal.valueOf(10));
        taskMap.get(9).setAverageExecutionTime(BigDecimal.valueOf(50f/3));
        taskMap.get(10).setAverageExecutionTime(BigDecimal.valueOf(44f/3));

        Task.EXECUTION_TIME[1][1] = BigDecimal.valueOf(14);
        Task.EXECUTION_TIME[1][2] = BigDecimal.valueOf(16);
        Task.EXECUTION_TIME[1][3] = BigDecimal.valueOf(9);
        Task.EXECUTION_TIME[2][1] = BigDecimal.valueOf(13);
        Task.EXECUTION_TIME[2][2] = BigDecimal.valueOf(19);
        Task.EXECUTION_TIME[2][3] = BigDecimal.valueOf(18);
        Task.EXECUTION_TIME[3][1] = BigDecimal.valueOf(11);
        Task.EXECUTION_TIME[3][2] = BigDecimal.valueOf(13);
        Task.EXECUTION_TIME[3][3] = BigDecimal.valueOf(19);
        Task.EXECUTION_TIME[4][1] = BigDecimal.valueOf(13);
        Task.EXECUTION_TIME[4][2] = BigDecimal.valueOf(8);
        Task.EXECUTION_TIME[4][3] = BigDecimal.valueOf(17);
        Task.EXECUTION_TIME[5][1] = BigDecimal.valueOf(12);
        Task.EXECUTION_TIME[5][2] = BigDecimal.valueOf(13);
        Task.EXECUTION_TIME[5][3] = BigDecimal.valueOf(10);
        Task.EXECUTION_TIME[6][1] = BigDecimal.valueOf(13);
        Task.EXECUTION_TIME[6][2] = BigDecimal.valueOf(16);
        Task.EXECUTION_TIME[6][3] = BigDecimal.valueOf(9);
        Task.EXECUTION_TIME[7][1] = BigDecimal.valueOf(7);
        Task.EXECUTION_TIME[7][2] = BigDecimal.valueOf(15);
        Task.EXECUTION_TIME[7][3] = BigDecimal.valueOf(11);
        Task.EXECUTION_TIME[8][1] = BigDecimal.valueOf(5);
        Task.EXECUTION_TIME[8][2] = BigDecimal.valueOf(11);
        Task.EXECUTION_TIME[8][3] = BigDecimal.valueOf(14);
        Task.EXECUTION_TIME[9][1] = BigDecimal.valueOf(18);
        Task.EXECUTION_TIME[9][2] = BigDecimal.valueOf(12);
        Task.EXECUTION_TIME[9][3] = BigDecimal.valueOf(20);
        Task.EXECUTION_TIME[10][1] = BigDecimal.valueOf(21);
        Task.EXECUTION_TIME[10][2] = BigDecimal.valueOf(7);
        Task.EXECUTION_TIME[10][3] = BigDecimal.valueOf(16);

        directGraph.addEdge(new Edge<>(taskMap.get(1), taskMap.get(2),new BigDecimal("18")));
        directGraph.addEdge(new Edge<>(taskMap.get(1), taskMap.get(3),new BigDecimal("12")));
        directGraph.addEdge(new Edge<>(taskMap.get(1), taskMap.get(4),new BigDecimal("9")));
        directGraph.addEdge(new Edge<>(taskMap.get(1), taskMap.get(5),new BigDecimal("11")));
        directGraph.addEdge(new Edge<>(taskMap.get(1), taskMap.get(6),new BigDecimal("14")));
        directGraph.addEdge(new Edge<>(taskMap.get(2), taskMap.get(8),new BigDecimal("19")));
        directGraph.addEdge(new Edge<>(taskMap.get(2), taskMap.get(9),new BigDecimal("16")));
        directGraph.addEdge(new Edge<>(taskMap.get(3), taskMap.get(7),new BigDecimal("23")));
        directGraph.addEdge(new Edge<>(taskMap.get(4), taskMap.get(8),new BigDecimal("27")));
        directGraph.addEdge(new Edge<>(taskMap.get(4), taskMap.get(9),new BigDecimal("23")));
        directGraph.addEdge(new Edge<>(taskMap.get(5), taskMap.get(9),new BigDecimal("13")));
        directGraph.addEdge(new Edge<>(taskMap.get(6), taskMap.get(8),new BigDecimal("15")));
        directGraph.addEdge(new Edge<>(taskMap.get(7), taskMap.get(10),new BigDecimal("17")));
        directGraph.addEdge(new Edge<>(taskMap.get(8), taskMap.get(10),new BigDecimal("11")));
        directGraph.addEdge(new Edge<>(taskMap.get(9), taskMap.get(10),new BigDecimal("13")));
    }


    private static void initVirtualMachine() {
        // 初始化虚拟机性能
//        for (int i = 1; i <= NODE_NUM; i++) {
//            // 这里测试同构集群
//            Node node = new Node(i, 0.26, 1, 0.26, 0.01, 0, 0.03, 0.8, 2.9);
//            // 添加到DAG中获得优先级
//            nodeMap.put(i, node);
//        }

        Node node1 = new Node(1, new BigDecimal("0.26"), new BigDecimal("1"), new BigDecimal("0.01"), new BigDecimal("0"), new BigDecimal("0.03"), new BigDecimal("0.8"), new BigDecimal("2.9"),0);
        Node node2 = new Node(2, new BigDecimal("0.26"), new BigDecimal("1"), new BigDecimal("0.01"), new BigDecimal("0"), new BigDecimal("0.04"), new BigDecimal("0.8"), new BigDecimal("2.5"),1);
        Node node3 = new Node(3, new BigDecimal("0.29"), new BigDecimal("1"), new BigDecimal("0.01"), new BigDecimal("0"), new BigDecimal("0.07"), new BigDecimal("1"), new BigDecimal("2.5"),2);
        nodeMap.put(1, node1);
        nodeMap.put(2, node2);
        nodeMap.put(3, node3);
    }


}
