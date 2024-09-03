package cn.eroad.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @project: ecai-any-any-mqtt-lamp
 * @ClassName: DeviceStatusDTO
 * @author: liyongqiang
 * @creat: 2022/7/13 11:15
 * 描述: 设备状态单条存储结构，临时放置在灯控工程，后续迁移
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("设备在线状态的实体类")
public class DeviceOnlineDTO {

    public static final String DEVICE_COMMON_DATA_REPORT_EXCHANGE = "deviceDataReportExchange";
    public static final String DEVICE_ONLINE_STATUS_REPORT = "device_ac_onlineStatus";
    public static final String ROUTING_KEY_DEVICE_ONLINE_STATUS = "device.single.onlineStatus";

    @ApiModelProperty("设备序列号")
    private String sn; // 设备序列号

    @ApiModelProperty("设备类型")
    private String deviceType; // 设备类型

    @ApiModelProperty("设备在线状态，1是在线，0是不在线")
    private Integer online; // 1是在线，0是不在线

    @ApiModelProperty("设备在线状态变更时间")
    private Date updateTime; // 在线状态变更时间

    @ApiModelProperty("设备在线离线原因")
    private String reason;

    private static DeviceOnlineDTO build(String sn, String deviceType, Integer online) {
        return DeviceOnlineDTO.builder().sn(sn).
                deviceType(deviceType)
                .online(online)
                .updateTime(new Date()).build();
    }

    private static DeviceOnlineDTO build(String sn, String deviceType, Integer online, String reason) {
        return DeviceOnlineDTO.builder().sn(sn).
                deviceType(deviceType)
                .online(online)
                .reason(reason)
                .updateTime(new Date()).build();
    }

    public static DeviceOnlineDTO buildLampOnlineReport(String sn, Integer online) {
        return build(sn, "lamp", online);
    }

    public static DeviceOnlineDTO buildMecOnlineReport(String sn, Integer online) {
        return build(sn, "mec", online);
    }
}
