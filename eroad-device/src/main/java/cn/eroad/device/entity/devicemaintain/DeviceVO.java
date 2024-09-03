package cn.eroad.device.entity.devicemaintain;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceVO extends DeviceMaintain implements Serializable {

    @ApiModelProperty("设备网络当天不可达次数")
    private Long unreachableTimesToday;


}
