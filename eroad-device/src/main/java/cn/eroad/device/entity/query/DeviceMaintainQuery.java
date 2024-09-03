package cn.eroad.device.entity.query;

import cn.eroad.core.domain.QueryDomain;
import cn.eroad.core.exception.AcException;
import cn.eroad.core.utils.StringUtil;
import cn.eroad.core.utils.ValueUtil;
import cn.eroad.device.entity.enums.DeviceMainEnum;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "DeviceMaintainQuery", description = "DeviceMaintainQuery")
@Slf4j
public class DeviceMaintainQuery extends QueryDomain<DeviceMaintain> {

    @ApiModelProperty(value = "deviceId")
    private String deviceId;

    @ApiModelProperty(value = "deviceName")
    private String deviceName;

    @ApiModelProperty(value = "deviceType")
    private String deviceType;

    @ApiModelProperty(value = "manufacturer")
    private String manufacturer;

    @ApiModelProperty(name = "onlineState", value = "设备上线状态 0：在线，1：离线）", position = 3, example = "1")
    private Integer onlineState;

    @ApiModelProperty(name = "alarmState", value = "告警状态 0:未告警 1:告警 ", position = 4, example = "1")
    private Integer alarmState;

    @ApiModelProperty(value = "网络状态(0:无网络  1：有网络）")
    private Integer networkLink;

    @ApiModelProperty(value = "deviceIp")
    private String deviceIp;

    /**
     * 0离线1在线   0不告警1告警
     * 正常设备normal：在线未告警—>0   online=1 alarm=0
     * 告警设备alarm：在线告警->1      online=1 alarm=1
     * 离线设备offline：离线->2       online=0
     */
    @ApiModelProperty(value = "normal(正常):0 alarm(告警):1 offline(离线):2")
    private List<Integer> state;

    @Override
    public QueryWrapper<DeviceMaintain> buildWrapper() {
        QueryWrapper<DeviceMaintain> queryWrapper = new QueryWrapper<>();

        queryWrapper.like(StringUtil.isNotBlank(deviceId), "device_id", ValueUtil.replaceAllSpecial(this.getDeviceId()));
        queryWrapper.like(StringUtil.isNotBlank(deviceName), "device_name", ValueUtil.replaceAllSpecial(this.getDeviceName()));
        queryWrapper.like(StringUtil.isNotBlank(deviceIp), "device_ip", ValueUtil.replaceAllSpecial(this.getDeviceIp()));
        queryWrapper.eq(StringUtil.isNotBlank(deviceType), "device_type", ValueUtil.replaceAllSpecial(this.getDeviceType()));
        queryWrapper.eq(StringUtil.isNotBlank(manufacturer), "manufacturer", ValueUtil.replaceAllSpecial(this.getManufacturer()));
        queryWrapper.eq(StringUtil.isNotNull(onlineState), "online_state", this.getOnlineState());
        queryWrapper.eq(StringUtil.isNotNull(alarmState), "alarm_state", this.getAlarmState());
        queryWrapper.eq(StringUtil.isNotNull(networkLink), "network_link", this.getNetworkLink());
        if (this.getState() != null) {
            for (int i = 0; i < this.getState().size(); i++) {
                if (i > 0) {
                    queryWrapper.or();
                }
                switch (this.getState().get(i)) {
                    case 0:
                        queryWrapper.eq("online_state", "1").eq("alarm_state", "0");
                        break;
                    case 1:
                        queryWrapper.eq("online_state", "1").eq("alarm_state", "1");
                        break;
                    case 2:
                        queryWrapper.ne("online_state", "0");
                        break;
                    default:
                        log.error("DeviceMaintainQuery|buildWrapper|设备分组查询中输入状态有误");
                        throw new AcException(DeviceMainEnum.STATE_ALREADY.getCode(), DeviceMainEnum.STATE_ALREADY.getMsg());
                }
            }
        }
        return queryWrapper;
    }
}
