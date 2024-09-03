package cn.eroad.videocast.model.data;

import com.alibaba.fastjson.annotation.JSONField;
import cn.eroad.videocast.model.regist.DataResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: zhaohanqing
 * @Date: 2022/8/11 10:08
 * @Description:
 */
@Data
public class Response {
    //响应消息URL
    @JSONField(name = "ResponseURL")
    @JsonProperty("ResponseURL")
    private String ResponseURL;

    //状态码
    @JSONField(name = "StatusCode")
    @JsonProperty("StatusCode")
    private String StatusCode;

    //状态描述
    @JSONField(name = "ResponseString")
    @JsonProperty("ResponseString")
    private String ResponseString;

    //序列号
    @JSONField(name = "Cseq")
    @JsonProperty("Cseq")
    private Long Cseq;

    //具体的数据
    @JSONField(name = "Data")
    @JsonProperty("Data")
    private DataResult Data;
}
