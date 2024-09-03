package cn.eroad.rad.vo;

import cn.eroad.rad.model.Network;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


/**
 * @author WuHang
 * @version 1.0
 * @description:
 * @date 2022/6/13 15:37
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SnNetList {

    @ApiModelProperty(value = "序列号，唯一标识")
    private String sn;

    @ApiModelProperty(value = "网络参数结构")
    private Network net;


}

