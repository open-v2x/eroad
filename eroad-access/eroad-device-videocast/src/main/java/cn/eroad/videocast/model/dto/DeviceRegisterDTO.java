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
 * @ClassName: DeviceRegisterDTO
 * @author: liyongqiang
 * @creat: 2022/7/13 11:15
 * 描述: 设备注册单条存储结构，用于发送给其他对接平台（非平台内部自动注册消息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("设备注册上报数据结构")
public class DeviceRegisterDTO {

    @ApiModelProperty("设备编码")
    private String sn;

    @ApiModelProperty("设备类型")
    private String deviceType;

    @ApiModelProperty("设备状告警上报时间")
    private Date updateTime;

    @ApiModelProperty("实际的告警信息，json格式")
    private String dataJson;

    private static DeviceRegisterDTO build(String sn, String deviceType, Object data) {
        return DeviceRegisterDTO.builder().sn(sn).
                deviceType(deviceType)
                .updateTime(new Date())
                .dataJson(JSONObject.toJSONString(data)).build();
    }

    public static DeviceRegisterDTO buildLampRegisterReport(String sn, Object data) {
        return build(sn, "lamp", data);
    }

    public static DeviceRegisterDTO buildRsuRegisterReport(String sn, Object data) {
        return build(sn, "rsu", data);
    }

    public static DeviceRegisterDTO buildRadRegisterReport(String sn, Object data) {
        return build(sn, "rad", data);
    }

    public static DeviceRegisterDTO buildLidRegisterReport(String sn, Object data) {
        return build(sn, "lid", data);
    }

    public static DeviceRegisterDTO buildCamRegisterReport(String sn, Object data) {
        return build(sn, "cam", data);
    }
}
