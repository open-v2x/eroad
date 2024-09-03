package cn.eroad.device.entity.devicerunninginfo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author WuHang
 * @description:
 * @date 2022/7/21 17:59
 */
@Data
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class QueryDeviceRunningInfoVo {

    private String sn;

    private String deviceType;

    private String statusType;
}
