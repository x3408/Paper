package entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import lombok.Data;
import org.apache.poi.ss.usermodel.FillPatternType;

@Data
@HeadStyle(fillPatternType = FillPatternType.NO_FILL)
@HeadFontStyle(fontHeightInPoints = 11)
@ColumnWidth(15)
@HeadRowHeight(20)
public class ExcelResult {
    @ExcelProperty("任务序号")
    private int taskId;
    @ExcelProperty("能耗约束")
    private String taskEnergyConstraint;
    @ExcelProperty("执行频率")
    private String taskFrequency;
    @ExcelProperty("执行节点")
    private int taskExecuteNode;
    @ExcelProperty("任务能耗")
    private String taskEnergy;
    @ExcelProperty("EFT(ni)")
    private String EFT;
    @ExcelProperty("E(G)")
    private String finalEnergy;
    @ExcelProperty("SL(G)")
    private String scheduleLength;
}
