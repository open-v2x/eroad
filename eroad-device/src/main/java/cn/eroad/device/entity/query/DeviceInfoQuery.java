package cn.eroad.device.entity.query;

import cn.eroad.device.entity.devicemaintain.ExcelUploadEntity;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DeviceInfoQuery extends ExcelUploadEntity {

    /**
     * 当前页码
     */
    @ExcelIgnore
    private Integer pageNum;
    /**
     * 页长
     */
    @ExcelIgnore
    private Integer pageSize;
}
