package cn.eroad.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 设备在线状态变更时发送给运维平台的数据结构
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("设备在线状态变更的实体类")
public class OnlineStateChangeDTO {
    public static final String EXCHANGE_DEVICE_DATA_REPORT = "deviceDataReportExchange";
    public static final String ROUTING_KEY_DEVICE_SINGLE_ONLINE_OFFLINE = "device.single.onlineOffline";
    public static final String QUEUE_DEVICE_SVR_ONLINE_OFFLINE = "device_svr_onlineOffline";

    @ApiModelProperty("设备序列号")
    private String sn; // 设备序列号

    @ApiModelProperty("设备类型")
    private String deviceType; // 设备类型

    @ApiModelProperty("设备在线状态，1是在线，0是不在线")
    private Integer oldState; // 1是在线，0是不在线

    @ApiModelProperty("设备在线状态，1是在线，0是不在线")
    private Integer newState; // 1是在线，0是不在线

    @ApiModelProperty("设备在线状态变更时间")
    private Date updateTime; // 在线状态变更时间

    @ApiModelProperty("设备在线离线原因")
    private String reason;
}
