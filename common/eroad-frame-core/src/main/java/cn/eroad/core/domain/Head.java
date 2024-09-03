package cn.eroad.core.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "状态信息和请求标识信息封装类", description = "状态信息和请求标识信息")
public class Head {

    /**
     * 业务响应状态
     */
    @ApiModelProperty(value = "业务响应状态", example = "S")
    private String status;

    /**
     * 业务响应码
     */
    @ApiModelProperty(value = "业务响应码", example = "000000")
    private String code;

    @ApiModelProperty(value = "业务响应信息", example = "异常")
    private String msg;

    public Head() {
        this.status = "S";
        this.code = "000000";
        this.msg = "success";
    }

    public Head(String status, String code, String msg) {
        this.status = status;
        this.code = code;
        this.msg = msg;
    }

    public boolean isOk() {
        if ("000000".equals(this.code)) {
            return true;
        }
        return false;
    }

}
