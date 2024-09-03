package cn.eroad.core.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: ID查询封装类
 * @Title: IdQuery
 * @ProjectName onecity-cc-vp-message
 * @Date 2021/4/20 14:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "IDs查询封装类", description = "IDs查询封装类")
public class IdsQuery {
    @ApiModelProperty(value = "ID集合, 多个用逗号拼接", example = "1,2")
    private String ids;
}
