package KMeans;

import entity.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KMeans {
    //聚类的个数
    int clusterNum;
    //数据集中的点
    List<Point> points = new ArrayList<>();
    //簇的中心点
    List<Point> centerPoints = new ArrayList<>();
    //聚类结果的集合簇，key为聚类中心点在centerPoints中的下标，value为该类簇下的数据点
    HashMap<Integer, List<Point>> clusters = new HashMap<>();

    public KMeans(Map<Integer, Task> taskMap, int clusterNum) {
        this.clusterNum = clusterNum;
        loadData(taskMap);
    }

    public void loadData(Map<Integer, Task> taskMap) {
        taskMap.forEach((taskId, task) -> {
            points.add(new Point(task));
        });
        //初始化KMeans模型，这里选数据集前classNum个点作为初始中心点
        centerPoints.add(new Point(25,75)); //CPU
        centerPoints.add(new Point(75,25)); //IO
        centerPoints.add(new Point(25,25)); //COMM
        for (int i = 0; i < clusterNum; i++) {
            clusters.put(i, new ArrayList<>());
        }
    }

    //KMeans聚类
    public void doKMeans(){
        double err = Integer.MAX_VALUE;
        while (err > 0.01 * clusterNum){
            //每次聚类前清空原聚类结果的list
            for (int key : clusters.keySet()){
                List<Point> list = clusters.get(key);
                list.clear();
                clusters.put(key, list);
            }
            //计算每个点所属类簇
            for (int i=0; i<points.size(); i++){
                dispatchPointToCluster(points.get(i), centerPoints);
            }
            //计算每个簇的中心点，并得到中心点偏移误差
            err = getClusterCenterPoint(centerPoints, clusters);
        }
        // 设置任务类别
        for (int i = 0; i < clusters.size(); i++) {
            List<Point> lists = clusters.get(i);
            for (Point list : lists) {
                list.getTask().setCategory(i);
            }
        }
        show(centerPoints, clusters);
    }

    //计算点对应的中心点，并将该点划分到距离最近的中心点的簇中
    public void dispatchPointToCluster(Point point, List<Point> centerPoints){
        int index = 0;
        double tmpMinDistance = Double.MAX_VALUE;
        for (int i=0; i<centerPoints.size(); i++){
            double distance = calDistance(point, centerPoints.get(i));
            if (distance < tmpMinDistance){
                tmpMinDistance = distance;
                index = i;
            }
        }
        List<Point> list = clusters.get(index);
        list.add(point);
        clusters.put(index, list);
    }

    //计算每个类簇的中心点，并返回中心点偏移误差
    public double getClusterCenterPoint(List<Point> centerPoints, HashMap<Integer, List<Point>> clusters){
        double error = 0;
        for (int i=0; i<centerPoints.size(); i++){
            Point tmpCenterPoint = centerPoints.get(i);
            double centerX = 0, centerY = 0;
            List<Point> lists = clusters.get(i);
            for (int j=0; j<lists.size(); j++){
                centerX += lists.get(j).getX();
                centerY += lists.get(j).getY();
            }
            centerX /= lists.size();
            centerY /= lists.size();
            error += Math.abs(centerX - tmpCenterPoint.getX());
            error += Math.abs(centerY - tmpCenterPoint.getY());
            centerPoints.set(i, new Point(centerX, centerY));
        }
        return error;
    }

    //计算点之间的距离，这里计算欧氏距离（不开方）
    public double calDistance(Point point1, Point point2){
        return Math.pow((point1.getX() - point2.getX()), 2) + Math.pow((point1.getY() - point2.getY()), 2);
    }

    //打印簇中心点坐标，及簇中其他点坐标
    public void show(List<Point> centerPoints, HashMap<Integer, List<Point>> clusters){
        System.out.println("----------------------- 任务聚类结果 ---------------------------");
        for (int i=0; i<centerPoints.size(); i++){
            switch (i) {
                case 0:
                    System.out.print(MessageFormat.format("CPU类的中心点: <{0}, {1}>\t成员点有：", centerPoints.get(i).getX(), centerPoints.get(i).getY()));
                    break;
                case 1:
                    System.out.print(MessageFormat.format("IO类的中心点: <{0}, {1}>\t成员点有：", centerPoints.get(i).getX(), centerPoints.get(i).getY()));
                    break;
                case 2:
                    System.out.print(MessageFormat.format("COMM类的中心点: <{0}, {1}>\t成员点有：", centerPoints.get(i).getX(), centerPoints.get(i).getY()));
                    break;
            }
            List<Point> lists = clusters.get(i);
            for (int j=0; j<lists.size(); j++){
                System.out.print("<"+lists.get(j).getTask().getId()+">\t");
            }
            System.out.println();
        }
    }

    //加载数据集
    public void loadData(String path) {
        File file = new File(path);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] strs = line.split(" ");
                points.add(new Point(strs[0], strs[1]));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //初始化KMeans模型，这里选数据集前classNum个点作为初始中心点
        for (int i = 0; i < clusterNum; i++) {
            centerPoints.add(points.get(i));    // 以前几个点作为初始
            clusters.put(i, new ArrayList<>());
        }
    }
}