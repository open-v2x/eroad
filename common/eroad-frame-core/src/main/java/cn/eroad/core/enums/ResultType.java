package cn.eroad.core.enums;

public enum ResultType {

    //执行成功
    SUCCESS(0, "执行成功"),
    //需要和设备通信主动发消息给设备，但是设备没有回应
    CONNECT_TIMEOUT(-1, "连接超时"),
    //    signatureError(-2,"签名错误"),
    //针对规范校验参数
    PARAMS_ERROR(-3, "参数有误"),
    //ecai执行出错
    EXECUTE_EXCEPTION(-4, "执行异常"),
    //ekong传过来的path不存在
    API_NOT_FOUND(-5, "找不到API"),
    SEND_FAIL(-6, "发送失败"),





    ;

    private int resultCode;
    private String resultMsg;

    ResultType(int code, String msg) {
        this.resultCode = code;
        this.resultMsg = msg;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }
}
