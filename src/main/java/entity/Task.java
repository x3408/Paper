package entity;

import lombok.Builder;
import lombok.Data;
import java.util.Objects;

@Data
@Builder
public class Task {
    private int id;
    private double averageExecutionTime;                // 任务在节点上的平均执行时长
    private double rankScore;                           // 优先级分数
    private double cpu;                                 // 任务cpu要求
    private double memory;                              // 任务IO要求
    private int category;                               // 任务所属分类0:CPU 1:IO 2:COMM
    private double minPreEnergy;                        // 任务最小预分配能耗
    private double maxPreEnergy;                        // 任务最大预分配能耗
    private double energyConstraint;                    // 任务能耗约束

    public static double[][] EXECUTION_TIME;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
