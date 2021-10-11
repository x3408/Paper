package utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import entity.ExcelApplicationResult;
import entity.ExcelTaskResult;
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
            excelWriter = EasyExcel.write(fileName, ExcelTaskResult.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("sheet1").build();
            excelWriter.write(getTaskResult(taskMap), writeSheet);
            excelWriter.write(getApplicationResult(applicationScheduleLength, applicationEnergy), writeSheet);
        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    private static List<ExcelApplicationResult> getApplicationResult(String applicationScheduleLength, String applicationEnergy) {
        ArrayList<ExcelApplicationResult> excelApplicationResults = new ArrayList<>();
        ExcelApplicationResult res = new ExcelApplicationResult();
        res.setFinalEnergy(applicationEnergy);
        res.setScheduleLength(applicationScheduleLength);
        excelApplicationResults.add(res);
        return excelApplicationResults;
    }

    private static List<ExcelTaskResult> getTaskResult(Map<Integer, Task> taskMap) {
        ArrayList<ExcelTaskResult> res = new ArrayList<>();

        for (Map.Entry<Integer, Task> entry : taskMap.entrySet()) {
            Task task = entry.getValue();
            ExcelTaskResult item = new ExcelTaskResult();
            item.setTaskId(task.getId());
            item.setEFT(task.getEFT().toString());
            item.setTaskEnergyConstraint(task.getEnergyConstraint().toString());
            item.setTaskEnergy(task.getFinalEnergy().toString());
            item.setTaskFrequency(task.getFrequency().toString());
            item.setTaskExecuteNode(task.getExecuteNode().getId());

            res.add(item);
        }
        return res;
    }
}
