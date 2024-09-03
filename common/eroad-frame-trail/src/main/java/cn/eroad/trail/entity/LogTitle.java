package cn.eroad.trail.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 描述
 * @Title: LogTitle
 * @ProjectName onecity-cc-vs-api
 * @Date 2021/4/8 11:03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "日志菜单", description = "日志菜单")
public class LogTitle {
    @ApiModelProperty(value = "菜单值")
    private String value;
    @ApiModelProperty(value = "菜单描述")
    private String label;
}
