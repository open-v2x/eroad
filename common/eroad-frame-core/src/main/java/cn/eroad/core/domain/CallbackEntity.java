package cn.eroad.core.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity基类
 *
 * @author liyq
 */
@ApiModel(value = "接口回调实体", description = "封装接口回调相关参数")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallbackEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 回调事务ID
     */
    @ApiModelProperty("回调事务ID")
    private String id;

    /**
     * 回调地址
     */
    @ApiModelProperty("回调地址")
    private String callbackUrl;

    /**
     * 请求方式
     */
    @ApiModelProperty("请求方式")
    private String method;

    /**
     * 令牌
     */
    @ApiModelProperty("令牌")
    private String token;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

}
