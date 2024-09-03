package cn.eroad.rad.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 设备工作状态
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WorkingStatus {
    /**
     * 电压状态
     */
    @ApiModelProperty(value = "电压状态")
    private String voltage;
    /**
     * 温度状态
     */
    @ApiModelProperty(value = "温度状态")
    private String temperature;
    /**
     * 湿度状态
     */
    @ApiModelProperty(value = "湿度状态")
    private String humidity;
    /**
     * 设备状态
     */
    @ApiModelProperty(value = "设备状态")
    private Integer status;
}
