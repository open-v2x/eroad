package cn.eroad.rad.vo.api;

import cn.eroad.rad.model.ConfData;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author WuHang
 * @version 1.0
 * @description:
 * @date 2022/6/13 16:41
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ConfigSetInVo {



    @ApiModelProperty(value = "工作状态结构")
    private ConfData confData;
}
