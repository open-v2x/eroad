package cn.eroad.rad.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ConfData {
    @ApiModelProperty(value = "轨迹信息，上传轨迹信息频率，单位：0.1Hz，当取值为0时，关闭轨迹数据上传")
    private Integer traceFre;
    @ApiModelProperty(value = "过车信息，是否上传检测断面过车信息，0：不上传，1：上传")
    private Integer passFre;
    @ApiModelProperty(value = "交通状态信息，上传交通状态实时信息频率，单位：0.1Hz，当取值为0时，关闭交通状态信息")
    private Integer trafficStatusFre;
    @ApiModelProperty(value = "交通流信息，交通流信息统计周期时长，单位：秒，当取值为0时，关闭交通流信息，最小周期1s，最大周期3600s")
    private Integer flowFre;
    @ApiModelProperty(value = "异常交通事件信息,是否上传异常交通事件信息  0：不上传，1：上传")
    private Integer eventUp;

}
