package cn.eroad.core.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DeviceBaseInfo {

    //设备编码
    String deviceId;
    //设备类型
    String deviceType;
    //设备名称
    String deviceName;
    //制造商
    String manufacturer;
    //ip
    String deviceIp;
    //端口
    String port;
    //经度
    String longitude;
    //纬度
    String latitude;
    //在线状态
    Integer online;

    private static DeviceBaseInfo build(String deviceIp, String port, String deviceId, String deviceType,
                                        String manufacturer, String longitude, String latitude) {
        return DeviceBaseInfo.builder()
                .deviceIp(deviceIp)
                .port(port)
                .deviceId(deviceId)
                .deviceName(deviceId + "/" + deviceType)
                .deviceType(deviceType)
                .manufacturer(manufacturer)
                .longitude(longitude)
                .latitude(latitude)
                .online(1)
                .build();
    }
}
