package cn.eroad.core.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;

@Data
@ApiModel("通用返回结构")
public class CommonContent<T> {
    public static final String SUCCESS_CODE = "000000";
    public static final String FAIL_CODE = "CC000500";
    public static final String BUSY_CODE = "CC000900";

    public static CommonContent ok() {
        CommonContent r = new CommonContent();
        Head head = new Head("S", SUCCESS_CODE, "success");
        r.setHead(head);
        return r;
    }

    public static <T> CommonContent<T> ok(T body) {
        CommonContent<T> r = ok();
        r.setBody(body);
        return r;
    }

    public static CommonContent error() {
        CommonContent r = new CommonContent();
        Head head = new Head("F", FAIL_CODE, "fail");
        r.setHead(head);
        return r;
    }

    public static CommonContent error(String msg) {
        CommonContent r = new CommonContent();
        Head head = new Head("F", FAIL_CODE, msg);
        r.setHead(head);
        return r;
    }


    public static CommonContent error(String status, String code, String msg) {
        CommonContent r = new CommonContent();
        Head head = new Head(status, code, msg);
        r.setHead(head);
        return r;
    }

    public static CommonContent error(Head h) {
        CommonContent r = new CommonContent();
        r.setHead(h);
        return r;
    }

    public static CommonContent buzy() {
        return error("F", BUSY_CODE, "系统繁忙, 请稍后再试");
    }

    @ApiModelProperty(value = "异步请求唯一标识", position = 1)
    private String id;

    @ApiModelProperty(value = "头信息", position = 2)
    private Head head;

    @ApiModelProperty(value = "数据体", position = 3)
    @Valid
    private T body;

}
