package cn.eroad.core.domain;

import cn.eroad.core.utils.StringUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author yujinfu
 * @version 1.0
 * @create 2021/11/2
 * @description 通用的请求
 */
@Data
@NoArgsConstructor
public class DeviceRequestBase {

    @ApiModelProperty("设备管理平台方-流水号")
    private Integer id;
    @ApiModelProperty("消息流水号 取值范围: 1-2147483647")
    private Integer reqId /*= CommonUtils.nextReqId()*/;
    @JsonIgnore
    private String msgType;

    @ApiModelProperty("灯控器序列号，唯一标识")
    @NotEmpty
    private List<String> snList;

    @JsonIgnore
    private String op;


    @JSONField(serialize = false, deserialize = false)
    @JsonIgnore
    public String getMsgKey() {
        return StringUtil.join("", "-", msgType, snList, reqId);
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public String getReMsgKey() {
        return StringUtil.join("re", "-", msgType, snList, reqId);
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public String getReMsgKeyNoReqId() {
        return StringUtil.join("re", "-", msgType, snList);
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public static String getMsgKey(String msgType, String sn, int reqId) {
        return StringUtil.join("", "-", msgType, sn, reqId);
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public static String getMsgKey(String msgType, String sn, int reqId, int id) {
        return StringUtil.join("", "-", msgType, sn, reqId, id);
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public static String getReMsgKey(String msgType, String sn, int reqId) {
        return StringUtil.join("re", "-", msgType, sn, reqId);
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public static String getReMsgKeyNoReqId(String msgType, String sn) {
        return StringUtil.join("re", "-", msgType, sn);
    }

    /**
     * 组装获取策略时的回应消息key
     *
     * @param msgType 消息类型
     * @param sn      sn码
     * @param reqId   通信消息id
     * @param id      策略id
     * @return
     */
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public static String getReMsgKey(String msgType, String sn, int reqId, int id) {
        return StringUtil.join("re", "-", msgType, sn, reqId, id);
    }

    /**
     * 从组装的msgKey、reMsgKey中拆解出reqId
     *
     * @param msgKey
     * @return
     */
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public static int getReqIdFromMsgKey(String msgKey) {
        String[] split = msgKey.split("-");
        return Integer.parseInt(split[3]);
    }
}
