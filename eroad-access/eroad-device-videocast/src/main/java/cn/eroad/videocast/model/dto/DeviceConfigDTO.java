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
 * @ClassName: DeviceConfigDTO
 * @author: liyongqiang
 * @creat: 2022/7/13 11:15
 * 描述: 设备配置单条存储结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("设备配置上报数据结构")
public class DeviceConfigDTO {
    public static final String DEVICE_COMMON_DATA_REPORT_EXCHANGE = "deviceDataReportExchange";
    public static final String DEVICE_AC_CONFIGINFO = "device_ac_configInfo";
    public static final String ROUTING_KEY_DEVICE_SINGLE_CONFIGINFO = "device.single.configInfo";

    @ApiModelProperty("设备编码")
    private String sn;

    @ApiModelProperty("设备类型")
    private String deviceType;

    @ApiModelProperty("配置类型")
    private String configType;

    @ApiModelProperty("设备配置变更时间")
    private Date updateTime;

    @ApiModelProperty("实际的状态信息，json格式")
    private String dataJson;

    private static DeviceConfigDTO build(String sn, String deviceType, String configType, Object data) {
        return DeviceConfigDTO.builder().sn(sn).
                deviceType(deviceType)
                .configType(configType)
                .updateTime(new Date())
                .dataJson(JSONObject.toJSONString(data)).build();
    }

    public static DeviceConfigDTO buildRsuPhysicalReport(String sn, Object data) {
        return build(sn, "rsu", "physical", data);
    }

    public static DeviceConfigDTO buildLampPhysicalReport(String sn, Object data) {
        return build(sn, "lamp", "physical", data);
    }

    public static DeviceConfigDTO buildRsuBizReport(String sn, Object data) {
        return build(sn, "rsu", "biz", data);
    }
}
