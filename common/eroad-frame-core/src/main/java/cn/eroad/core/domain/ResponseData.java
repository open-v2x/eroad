package cn.eroad.core.domain;


import cn.eroad.core.enums.ResultType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;



/**
 * @author yujinfu
 * @version 1.0
 * @create 2021/11/11
 * @description
 */
@Data
@ToString

@AllArgsConstructor
@NoArgsConstructor
public class ResponseData<T> {

    private int resultCode;
    private String resultMsg;
    private T resultData;

    public static <T> ResponseData<T> success() {
        ResponseData<T> responseData = new ResponseData<T>();
        responseData.resultCode = 0;
        responseData.resultMsg = "success";
        return responseData;
    }

    public static <T> ResponseData<T> success(T data) {
        ResponseData<T> responseData = new ResponseData<T>();
        responseData.resultCode = 0;
        responseData.resultMsg = "成功";
        responseData.resultData = data;
        return responseData;
    }

    public static <T> ResponseData<T> fail(ResultType resultType) {
        ResponseData<T> responseData = new ResponseData<T>();
        responseData.resultCode = resultType.getResultCode();
        responseData.resultMsg = resultType.getResultMsg();
        return responseData;
    }

    public static <T> ResponseData<T> fail(int code, String message) {
        ResponseData<T> responseData = new ResponseData<T>();
        responseData.resultCode = code;
        responseData.resultMsg = message;
        return responseData;
    }

    public static <T> ResponseData<T> fail(T data) {
        ResponseData<T> responseData = new ResponseData<T>();
        responseData.resultCode = -1;
        responseData.resultMsg = "失败";
        responseData.setResultData(data);
        return responseData;
    }

    public static <T> ResponseData<T> with(int code, String messsage, T data) {
        ResponseData<T> responseData = new ResponseData<T>();
        responseData.setResultCode(code);
        responseData.setResultMsg(messsage);
        responseData.setResultData(data);
        return responseData;
    }

    public static <T> ResponseData<T> failToSend(T data) {
        ResponseData<T> responseData = new ResponseData<T>();
        responseData.setResultCode(ResultType.SEND_FAIL.getResultCode());
        responseData.setResultMsg(ResultType.SEND_FAIL.getResultMsg());
        responseData.setResultData(data);
        return responseData;
    }
}
