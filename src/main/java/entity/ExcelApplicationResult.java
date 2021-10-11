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
public class ExcelApplicationResult {
    @ExcelProperty("全局能耗")
    private String finalEnergy;
    @ExcelProperty("调度长度")
    private String scheduleLength;
}
