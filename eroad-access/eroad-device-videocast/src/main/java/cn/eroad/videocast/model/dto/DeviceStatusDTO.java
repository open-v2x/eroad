package cn.eroad.videocast.model.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @project: ecai-any-any-mqtt-videocast
 * @ClassName: DeviceStatusDTO
 * @author: liyongqiang
 * @creat: 2022/7/13 11:15
 * 描述: 设备状态单条存储结构，临时放置在灯控工程，后续迁移
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("设备状态上报数据结构")
public class DeviceStatusDTO {
    public static final String DEVICE_COMMON_DATA_REPORT_EXCHANGE = "deviceDataReportExchange";
    public static final String DEVICE_AC_STATUSINFO = "device_ac_statusInfo";
    public static final String ROUTING_KEY_DEVICE_SINGLE_STATUSINFO = "device.single.statusInfo";
    @ApiModelProperty("设备编码")
    private String sn;

    @ApiModelProperty("设备类型")
    private String deviceType;

    @ApiModelProperty("状态类型")
    private String statusType;

    @ApiModelProperty("设备状态变更时间")
    private Date updateTime;

    @ApiModelProperty("实际的状态信息，json格式")
    private String dataJson;

    private static DeviceStatusDTO build(String sn, String deviceType, String statusType, Object data) {
        return DeviceStatusDTO.builder().sn(sn).
                deviceType(deviceType)
                .statusType(statusType)
                .updateTime(new Date())
                .dataJson(JSONObject.toJSONString(data)).build();
    }

    public static DeviceStatusDTO buildLampCycleReport(String sn, Object data) {
        return build(sn, "lamp", "cycle", data);
    }

    public static DeviceStatusDTO buildLampChangeReport(String sn, Object data) {
        return build(sn, "lamp", "change", data);
    }

    public static DeviceStatusDTO buildRsuPhysicalReport(String sn, Object data) {
        return build(sn, "rsu", "physical", data);
    }

    public static DeviceStatusDTO buildRsuBizReport(String sn, Object data) {
        return build(sn, "rsu", "biz", data);
    }

    public static DeviceStatusDTO buildRadPhysicalReport(String sn, Object data) {
        return build(sn, "rad", "physical", data);
    }

    public static DeviceStatusDTO buildLidPhysicalReport(String sn, Object data) {
        return build(sn, "lid", "physical", data);
    }

    public static DeviceStatusDTO buildMecPhysicalReport(String sn, Object data) {
        return build(sn, "mec", "physical", data);
    }
}
