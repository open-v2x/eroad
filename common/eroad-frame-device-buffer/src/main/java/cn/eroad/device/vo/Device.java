package cn.eroad.device.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * 平台(设备)缓存对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Device {
    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 设备所属服务
     */
    private String domainName;

    /**
     * 设备IP
     */
    private String deviceIp;

    /**
     * 设备父ID
     */
    private String parentDeviceId;

    /**
     * 设备端口
     */
    private String port;

    /**
     * 设备厂商
     */
    private String manufacturer;

    /**
     * 设备区号
     */
    private String deviceArea;

    /**
     * 设备标志，如果设备为自动注册，则为0
     */
    private Integer deviceSign;

    /**
     * 标志操作状态  0：删除
     *
     * @return
     */
    private Integer operateSign;

    /**
     * 在线状态（0：离线  1：在线  ）
     */
    private Integer onlineState;

    /**
     * 告警状态(0:不告警 1:告警 )
     */
    private Integer alarmState;

    /**
     * 设备真实sn，用于下发和回复使用
     */
    private String realSn;

    /**
     * 网络状态(0:无网络 1：有网络)
     */
    private Integer networkLink;

    public String getDomain() {
        if (!StringUtils.isEmpty(this.domainName)) {
            return this.domainName;
        } else {
            return this.deviceType;
        }
    }

    public String getDeviceArea() {
        if (StringUtils.isEmpty(deviceArea)) {
            return "130632";
        }
        return deviceArea;
    }

    /**
     * 设备mac地址
     */
    private String mac;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 设备账户
     */
    private String deviceAccount;

    /**
     * 设备密码
     */
    private String devicePassWord;

    /**
     * 租户名
     */
    private String tenantCode;
}
