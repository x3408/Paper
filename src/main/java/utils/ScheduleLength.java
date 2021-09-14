package utils;

import DAG.IDirectGraph;
import entity.Node;
import entity.Task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

public class ScheduleLength {

    public static BigDecimal calculateEarlierFinishTime(Task task, Node node, BigDecimal frequency, IDirectGraph<Task> directGraph) {
        BigDecimal EST = calculateEarlierStartTime(task, node, directGraph);

        return EST.add(Task.EXECUTION_TIME[task.getId()][node.getId()].multiply(node.getMaxFrequency().divide(frequency, 8,  RoundingMode.HALF_UP)));
    }

    public static BigDecimal calculateEarlierStartTime(Task task, Node node, IDirectGraph<Task> directGraph) {
        if (task.getId()==1) return BigDecimal.ZERO;

        BigDecimal nodeAvailTime = node.getAvailableTime();
        BigDecimal maxPreTaskTime = BigDecimal.ZERO;
        // 前继节点的最大EFT
        Set<Task> predecessor = directGraph.getPredecessor(task);
        for (Task preTask : predecessor) {
            BigDecimal weight = directGraph.getEdge(preTask.getId() - 1, task.getId() - 1).getWeight();
            if (preTask.getExecuteNode().getId()==node.getId()) weight=BigDecimal.ZERO;
            maxPreTaskTime = maxPreTaskTime.max(preTask.getEFT().add(weight));
        }
        return nodeAvailTime.max(maxPreTaskTime);
    }
}
