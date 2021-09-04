package KMeans;

import entity.Task;
import lombok.Data;

@Data
public class Point {
    private double x;
    private double y;
    private Task task;

    public Point(Task task) {
        this.task = task;
        this.x = task.getMemory();
        this.y = task.getCpu();
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    //读文件初始化时需要用到
    public Point(String x, String y){
        this.x = Double.parseDouble(x);
        this.y = Double.parseDouble(y);
    }
}
