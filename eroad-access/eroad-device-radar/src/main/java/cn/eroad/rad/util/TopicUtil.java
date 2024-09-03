package cn.eroad.rad.util;

public class TopicUtil {

    // caikong-设备类型-设备编码-业务数据类型
    private static final String topicTemplate = "caikong-%s-%s-%s";

    private static final String bizTemplate = "kafka_radar_%s";

    public enum DeviceEnum {

        RAD("毫米波雷达", "rad"),
        LID("激光雷达", "lid"),
        RSU("路侧单元", "rsu");

        private String name;
        private String code;

        DeviceEnum(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public enum ServiceDataEnum {

        // 毫米波雷达
        RAD_POINT("点云数据", "point"),
        RAD_TRACK("车轨迹数据", "track"),
        RAD_CROSS("过车信息", "cross"),
        RAD_STATUS("交通状态信息", "status"),
        RAD_FLOW("交通流信息", "flow"),
        RAD_EVENT("异常事件信息", "event"),
        // 激光雷达
        LID_POINT("点云数据", "point"),
        // rsu
        RSU_REGISTER("设备注册", "register"),
        RSU_INFO("设备信息上报", "info"),
        RSU_OPERATION("运维配置信息上报", "operation"),
        RSU_SERVICE("业务配置信息上报", "service"),
        RSU_DEVICE_STATE("设备运行状态上报", "deviceState"),
        RSU_SERVICE_STATE("业务运行状态上报", "serviceState"),
        RSU_ALARM("实时告警信息", "alarm"),
        RSU_LOG("操作日志信息", "log"),
        RSU_HEARTBEAT("心跳", "heartbeat"),
        RSU_LAST_WILL("遗言", "lastWill");

        private String name;
        private String code;

        ServiceDataEnum(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static String getTopic(DeviceEnum device, String sn, ServiceDataEnum serviceData) {
        return String.format(topicTemplate, device.code, sn, serviceData.code);
    }

    public static String getBizTopic(ServiceDataEnum serviceData) {
        return String.format(bizTemplate, serviceData.code);
    }
}
