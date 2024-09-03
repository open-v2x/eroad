package cn.eroad.device.entity.devicemaintain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@TableName("device_maintain")
public class ExcelUploadEntity implements Serializable {

    /**
     * 设备编码(必填)
     */
    @ExcelProperty("设备编码(必填)")
    private String deviceId;

    /**
     * 设备名称（必填）
     */
    @ExcelProperty("设备名称(必填)")
    private String deviceName;

    /**
     * 设备类型（必填）
     */
    @ExcelProperty("设备类型(必填)")
    private String deviceType;

    /**
     * 厂商
     */
    @ExcelProperty("厂商(必填)")
    private String manufacturer;

    /**
     * 经纬度、高程
     */
    @ExcelProperty("经度")
    private String longitude;
    @ExcelProperty("纬度")
    private String latitude;
    @ExcelProperty("高程")
    private String altitude;

    /**
     * 设备账户
     */
    @ExcelProperty("设备账户")
    private String deviceAccount;

    /**
     * 设备密码
     */
    @ExcelProperty("设备密码")
    private String devicePassWord;

    /**
     * 网络类型
     */
    @ExcelProperty("网络类型")
    private String networkType;

    /**
     * 父设备编码
     */
    @ExcelProperty("父设备编码")
    private String parentDeviceId;

    /**
     * 设备IP
     */
    @ExcelProperty("设备IP")
    private String deviceIp;


    @ExcelProperty("服务IP或域名")
    private String domainName;

    /**
     * 属地
     */
    @ExcelProperty("属地")
    private String dependency;

    /**
     * 设备区划（12位以内）
     */
    @ExcelProperty("设备区划")
    private String deviceArea;

    @ExcelProperty("端口号")
    private String port;

    /**
     * 版本
     */
    @ExcelProperty("版本")
    private String version;


    /**
     * 网络状态
     */
    @ExcelProperty("网络状态")
    private Integer networkLink;

    @ExcelProperty("备注")
    private String comment;

    @ExcelProperty("设备MAC")
    private String mac;














}
