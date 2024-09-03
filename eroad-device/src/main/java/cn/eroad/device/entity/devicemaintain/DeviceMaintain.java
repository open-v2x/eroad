package cn.eroad.device.entity.devicemaintain;


import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("device_maintain")
public class DeviceMaintain implements Serializable {

    /**
     * 设备编码(必填)
     */
    @ExcelProperty("设备编码(必填)")
    @ApiModelProperty("设备编码")
    @TableId(value = "device_id")
    private String deviceId;

    /**
     * 设备名称（必填）
     */
    @ExcelProperty("设备名称(必填)")
    @TableField(value = "device_name")
    @ApiModelProperty("设备名称")
    private String deviceName;

    /**
     * 设备类型（必填）
     */
    @ExcelProperty("设备类型(必填)")
    @TableField(value = "device_type")
    @ApiModelProperty("设备类型")
    private String deviceType;

    /**
     * 厂商（必填）
     */
    @ExcelProperty("厂商(必填)")
    @TableField(value = "manufacturer")
    @ApiModelProperty("厂商")
    private String manufacturer;

    /**
     * 经纬度、高程
     */
    @ExcelProperty("经度")
    @TableField(value = "longitude")
    @ApiModelProperty("经度")
    private String longitude;

    @ExcelProperty("纬度")
    @TableField(value = "latitude")
    @ApiModelProperty("纬度")
    private String latitude;

    @ExcelProperty("高程")
    @TableField(value = "altitude")
    @ApiModelProperty("高程")
    private String altitude;

    /**
     * 设备账户
     */
    @ExcelProperty("设备账户")
    @TableField(value = "device_account")
    @ApiModelProperty("设备账户")
    private String deviceAccount;

    /**
     * 设备密码
     */
    @ExcelProperty("设备密码")
    @TableField(value = "device_passWord")
    @ApiModelProperty("设备密码")
    private String devicePassWord;


    /**
     * 网络类型
     */
    @ExcelProperty("网络类型")
    @TableField(value = "network_type")
    @ApiModelProperty("网络类型")
    private String networkType;

    /**
     * 父设备编码
     */
    @ExcelProperty("父设备编码")
    @TableField(value = "parent_device_id")
    @ApiModelProperty("父设备编码")
    private String parentDeviceId;


    /**
     * 设备ip
     */
    @ExcelProperty("设备IP")
    @TableField(value = "device_ip")
    @ApiModelProperty("设备IP")
    private String deviceIp;

    @ExcelProperty("服务IP或域名")
    @TableField(value = "domain_name")
    @ApiModelProperty("服务IP或域名")
    private String domainName;


    /**
     * 属地
     */
    @ExcelProperty("属地")
    @TableField(value = "dependency")
    @ApiModelProperty("属地")
    private String dependency;

    /**
     * 设备区划（12位以内数字）
     */
    @ExcelProperty("设备区划")
    @TableField(value = "device_area")
    @ApiModelProperty("设备区划（12位以内数字）")
    private String deviceArea;

    @ExcelProperty("端口号")
    @TableField(value = "port")
    @ApiModelProperty("端口号")
    private String port;

    /**
     * 版本
     */
    @ExcelProperty("版本")
    @TableField(value = "version")
    @ApiModelProperty("版本")
    private String version;


    /**
     * 在线状态（0：离线  1：在线  ）
     */
    @ExcelProperty("在线状态(0:离线,1:在线)")
    @TableField(value = "online_state")
    @ApiModelProperty("在线状态（0：离线  1：在线  ）")
    private Integer onlineState;

    /**
     * 告警状态(0:不告警 1:告警 )
     */
    @ExcelProperty("告警状态(0:未告警,1:告警)")
    @TableField(value = "alarm_state")
    @ApiModelProperty(" 告警状态(0:不告警 1:告警 )")
    private Integer alarmState;

    /**
     * 网络状态
     */
    @ExcelProperty("网络状态(0:无网络,1:有网络)")
    @TableField(value = "network_link")
    @ApiModelProperty("网络状态(0:无网络 1：有网络)")
    private Integer networkLink;


    /**
     * 备注
     */
    @ExcelProperty("备注")
    @TableField(value = "comment")
    @ApiModelProperty("备注")
    private String comment;

    /**
     * 在线状态更新时间
     */
    @ExcelProperty("在线状态更新时间")
    @TableField(value = "online_state_update_time")
    @ApiModelProperty("在线状态更新时间")
    @DateTimeFormat(style = "yyyy-MM-dd HH:mm:ss")
    private Date onlineStateUpdateTime;

    /**
     * 告警状态更新时间
     */
    @ExcelProperty("告警状态更新时间")
    @TableField(value = "alarm_state_update_time")
    @ApiModelProperty("在线状态更新时间")
    @DateTimeFormat(style = "yyyy-MM-dd HH:mm:ss")
    private Date alarmStateUpdateTime;


    /**
     * 设备标志，如果设备为自动注册，则为0
     */
    @TableField(value = "device_sign")
    @ApiModelProperty("设备标志，如果设备为自动注册，则为0")
    private Integer deviceSign;


    /**
     * 设备mac地址
     */
    @TableField(value = "mac")
    @ApiModelProperty("设备mac地址")
    private String mac;


}
