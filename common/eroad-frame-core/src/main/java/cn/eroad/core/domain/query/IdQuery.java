package cn.eroad.core.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @Description: ID查询封装类
 * @Title: IdQuery
 * @ProjectName onecity-cc-vp-message
 * @Date 2021/4/20 14:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ID查询封装类", description = "ID查询封装类")
public class IdQuery {
    @ApiModelProperty(value = "ID", example = "1")
    @NotNull(message = "ID不能为空")
    private Long id;
}
