package cn.eroad.core.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class AutoRegisterDTO {
    public static final String DEVICE_COMMON_DATA_REPORT_EXCHANGE = "deviceDataReportExchange";
    //public static final String DEVICE_AC_ALARMINFO = "device_ac_alarmInfo";
    public static final String ROUTING_KEY_DEVICE_SINGLE_ALARMINFO = "device.single.autoRegisterInfo";
    //设备编码
    String deviceId;
    //设备类型
    String deviceType;
    String deviceName;
    //制造商
    String manufacturer;
    String deviceIp;
    String port;
    //区划
    String deviceArea;
    //MAC
    String mac;
    /**
     * 设备用户名
     */
    private String deviceAccount;

    /**
     * 设备密码
     */
    private String devicePassWord;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    private static AutoRegisterDTO build(String deviceId, String deviceType, String manufacturer) {
        return AutoRegisterDTO.builder().deviceId(deviceId).deviceName(deviceId + "/" + deviceType)
                .deviceType(deviceType)
                .manufacturer(manufacturer)
                .build();
    }

    private static AutoRegisterDTO build(String deviceId, String deviceType, String manufacturer, String ip, String port) {
        return AutoRegisterDTO.builder().deviceId(deviceId).deviceName(deviceId + "/" + deviceType)
                .deviceType(deviceType)
                .manufacturer(manufacturer)
                .deviceIp(ip).port(port)
                .build();
    }

    public static AutoRegisterDTO buildAutoRegister(String sn, String deviceType, String manufacturer) {
        return build(sn, deviceType, manufacturer);
    }

    public static AutoRegisterDTO buildMecAutoRegister(String sn, String manufacturer) {
        return build(sn, "mec", manufacturer);
    }

    public static AutoRegisterDTO buildLampAutoRegister(String sn, String manufacturer) {
        return build(sn, "lamp", manufacturer);
    }

    public static AutoRegisterDTO buildRadAutoRegister(String sn, String manufacturer, String ip, String port) {
        return build(sn, "rad", manufacturer, ip, port);
    }
}
