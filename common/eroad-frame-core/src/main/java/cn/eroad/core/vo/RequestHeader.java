package cn.eroad.core.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设备在线状态的实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel
public class RequestHeader implements Serializable {
    @ApiModelProperty("终端信息")
    private String userAgent;
    @ApiModelProperty("语言")
    private String acceptLanguage;
    @ApiModelProperty("请求类型")
    private String contentType;
    @ApiModelProperty("源")
    private String origin;
}
