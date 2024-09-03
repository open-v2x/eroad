package cn.eroad.core.domain;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;



@Data
public class CommonResult {
    //入参的流水号
    private String id;
    private String queryResp;
    //    private List successQueryList = new ArrayList();
    private List successList = new ArrayList();
    private List failList = new ArrayList();
    private long timestamp;

    public CommonResult() {
        timestamp = System.currentTimeMillis();
    }

    @NoArgsConstructor
    @Data
    public static class FailItem {
        private String sn;
        private String errMsg;
        //        @ApiModelProperty("错误代码")
        private Integer errCode;

        private Integer chnNo;
        private Integer alarmId;
    }































    @JSONField(serialize = false, deserialize = false)
    public boolean isSuccess() {
        return failList == null || failList.size() == 0;
    }

    public <T> ResponseData<T> mapToResponseData() {
        ResponseData responseData = new ResponseData();
        responseData.setResultCode(isSuccess() ? 0 : -1);
        responseData.setResultMsg(isSuccess() ? "成功" : "失败");
        responseData.setResultData(this);
        return responseData;
    }
}
