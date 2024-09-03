package cn.eroad.core.vo;

import cn.eroad.core.domain.CallbackEntity;
import cn.eroad.core.utils.StringUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;


/**
 * @Author elvin
 * @Date Created by elvin on 2018/9/19.
 * @Description : buukle 公共请求规范
 */
@Data
@NoArgsConstructor
public class DeviceCommonRequest<T> {

    @ApiModelProperty("设备管理平台方-流水号")
    private String id;

    @JsonIgnore
    @ApiModelProperty("消息流水号 取值范围: 1-2147483647")
    private String reqId /*= CommonUtils.nextReqId()*/;
    @JsonIgnore
    @ApiModelProperty("消息类型")
    private String msgType;

    @ApiModelProperty("设备序列号，唯一标识")
    @NotEmpty
    private List<String> snList;
    @JsonIgnore
    @ApiModelProperty("操作类型，R表示读取操作。支持W, R,D三种操作，分别代表写、读、删除")
    private String op;

    @ApiModelProperty("间隔时间")
    private Integer intervalTime;

    @ApiModelProperty("表示每个操作间隔时间，批量操作的个数")
    private Integer batchCount = 10;

    @ApiModelProperty("接口回调参数实体类")
    private CallbackEntity callback;

    /**
     * 请求体
     */
    @ApiModelProperty("接口请求命令参数")
    private T cmdProps;

















    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public static String getMsgKey(String msgType, String sn, String reqId) {
        return StringUtil.join("", "-", msgType, sn, reqId);
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public static String getMsgKey(String msgType, String sn, String reqId, int id) {
        return StringUtil.join("", "-", msgType, sn, reqId, id);
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public static String getReMsgKey(String msgType, String sn, String reqId) {
        return StringUtil.join("re", "-", msgType, sn, reqId);
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public static String getReMsgKeyNoReqId(String msgType, String sn) {
        return StringUtil.join("re", "-", msgType, sn);
    }

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public String getReMsgKeyNoReqId(String sn) {
        return StringUtil.join("re", "-", msgType, sn, reqId);
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
    public static String getReMsgKey(String msgType, String sn, String reqId, int id) {
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

    public T getCmdProps() {
        return cmdProps;
    }

    public void setCmdProps(T cmdProps) {
        this.cmdProps = cmdProps;
    }
}


