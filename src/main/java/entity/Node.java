package entity;

import DAG.IDirectGraph;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utils.Energy;
import utils.ScheduleLength;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {
     private int id;
     private double minFrequency;
     private double maxFrequency;
     private double accuracy;
     private double availableTime;

     private double Pind;
     private double Cef;
     private double M;

     public void getSuitableNode(Map<Integer, Task> taskMap, Map<Integer, Node> nodeMap, IDirectGraph<Task> directGraph) {
          System.out.println("----------------------  任务执行EFT -----------------------------");
          for (Map.Entry<Integer,Task> taskEntry : taskMap.entrySet()) {
               double AFT = Double.MAX_VALUE;
               double EFT = Double.MAX_VALUE;
               Task task = taskEntry.getValue();
               for (Map.Entry<Integer,Node> nodeEntry : nodeMap.entrySet()) {
                    Node node = nodeEntry.getValue();
                    // 遍历每个频率
                    for (double frequency=node.getMinFrequency(); frequency<node.getMaxFrequency(); frequency+=node.getAccuracy()) {
                         double realEnergy = Energy.calculateTaskEnergy(task, node, frequency);
                         if (realEnergy > task.getEnergyConstraint()) continue; // 过滤掉不符合规则的
                         // calculate EFT
                         EFT = ScheduleLength.calculateEarlierFinishTime(task, node, frequency, directGraph);
                         // 若EFT小于AFT 则任务选择在该节点上执行
                         if (EFT < AFT) {
                              AFT = EFT;
                              task.setExecuteNode(node);
                              task.setEFT(AFT);
                              node.setAvailableTime(AFT);
                              task.setFrequency(frequency);
                              task.setFinalEnergy(realEnergy);
                         }
                    }
               }
               System.out.print("task:"+task.getId() + " node: " + task.getExecuteNode().getId()+ " EFT:" + String.format("%.2f", task.getEFT()) + "   ");
          }
          System.out.println();
     }
}
