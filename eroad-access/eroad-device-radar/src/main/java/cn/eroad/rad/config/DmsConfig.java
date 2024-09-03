package cn.eroad.rad.config;

public class DmsConfig {

    // 设备工作状态上报
    public static final String DMS_TOPIC_WORK_STATUS = "device.single.statusInfo";
    public static final String DMS_EXCHANGE_WORK_STATUS = "deviceDataReportExchange";
    // 自动注册信息上报
    public static final String ROUTING_KEY_AUTO_REGISTER = "device.single.autoRegisterInfo";
    public static final String EXCHANGE_AUTO_REGISTER = "deviceDataReportExchange";

    // 注册信息上报（超维需要）
    public static final String ROUTING_KEY_REGISTER = "device.single.registerInfo";
    public static final String EXCHANGE_REGISTER = "deviceDataReportExchange";
    /**
     * RabbitMQ 遗言
     */
    public static final String EXCHANGE_LAST_WORDS = "deviceDataReportExchange";
    public static final String ROUTING_KEY_LAST_WORDS = "device.single.onlineStatus";
    /**
     * RabbitMQ 基础信息上报
     */
    public static final String EXCHANGE_BASEINFO = "deviceDataReportExchange";
    public static final String ROUTING_BASEINFO = "device.single.modifyInfo";
    //配置上报
    public static final String EXCHANGE_CONFIG_INFO = "deviceDataReportExchange";
    public static final String ROUTING_CONFIG_INFO = "device.single.configInfo";

}
