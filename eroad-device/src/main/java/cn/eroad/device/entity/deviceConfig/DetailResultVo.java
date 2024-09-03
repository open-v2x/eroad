package cn.eroad.device.entity.deviceConfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DetailResultVo {
    @ApiModelProperty(value = "设备id")
    private String deviceId;

    @ApiModelProperty(value = "配置详情")
    private String configJson;
}
