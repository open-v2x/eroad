package cn.eroad.device.entity.devicemaintain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeviceState {

    @ApiModelProperty("设备总量")
    private int  sum;

    @ApiModelProperty("正常状态设备")
    private int normal;

    @ApiModelProperty("离线状态设备")
    private int offLine;

    @ApiModelProperty("告警状态设备")
    private int  alarm;
}
