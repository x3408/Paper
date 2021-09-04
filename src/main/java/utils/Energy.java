package utils;

import entity.Node;
import entity.Task;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Energy {
    private double applicationEnergyConstraint;
    private Map<Integer, Task> taskMap;
    private Map<Integer, Node> nodeMap;
    /**
     * 计算所有的任务能耗约束
     * @param applicationEnergyConstraint
     * @param taskMap
     * @param nodeMap
     */
    public void calculateTaskEnergyConstraint(double applicationEnergyConstraint, Map<Integer, Task> taskMap, Map<Integer, Node> nodeMap) {
        this.applicationEnergyConstraint = applicationEnergyConstraint;
        // 这里将整个map重新拷贝 否则会因为下面的函数重复集合调用而导致死循环
        this.taskMap = new LinkedHashMap<>(taskMap);
        this.nodeMap = nodeMap;
        // 任务在不同节点上以最小频率执行的能耗值取最小作为预分配能耗
        // 为每个task计算minEnergy
        taskMap.forEach((taskId, task) -> calculateTaskMinPreEnergy(task));
        // 再重新计算实际能耗约束并设置属性值
        taskMap.forEach((taskId, task) -> calculateTaskRealEnergyConstraint(task));
        // 输出结果
        taskMap.forEach((id, t) -> System.out.print("任务Id:"+t.getId() + "-能耗约束值:" + t.getEnergyConstraint() + "\t"));
        System.out.println();
    }

    /**
     * 计算单个任务能耗约束
     * @param task
     */
    private void calculateTaskRealEnergyConstraint(Task task) {
        double realEnergyConstraintSum=0, preMinEnergyConstraintSum=0;
        // 计算任务i以前的任务集合的真实能耗
        for (int i = 1; i <= task.getId()-1 ; i++) {
            realEnergyConstraintSum+=taskMap.get(i).getEnergyConstraint();
        }
        // 计算任务i以后的任务集合的预分配能耗
        for (int i = task.getId()+1; i <= taskMap.size() ; i++) {
            preMinEnergyConstraintSum+=taskMap.get(i).getMinPreEnergy();
        }
        // 计算任务i的能耗约束
        double energyConstraint = applicationEnergyConstraint - realEnergyConstraintSum - preMinEnergyConstraintSum;
        task.setEnergyConstraint(energyConstraint);
    }

    /**
     * 计算任务最小预分配能耗
     * @param task
     */
    private void calculateTaskMinPreEnergy(Task task) {
        AtomicReference<Double> minPreEnergy= new AtomicReference<>(Double.MAX_VALUE);
        // 计算任务在哪个节点上的能耗最小
        nodeMap.forEach((nodeId, node) -> minPreEnergy.set(Math.min(minPreEnergy.get(), calculateTaskEnergy(task, node, node.getMinFrequency()))));
        task.setMinPreEnergy(minPreEnergy.get());
    }

    /**
     * 计算任务能耗
     */
    public static double calculateTaskEnergy(Task task, Node node, double frequency) {
        // E = P * w *  fmax/f
        // P = (Pk,ind + Ck,ef × (fk,h)^mk)
        double P = node.getPind() + node.getCef() * Math.pow(frequency, node.getM());
        return P * Task.EXECUTION_TIME[task.getId()][node.getId()] * node.getMaxFrequency()/ frequency;
    }

}
