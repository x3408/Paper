package utils;

import entity.Task;

import java.util.Map;

public class Order {
    public static void quickSort(Map<Integer, Task> taskMap, int left, int right) {
        if (left > right) return;
        int index = partition(taskMap, left, right);
        quickSort(taskMap, left, index-1);
        quickSort(taskMap, index+1, right);
    }

    private static int partition(Map<Integer, Task> taskMap, int left, int right) {
        int pivot = left;
        int index = pivot + 1;
        for (int i=index; i<right; i++) {
            if (taskMap.get(i).getAverageExecutionTime()<taskMap.get(pivot).getAverageExecutionTime()) {
                swap(taskMap, i, index);
                index++;
            }
        }
        return 0;
    }

    private static void swap(Map<Integer, Task> taskMap, int i, int index) {
        return;
    }
}
