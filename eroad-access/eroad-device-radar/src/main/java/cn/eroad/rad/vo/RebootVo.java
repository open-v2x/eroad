package cn.eroad.rad.vo;

import cn.eroad.core.domain.CallbackEntity;
import cn.eroad.core.domain.DeviceRequestBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author WuHang
 * @version 1.0
 * @description:
 * @date 2022/6/13 10:02
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RebootVo extends DeviceRequestBase {

    //    @ApiModelProperty(value = "设备序列号数组")

    @ApiModelProperty(value = "操作间隔时间,单位毫秒")
    private Integer intervalTime;
    private CallbackEntity callback;
}
