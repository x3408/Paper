package utils;

import entity.Node;
import entity.Task;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class Energy {
    private Map<Integer, Node> nodeMap;
    private LinkedList<Integer> taskQue = new LinkedList<>();

    /**
     * 计算任务最小预分配能耗
     * @param task
     */
    private void calculateTaskMinPreEnergy(Task task) {
        final BigDecimal[] minPreEnergy = new BigDecimal[1];
        minPreEnergy[0] = new BigDecimal("9999999999");
        nodeMap.forEach((nodeId, node) -> {
//            double fee = Math.sqrt(node.getPind() / ((node.getM() - 1) * node.getCef())) * node.getM();
            BigDecimal fee = node.getPind().divide((node.getM().subtract(BigDecimal.ONE).multiply(node.getCef())),8,RoundingMode.HALF_UP).sqrt(new MathContext(2)).multiply(node.getM());
            BigDecimal lowFrequency = node.getMinFrequency().compareTo(fee)>0?node.getMinFrequency():fee;
            minPreEnergy[0] = minPreEnergy[0].min(BigDecimal.valueOf(calculateTaskEnergy(task, node, lowFrequency)));
        });
        task.setMinPreEnergy(minPreEnergy[0]);
    }

    /**
     * 计算任务最大预分配能耗
     * @param task
     */
    private void calculateTaskMaxPreEnergy(Task task) {
        final BigDecimal[] maxPreEnergy = new BigDecimal[1];
        maxPreEnergy[0] = new BigDecimal("-9999999999");

        nodeMap.forEach((nodeId, node) -> maxPreEnergy[0] = maxPreEnergy[0].max(BigDecimal.valueOf(calculateTaskEnergy(task, node, node.getMaxFrequency()))));
//        nodeMap.forEach((nodeId, node) -> {
//            double fee = Math.sqrt(node.getPind() / ((node.getM() - 1) * node.getCef())) * node.getM();
//            maxPreEnergy.set(Math.max(maxPreEnergy.get(), calculateTaskEnergy(task, node, fee)));
//        });
        task.setMaxPreEnergy(maxPreEnergy[0]);
    }

    /**
     * 计算任务能耗
     */
    public static double calculateTaskEnergy(Task task, Node node, BigDecimal frequency) {
        // P = (Pk,ind + Ck,ef × (fk,h)^mk)
        // E = P * w *  fmax/f
        double P = node.getPind().doubleValue() + node.getCef().doubleValue() * Math.pow(frequency.doubleValue(), node.getM().doubleValue());
        return P * Task.EXECUTION_TIME[task.getId()][node.getId()].doubleValue() * (node.getMaxFrequency().doubleValue() / frequency.doubleValue());
    }


    public void calculateTaskPreEnergy(Map<Integer, Task> taskMap, Map<Integer, Node> nodeMap) {
        // 这里将整个map重新拷贝 否则会因为下面的函数重复集合调用而导致死循环
        this.nodeMap = nodeMap;
        // 任务在不同节点上以最小频率执行的能耗值取最小作为预分配能耗
        // 为每个task计算minEnergy,maxEnergyConstraint
        System.out.println("----------------------- 任务能耗上下界 -------------------------");
        System.out.print("minEnergy: ");
        taskMap.forEach((taskId, task) -> {
            calculateTaskMinPreEnergy(task);
            System.out.print(taskId+": "+task.getMinPreEnergy().setScale(2, RoundingMode.HALF_UP) + "    ");
        });
        System.out.println();
        System.out.print("maxEnergy: ");
        taskMap.forEach((taskId, task) -> {
            calculateTaskMaxPreEnergy(task);
            System.out.print(taskId+": "+task.getMaxPreEnergy().setScale(2, RoundingMode.HALF_UP) + "    ");
        });
        System.out.println();
    }
}
