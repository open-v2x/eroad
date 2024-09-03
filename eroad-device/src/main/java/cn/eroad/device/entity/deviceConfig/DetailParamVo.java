package cn.eroad.device.entity.deviceConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
@ApiModel(description = "设备配置详情参数")
public class DetailParamVo {
    @ApiModelProperty(value = "设备id",required = true)
    @NotBlank
    private String deviceId;

    @ApiModelProperty(value = "配置类型",required = true)
    @NotBlank
    private String configType;
}
