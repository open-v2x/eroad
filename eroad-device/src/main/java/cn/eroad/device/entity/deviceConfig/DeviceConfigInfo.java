package cn.eroad.device.entity.deviceConfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * @Description: 设备配置信息
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/5/18
 */
@Data
public class DeviceConfigInfo {
    @ApiModelProperty(value = "设备id")
    private String deviceId;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "配置类型")
    private String configType;

    @ApiModelProperty(value = "配置信息")
    private String configJson;

    @ApiModelProperty(value = "状态，1：可用，0：不可用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
