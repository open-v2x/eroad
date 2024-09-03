package cn.eroad.rad.constant;

/**
 * @author mrChen
 * @date 2022/7/7 16:00
 */
public enum ObjectIdentificationEnum {


    REGISTER("0101", "注册"),

    HEART("0102", "心跳"),

    CONF("0204", "配置参数"),

    WORKER_STATUS("0205", "工作状态"),

    NET_CONF("0206", "网络参数"),

    RESET("0207", "设备恢复出厂设置"),

    REBOOT("0208", "设备重启"),

    TRAFFIC_TARGET_TRAJECTORY("0301", "交通目标轨迹"),

    HEART_SEARCH("0302", "检测断面"),

    TRAFFIC_STATUS("0303", "交通状态"),

    TRAFFIC_FLOW("0304", "交通流信息"),

    ABNORMAL_EVENT("0305", "异常事件"),

    POINT_CLOUD("0306", "点云数据");


    private String code;

    private String type;

    ObjectIdentificationEnum(String code, String type) {
        this.code = code;
        this.type = type;
    }

    public static String getType(String codeValue) {
        ObjectIdentificationEnum[] approvalProcessEnums = values();
        for (ObjectIdentificationEnum approvalProcessEnum : approvalProcessEnums) {
            if (approvalProcessEnum.code().equals(codeValue)) {
                return approvalProcessEnum.type();
            }
        }
        return null;
    }

    public String code() {
        return this.code;
    }

    public String type() {
        return this.type;
    }
}
