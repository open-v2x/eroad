package cn.eroad.rad.model.response;

/**
 * @author yujinfu
 * @version 1.0
 * @create 2021/11/26
 * @description
 */
public enum ResultType {

    success(0, "执行成功"),
    //需要和设备通信主动发消息给设备，但是设备没有回应
    connectTimeout(-1, "连接超时"),
    //    signatureError(-2,"签名错误"),
    //针对规范校验参数
    paramsError(-3, "参数有误"),
    //ecai执行出错
    executeException(-4, "执行异常"),
    //ekong传过来的path不存在
    apiNotFound(-5, "找不到API"),

    netInfoError(-6, "网元信息查询失败"),





    ;

    private int ResultCode;
    private String ResultMsg;

    ResultType(int code, String msg) {
        this.ResultCode = code;
        this.ResultMsg = msg;
    }

    public int getResultCode() {
        return ResultCode;
    }

    public String getResultMsg() {
        return ResultMsg;
    }
}
