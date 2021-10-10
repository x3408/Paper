package utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import entity.ExcelResult;
import entity.Node;
import entity.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelUtil {
    private static final String OUTPUTPATH = "C:\\Users\\x3408\\Desktop\\";

    public static void writeToTable(Map<Integer, Task> taskMap, String applicationScheduleLength, String applicationEnergy) {
        String fileName = OUTPUTPATH + "实验结果" + System.currentTimeMillis() + ".xlsx";
        // 这里 需要指定写用哪个class去写
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(fileName, ExcelResult.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("模板").build();
            excelWriter.write(data(taskMap, applicationScheduleLength, applicationEnergy), writeSheet);
        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    private static List<ExcelResult> data(Map<Integer, Task> taskMap,String applicationScheduleLength, String applicationEnergy) {
        ArrayList<ExcelResult> res = new ArrayList<>();

        for (Map.Entry<Integer, Task> entry : taskMap.entrySet()) {
            Task task = entry.getValue();
            ExcelResult item = new ExcelResult();
            item.setTaskId(task.getId());
            item.setEFT(task.getEFT().toString());
            item.setTaskEnergyConstraint(task.getEnergyConstraint().toString());
            item.setTaskFrequency(task.getFrequency().toString());
            item.setTaskExecuteNode(task.getExecuteNode().getId());
            item.setScheduleLength(applicationScheduleLength);
            item.setFinalEnergy(applicationEnergy);

            res.add(item);
        }
        return res;
    }
}
