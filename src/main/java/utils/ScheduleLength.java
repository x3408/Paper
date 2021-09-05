package utils;

import DAG.IDirectGraph;
import entity.Node;
import entity.Task;

import java.util.Set;

public class ScheduleLength {

    public static double calculateEarlierFinishTime(Task task, Node node, double frequency, IDirectGraph<Task> directGraph) {
        double EST = calculateEarlierStartTime(task, node, directGraph);
        double EFT = EST + Task.EXECUTION_TIME[task.getId()][node.getId()] * node.getMaxFrequency() / frequency;
        node.setAvailableTime(EFT);
        task.setEFT(EFT);

        return EFT;
    }

    public static double calculateEarlierStartTime(Task task, Node node, IDirectGraph<Task> directGraph) {
        if (task.getId()==1) return 0;

        double nodeAvailTime = node.getAvailableTime();
        double maxPreTaskTime = Double.MIN_VALUE;
        // 前继节点的最大EFT
        Set<Task> predecessor = directGraph.getPredecessor(task);
        for (Task preTask : predecessor) {
            maxPreTaskTime = Math.min(maxPreTaskTime, preTask.getEFT() + directGraph.getEdge(preTask.getId()-1, task.getId()-1).getWeight());
        }
        return Math.max(nodeAvailTime, maxPreTaskTime);
    }
}
