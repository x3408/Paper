package entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

@Data
@Builder
public class Task {
    private int id;
    private BigDecimal averageExecutionTime;                // 任务在节点上的平均执行时长
    private BigDecimal rankScore;                           // 优先级分数
    private BigDecimal cpu;                                 // 任务cpu要求
    private BigDecimal memory;                              // 任务IO要求
    private int type;                               // 任务所属分类0:CPU 1:IO 2:COMM
    private BigDecimal minPreEnergy;                        // 任务最小预分配能耗
    private BigDecimal maxPreEnergy;                        // 任务最大预分配能耗
    private BigDecimal finalEnergy;                         // 最终任务能耗
    private BigDecimal energyConstraint;                    // 任务能耗约束
    private BigDecimal EFT;                                 // 任务最早结束时间
    private Node executeNode;                           // 真正执行任务的节点
    private BigDecimal frequency;                           // 执行频率

    public static BigDecimal[][] EXECUTION_TIME;

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
