package cn.eroad.core.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 设备在线状态的实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel
public class OrderRetryVO implements Serializable {
    @ApiModelProperty("设备序列号")

    private List<String> snList;
    @ApiModelProperty("请求路径")
    private String requestUrl;
    @ApiModelProperty("请求方法")
    private String methodName;
    @ApiModelProperty("请求方法")
    private String methodType;
    @ApiModelProperty("请求参数列表")
    private Object[] args;
    @ApiModelProperty("请求header")
    private RequestHeader header;
    @ApiModelProperty("类名")
    private String className; // 设备序列号
}
