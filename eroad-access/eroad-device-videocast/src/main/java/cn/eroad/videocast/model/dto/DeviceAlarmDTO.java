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
 * @ClassName: DeviceAlarmDTO
 * @author: liyongqiang
 * @creat: 2022/7/13 11:15
 * 描述: 设备告警单条存储结构，临时放置在灯控工程，后续迁移
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("设备状态上报数据结构")
public class DeviceAlarmDTO {

    public static final String DEVICE_COMMON_DATA_REPORT_EXCHANGE = "deviceDataReportExchange";
    public static final String DEVICE_AC_ALARMINFO = "device_ac_alarmInfo";
    public static final String ROUTING_KEY_DEVICE_SINGLE_ALARMINFO = "device.single.alarmInfo";

    @ApiModelProperty("设备编码")
    private String sn;

    @ApiModelProperty("设备类型")
    private String deviceType;

    @ApiModelProperty("告警状态：0:报警消失，1:告警开始")
    private Integer alarmStatus;

    @ApiModelProperty("报警类型：0:设备报警；1:业务报警")
    private Integer alarmType;

    @ApiModelProperty("设备状告警上报时间")
    private Date updateTime;

    @ApiModelProperty("设备状告警发生时间")
    private Date alarmTime;

    @ApiModelProperty("告警级别")
    private Integer alarmLevel;

    @ApiModelProperty("告警信息，简短中文说明")
    private String alarmMessage;

    @ApiModelProperty("实际的告警信息，json格式")
    private String dataJson;

    private static DeviceAlarmDTO build(String sn, String deviceType, Object data) {
        return DeviceAlarmDTO.builder().sn(sn).
                deviceType(deviceType)
                .updateTime(new Date())
                .dataJson(JSONObject.toJSONString(data)).build();
    }

    public static DeviceAlarmDTO buildLampAlarmReport(String sn, Object data) {
        return build(sn, "lamp", data);
    }

    public static DeviceAlarmDTO buildRsuAlarmReport(String sn, Object data) {
        return build(sn, "rsu", data);
    }

    public static DeviceAlarmDTO buildRadAlarmReport(String sn, Object data) {
        return build(sn, "rad", data);
    }

    public static DeviceAlarmDTO buildLidAlarmReport(String sn, Object data) {
        return build(sn, "lid", data);
    }

    public static DeviceAlarmDTO buildOnuAlarmReport(String sn, Object data) {
        return build(sn, "onu", data);
    }

    public static DeviceAlarmDTO buildLteAlarmReport(String sn, String deviceType, Object data) {
        return build(sn, deviceType, data);
    }

}
