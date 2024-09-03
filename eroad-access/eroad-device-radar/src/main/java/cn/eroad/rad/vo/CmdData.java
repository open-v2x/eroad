package cn.eroad.rad.vo;

import cn.eroad.rad.model.ConfData;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author WuHang
 * @version 1.0
 * @description:
 * @date 2022/6/13 15:35
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CmdData {

    @ApiModelProperty(value = "配置参数结构")
    private List<SnNetList> snNetList;

    @ApiModelProperty(value = "序列号，唯一标识")
    private List<String> snList;
    @ApiModelProperty(value = "工作状态结构")
    private ConfData confData;

}
