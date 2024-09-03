package cn.eroad.core.constants;

/**
 * 返回错误码枚举
 *
 * @date 2021/5/21 11:10
 */
public enum ResCodeEnum {

    /**
     * 不存在的stream id
     */
    STREAM_ID_NOT_EXISTS("CC040031", "streamId不存在"),

    /**
     * 播放速度只支持1,2,4,8
     */
    SPEED_NOT_AVAILABLE("CC040032", "播放速度只支持1,2,4,8"),

    /**
     * 设备回放失败
     */
    DEVICE_PLAY_ERROR("CC040033", "设备播放失败"),

    /**
     * 负载均衡时提供的key为null
     */
    BALANCER_KEY_NOT_EXISTS("CC040034", "负载均衡时提供的key为null"),

    /**
     * 媒体服务不可用
     */
    ZLM_SERVER_NOT_FOUND("CC040035", "媒体服务不可用"),

    /**
     * 比对任务开关切换超时
     */
    COMPARE_TASK_SWITCH_TIMEOUT("CC040040", "比对任务开关切换超时"),

    /**
     * 比对任务开关切换失败
     */
    COMPARE_TASK_SWITCH_FAIL("CC040041", "比对任务开关切换失败");

    /**
     * 错误信息
     */
    private String resMsg;

    /**
     * 错误码
     */
    private String resCode;


    ResCodeEnum(String resCode, String resMsg) {
        this.resCode = resCode;
        this.resMsg = resMsg;
    }

    public String getResCode() {
        return resCode;
    }

    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }
}
