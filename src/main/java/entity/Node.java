package entity;

import DAG.IDirectGraph;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utils.Energy;
import utils.ScheduleLength;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {
     private int id;
     private BigDecimal minFrequency;
     private BigDecimal maxFrequency;
     private BigDecimal accuracy;
     private BigDecimal availableTime;

     private BigDecimal Pind;
     private BigDecimal Cef;
     private BigDecimal M;
     private int type;

     public void getSuitableNodeforTask(BigDecimal applicationEnergyConstraint, Map<Integer, Task> taskMap, Map<Integer, Node> nodeMap, IDirectGraph<Task> directGraph) {
          ArrayList<Integer> taskQue = new ArrayList<>();
          taskMap.forEach((taskId, task) -> taskQue.add(taskId));
          System.out.println("------------------------ 任务能耗约束值 ------------------------");
          for (Map.Entry<Integer, Task> taskEntry : taskMap.entrySet()) {
               BigDecimal AFT = new BigDecimal("9999999999");
               BigDecimal EFT;
               Node executeNode = null;
               BigDecimal executeFrequency = null;
               BigDecimal energyConsumption = null;
               Task task = taskEntry.getValue();
               // 计算当前任务能耗约束
               calculateRealEnergyConstraintForTask(applicationEnergyConstraint, task, new HashMap<>(taskMap), taskQue);
               // 遍历节点尝试找到最小能耗的节点与频率
               for (Map.Entry<Integer, Node> nodeEntry : nodeMap.entrySet()) {
                    Node node = nodeEntry.getValue();
                    // 遍历每个频率
                    for (BigDecimal frequency=node.getMinFrequency(); frequency.compareTo(node.getMaxFrequency())<=0; frequency = frequency.add(node.getAccuracy())) {
                         double realEnergy = Energy.calculateTaskEnergy(task, node, frequency);
                         if (BigDecimal.valueOf(realEnergy).compareTo(task.getEnergyConstraint()) == 1) continue; // 过滤掉不符合规则的
                         // calculate EFT
                         EFT = ScheduleLength.calculateEarlierFinishTime(task, node, frequency, directGraph);
                         // 若EFT小于AFT 则任务选择在该节点上执行
                         if (EFT.compareTo(AFT) == -1) {
                              AFT = EFT;
                              executeNode = node;
                              executeFrequency = frequency;
                              energyConsumption = BigDecimal.valueOf(realEnergy);
                         }
                    }
               }
               assert executeNode != null;
               task.setEFT(AFT);
               // 设置备用能耗最低节点
               task.setExecuteNode(executeNode);
               executeNode.setAvailableTime(AFT);
               task.setFrequency(executeFrequency);
               task.setFinalEnergy(energyConsumption);
               // 尝试使用类型一致节点
               AFT = new BigDecimal("9999999999");
               executeNode = null;
               executeFrequency = null;
               energyConsumption = null;
               for (Map.Entry<Integer, Node> nodeEntry : nodeMap.entrySet()) {
                    Node node = nodeEntry.getValue();
                    if (node.getType()!=task.getType()) continue;
                    // 遍历每个频率
                    for (BigDecimal frequency=node.getMinFrequency(); frequency.compareTo(node.getMaxFrequency())<=0; frequency = frequency.add(node.getAccuracy())) {
                         double realEnergy = Energy.calculateTaskEnergy(task, node, frequency);
                         if (BigDecimal.valueOf(realEnergy).compareTo(task.getEnergyConstraint()) == 1) continue; // 过滤掉不符合规则的
                         // calculate EFT
                         EFT = ScheduleLength.calculateEarlierFinishTime(task, node, frequency, directGraph);
                         // 若EFT小于AFT 则任务选择在该节点上执行
                         if (EFT.compareTo(AFT) == -1) {
                              AFT = EFT;
                              executeNode = node;
                              executeFrequency = frequency;
                              energyConsumption = BigDecimal.valueOf(realEnergy);
                         }
                    }
               }
               if (executeNode==null) continue;
               task.setEFT(AFT);
               // 设置备用能耗最低节点
               task.setExecuteNode(executeNode);
               executeNode.setAvailableTime(AFT);
               task.setFrequency(executeFrequency);
               task.setFinalEnergy(energyConsumption);
          }
     }

     private void calculateRealEnergyConstraintForTask(BigDecimal applicationEnergyConstraint, Task task, HashMap<Integer, Task> taskMap, ArrayList<Integer> taskQue) {
          BigDecimal realEnergyConstraintSum = new BigDecimal("0");
          BigDecimal preMinEnergyConstraintSum = new BigDecimal("0");
//        // 计算任务i以前的任务集合的真实能耗
          int index=0;    // 记录当前task的下标
          for (int i = 0; taskQue.get(i) != task.getId(); index=++i) {
               realEnergyConstraintSum=realEnergyConstraintSum.add(taskMap.get(taskQue.get(i)).getFinalEnergy());
          }
          for (int i = index+1; i<taskQue.size(); i++) {
               preMinEnergyConstraintSum=preMinEnergyConstraintSum.add(taskMap.get(taskQue.get(i)).getMinPreEnergy());
          }

          // 计算任务i的能耗约束
          BigDecimal energyConstraint = applicationEnergyConstraint.subtract(realEnergyConstraintSum.add(preMinEnergyConstraintSum));

          // 设置能耗约束在规定范围内
          energyConstraint = task.getMinPreEnergy().max(energyConstraint);
          energyConstraint = task.getMaxPreEnergy().min(energyConstraint);
          task.setEnergyConstraint(energyConstraint);
          // sout
          System.out.print(task.getEnergyConstraint().setScale(2, RoundingMode.HALF_UP) + "   ");
     }
}