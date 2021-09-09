package utils;

import entity.Node;
import entity.Task;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Energy {
    private double applicationEnergyConstraint;
    private Map<Integer, Task> taskMap;
    private Map<Integer, Node> nodeMap;
    private LinkedList<Integer> taskQue = new LinkedList<>();
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
        taskMap.forEach((taskId, task) -> taskQue.add(taskId));
        this.nodeMap = nodeMap;
        // 任务在不同节点上以最小频率执行的能耗值取最小作为预分配能耗
        // 为每个task计算minEnergy,maxEnergyConstraint
        System.out.println("----------------------- 任务的能耗约束值 -------------------------");
        taskMap.forEach((taskId, task) -> {
            calculateTaskMinPreEnergy(task);
            System.out.print("minEnergy: ");
            System.out.print(taskId+": "+String.format("%.2f",task.getMinPreEnergy()) + "    ");
        });
        System.out.println();
        taskMap.forEach((taskId, task) -> {
            calculateTaskMaxPreEnergy(task);
            System.out.print("maxEnergy: ");
            System.out.print(taskId+": "+String.format("%.2f",task.getMaxPreEnergy()) + "    ");
        });
        // 再重新计算实际能耗约束并设置属性值
        taskMap.forEach((taskId, task) -> calculateTaskRealEnergyConstraint(task));
        // 输出实际任务能耗限制结果
        System.out.println();
        taskMap.forEach((id, t) -> System.out.print(t.getId() + ": " + String.format("%.2f",t.getEnergyConstraint()) + "    "));
        System.out.println();
    }

    /**
     * 计算单个任务能耗约束
     * @param task
     */
    private void calculateTaskRealEnergyConstraint(Task task) {
        double realEnergyConstraintSum=0, preMinEnergyConstraintSum=0;
//        // 计算任务i以前的任务集合的真实能耗
//        for (int i = 1; i <= task.getId()-1 ; i++) {
//            realEnergyConstraintSum+=taskMap.get(i).getEnergyConstraint();
//        }
//        // 计算任务i以后的任务集合的预分配能耗
//        for (int i = task.getId()+1; i <= taskMap.size() ; i++) {
//            preMinEnergyConstraintSum+=taskMap.get(i).getMinPreEnergy();
//        }
        int index=0;    // 记录当前task的下标
        for (int i = 0; taskQue.get(i) != task.getId(); index=++i) {
            realEnergyConstraintSum+=taskMap.get(taskQue.get(i)).getEnergyConstraint();
        }
        for (int i = index+1; i<taskQue.size(); i++) {
            preMinEnergyConstraintSum+=taskMap.get(taskQue.get(i)).getMinPreEnergy();
        }

        // 计算任务i的能耗约束
        double energyConstraint = applicationEnergyConstraint - realEnergyConstraintSum - preMinEnergyConstraintSum;
        // 设置能耗约束在规定范围内
        energyConstraint = Math.max(task.getMinPreEnergy(), energyConstraint);
        energyConstraint = Math.min(task.getMaxPreEnergy(), energyConstraint);
        task.setEnergyConstraint(energyConstraint);
    }

    /**
     * 计算任务最小预分配能耗
     * @param task
     */
    private void calculateTaskMinPreEnergy(Task task) {
        AtomicReference<Double> minPreEnergy= new AtomicReference<>(Double.MAX_VALUE);
        nodeMap.forEach((nodeId, node) -> {
            double fee = Math.sqrt(node.getPind() / ((node.getM() - 1) * node.getCef())) * node.getM();
//            minPreEnergy.set(Math.min(minPreEnergy.get(), calculateTaskEnergy(task, node, Math.max(fee, node.getMinFrequency()))));
//            minPreEnergy.set(Math.min(minPreEnergy.get(), calculateTaskEnergy(task, node, fee)));
            minPreEnergy.set(Math.min(minPreEnergy.get(), calculateTaskEnergy(task, node, node.getMinFrequency())));
        });
        task.setMinPreEnergy(minPreEnergy.get());
    }

    /**
     * 计算任务最大预分配能耗
     * @param task
     */
    private void calculateTaskMaxPreEnergy(Task task) {
        AtomicReference<Double> maxPreEnergy= new AtomicReference<>(Double.MIN_VALUE);
        nodeMap.forEach((nodeId, node) -> maxPreEnergy.set(Math.max(maxPreEnergy.get(), calculateTaskEnergy(task, node, node.getMaxFrequency()))));
//        nodeMap.forEach((nodeId, node) -> {
//            double fee = Math.sqrt(node.getPind() / ((node.getM() - 1) * node.getCef())) * node.getM();
//            maxPreEnergy.set(Math.max(maxPreEnergy.get(), calculateTaskEnergy(task, node, fee)));
//        });
        task.setMaxPreEnergy(maxPreEnergy.get());
    }

    /**
     * 计算任务能耗
     */
    public static double calculateTaskEnergy(Task task, Node node, double frequency) {
        // P = (Pk,ind + Ck,ef × (fk,h)^mk)
        // E = P * w *  fmax/f
        double P = node.getPind() + node.getCef() * Math.pow(frequency, node.getM());
        return P * Task.EXECUTION_TIME[task.getId()][node.getId()] * (node.getMaxFrequency() / frequency);
    }

}
