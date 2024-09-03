package cn.eroad.device.entity.deviceConfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeviceMaintainVo {
    @ApiModelProperty(value = "设备id")
    private String deviceId;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;
}
