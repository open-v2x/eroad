package cn.eroad.device.entity.deviceConfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
* @Description: 获取设备配置返回值（超维用）
* @Param:
* @return:
* @Author: nbr
* @Date: 2022/8/1
*/
@Data
public class ConfigSuperResultVo {
    @ApiModelProperty(value = "设备sn")
    private String sn;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "配置类型")
    private String configType;

    @ApiModelProperty(value = "配置详情")
    private String dataJson;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
