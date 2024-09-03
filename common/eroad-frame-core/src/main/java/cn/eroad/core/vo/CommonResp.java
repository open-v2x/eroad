package cn.eroad.core.vo;

import cn.eroad.core.enums.ResultType;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yujinfu
 * @version 1.0
 * @create 2021/11/2
 * @description 通用的响应
 */
@Slf4j
@Data
@NoArgsConstructor
@ApiModel("通用的响应")
public class CommonResp extends CommonReq {

    @ApiModelProperty("错误码，0成功，非0失败")
    private Integer err;
    @ApiModelProperty("错误原因")
    private String errInfo;


    @JSONField(serialize = false, deserialize = false)
    @JsonIgnore
    public boolean isSuccess() {
        return err != null && err == 0;
    }

    @JSONField(serialize = false, deserialize = false)
    @JsonIgnore
    public static boolean isSuccess(int err) {
        return err == 0;
    }

    public static <T> T respWith(String reqId, String sn, ResultType resultType, Class<T> clazz) {
        try {
            CommonResp resp = (CommonResp) clazz.newInstance();
            resp.setId(reqId);
            resp.setReqId(reqId);
            resp.setErr(resultType.getResultCode());
            resp.setErrInfo(resultType.getResultMsg());
            resp.setSn(sn);
            return (T) resp;
        } catch (Exception e) {
            log.error("respWith 出现exception：{}", e);
        }
        return null;
    }

}
