package cn.eroad.core.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 简单结果，内部方法校验返回体，不建议用于接口数据传输
 * @Title: SimpleContent
 * @Date 2021/3/29 9:37
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "简单校验返回体", description = "简单校验返回体")
public class SimpleContent {
    @ApiModelProperty(value = "结果标识， true & false")
    Boolean code;
    @ApiModelProperty(value = "结果描述")
    String message;

    public static SimpleContent OK = new SimpleContent(true, "ok");

    public static SimpleContent FAIL = new SimpleContent(false, "");

    public static SimpleContent ok(String msg) {
        return new SimpleContent(true, msg);
    }

    public static SimpleContent fail(String msg) {
        return new SimpleContent(false, msg);
    }
}
