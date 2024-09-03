package cn.eroad.device.entity.form;


import cn.eroad.core.constants.RegexConstants;
import cn.eroad.core.constants.ValidateConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;


@Data
@ApiModel(value = "Device对象表单", description = "设备对象表单")
public class DeviceForm {
    @ApiModel(value = "设备新增Form", description = "设备新增Form")
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class AddForm {
        @NotBlank(message = ValidateConstants.DEVICE_ID_REQUIRED)
        @Pattern(regexp = RegexConstants.DEVICE_ID_INVALID_MAX, message = ValidateConstants.DEVICE_ID_INVALID_MAX)
        @ApiModelProperty(value = "设备编码", required = true, position = 1, example = "34020000001110400001")
        private String deviceId;

        @NotBlank(message = ValidateConstants.DEVICE_NAME_REQUIRED)
        @Pattern(regexp = RegexConstants.DEVICE_NAME_INVALID, message = ValidateConstants.DEVICE_NAME_INVALID)
        @ApiModelProperty(name = "deviceName", value = "设备名称", required = true, position = 2, example = "NVR")
        private String deviceName;

        @NotNull(message = ValidateConstants.DEVICE_TYPE_REQUIRED)
        //   @Pattern(regexp = RegexConstants.DEVICE_TYPE_INVALID, message = ValidateConstants.DEVICE_TYPE_INVALID)
        @ApiModelProperty(name = "deviceType", value = "设备类型", required = true, position = 4, example = "1")
        private String deviceType;

        @NotNull(message = ValidateConstants.DEVICE_MANUFACTURER_REQUIRED)
        @ApiModelProperty(value = "厂商")
        private String manufacturer;

        @Pattern(regexp = RegexConstants.GPS_LG_VALUE_INVALID, message = ValidateConstants.GPS_LG_VALUE_INVALID)
        @ApiModelProperty(name = "longitude", value = "经度", position = 5, example = "432.123456")
        private String longitude;

        @Pattern(regexp = RegexConstants.GPS_LA_VALUE_INVALID, message = ValidateConstants.GPS_LA_VALUE_INVALID)
        @ApiModelProperty(name = "latitude", value = "纬度", position = 6, example = "5414.123456")
        private String latitude;

        @Pattern(regexp = RegexConstants.GPS_AL_VALUE_INVALID, message = ValidateConstants.GPS_AL_VALUE_INVALID)
        @ApiModelProperty(name = "altitude", value = "高度", position = 7, example = "8544")
        private String altitude;

        @ApiModelProperty(name = "passWord", value = "设备接入密码", required = true, position = 3, example = "12345")
        private String passWord;

        //个人补充
        @ApiModelProperty(value = "设备账户")
        private String deviceAccount;

        @ApiModelProperty(value = "设备密码")
        private String devicePassWord;

        @ApiModelProperty(value = "网络类型")
        private String networkType;

        @Pattern(regexp = RegexConstants.DEVICE_PARENT_INVALID, message = ValidateConstants.DEVICE_PARENT_INVALID)
        @ApiModelProperty(value = "父设备编码")
        private String parentDeviceId;

        @ApiModelProperty(value = "设备ip")
        private String deviceIp;

        @ApiModelProperty(value = "属地")
        private String dependency;

        @ApiModelProperty(value = "版本")
        private String version;

        /**
         * 在线状态（0：离线 1：在线  ）
         */
        @ApiModelProperty(value = "在线状态")
        private Integer onlineState;

        /**
         * 告警状态(0:正常 1:告警 )
         */
        @ApiModelProperty(value = "告警信息")
        private Integer alarmState;

        @ApiModelProperty(value = "备注")
        private String comment;

        @ApiModelProperty(value = "服务ip或域名")
        private String domainName;

        @Pattern(regexp = RegexConstants.DEVICE_AREA_INVALID, message = ValidateConstants.DEVICE_AREA_INVALID)
        @ApiModelProperty(value = "设备区划")
        private String deviceArea;

        @Pattern(regexp = RegexConstants.PORT_INVALID, message = ValidateConstants.PORT_INVALID)
        @ApiModelProperty(value = "端口号")
        private String port;

        @ApiModelProperty(value = "网络状态(0:无网络  1：有网络）")
        private Integer networkLink;

    }

    @ApiModel(value = "设备修改Form", description = "设备修改Form")
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class EditForm {

        //
        @NotBlank(message = ValidateConstants.DEVICE_ID_REQUIRED)
        @Pattern(regexp = RegexConstants.DEVICE_ID_INVALID_MAX, message = ValidateConstants.DEVICE_ID_INVALID_MAX)
        @ApiModelProperty(value = "设备编码", required = true, position = 1, example = "34020000001110400001")
        private String deviceId;

        @Pattern(regexp = RegexConstants.DEVICE_ID_INVALID_MAX, message = ValidateConstants.DEVICE_ID_INVALID_MAX)
        @ApiModelProperty(value = "设备编码", required = true, position = 1, example = "34020000001110400001")
        private String deviceIdNew;

        @NotBlank(message = ValidateConstants.DEVICE_NAME_REQUIRED)
        @Pattern(regexp = RegexConstants.DEVICE_NAME_INVALID, message = ValidateConstants.DEVICE_NAME_INVALID)
        @ApiModelProperty(name = "deviceName", value = "设备名称", required = true, position = 2, example = "NVR")
        private String deviceName;

        // @NotBlank(message = ValidateConstants.DEVICE_PASSWD_REQUIRED)
        @Length(max = 12, message = ValidateConstants.DEVICE_PASSWD_INVALID)
        @ApiModelProperty(name = "passWord", value = "设备接入密码", required = true, position = 3, example = "12345")
        private String passWord;

        @NotNull(message = ValidateConstants.DEVICE_TYPE_REQUIRED)
        //@Pattern(regexp = RegexConstants.DEVICE_TYPE_INVALID, message = ValidateConstants.DEVICE_TYPE_INVALID)
        @ApiModelProperty(name = "deviceType", value = "设备类型:1表示 摄像头 ，2 表示NVR或第三方平台", required = true, position = 4, example = "1")
        private String deviceType;

        @Pattern(regexp = RegexConstants.GPS_LG_VALUE_INVALID, message = ValidateConstants.GPS_LG_VALUE_INVALID)
        @ApiModelProperty(name = "longitude", value = "经度", position = 5, example = "432.123456")
        private String longitude;

        @Pattern(regexp = RegexConstants.GPS_LA_VALUE_INVALID, message = ValidateConstants.GPS_LA_VALUE_INVALID)
        @ApiModelProperty(name = "latitude", value = "纬度", position = 6, example = "5414.123456")
        private String latitude;

        @Pattern(regexp = RegexConstants.GPS_AL_VALUE_INVALID, message = ValidateConstants.GPS_AL_VALUE_INVALID)
        @ApiModelProperty(name = "altitude", value = "高度", position = 7, example = "8544")
        private String altitude;

        @NotNull(message = ValidateConstants.DEVICE_MANUFACTURER_REQUIRED)
        @ApiModelProperty(value = "厂家")
        private String manufacturer;

        @ApiModelProperty(value = "设备账户")
        private String deviceAccount;

        @ApiModelProperty(value = "设备密码")
        private String devicePassWord;

        @ApiModelProperty(value = "网络类型")
        private String networkType;

        @Pattern(regexp = RegexConstants.DEVICE_PARENT_INVALID, message = ValidateConstants.DEVICE_PARENT_INVALID)
        @ApiModelProperty(value = "父设备编码")
        private String parentDeviceId;

        @ApiModelProperty(value = "设备ip")
        private String deviceIp;

        @ApiModelProperty(value = "属地")
        private String dependency;

        @ApiModelProperty(value = "版本")
        private String version;

        /**
         * 在线状态（0：离线  1：在线  ）
         */
        @ApiModelProperty(value = "在线状态")
        private Integer onlineState;

        /**
         * 告警状态(0:正常 1:告警 )
         */
        @ApiModelProperty(value = "告警信息")
        private Integer alarmState;

        @ApiModelProperty(value = "备注")
        private String comment;

        @ApiModelProperty(value = "服务ip或域名")
        private String domainName;


        @ApiModelProperty(value = "代理流的状态，0：结束推流，1：开始推流")
        private Integer proxyStreamState;

        @Pattern(regexp = RegexConstants.DEVICE_AREA_INVALID, message = ValidateConstants.DEVICE_AREA_INVALID)
        @ApiModelProperty(value = "设备区划")
        private String deviceArea;

        @Pattern(regexp = RegexConstants.PORT_INVALID, message = ValidateConstants.PORT_INVALID)
        @ApiModelProperty(value = "端口号")
        private String port;

        @ApiModelProperty(value = "网络状态(0:无网络  1：有网络）")
        private Integer networkLink;


    }

    @ApiModel(value = "设备删除Form", description = "设备删除Form")
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class DeleteForm {

        @ApiModelProperty(value = "设备编码", required = true, position = 1, example = "34020000001110400001")
        private List<String> deviceId;
    }

    @ApiModel(value = "单个设备查询Form", description = "单个设备查询Form")
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class SelectByIdForm {
        @ApiModelProperty(value = "设备编码", required = true, position = 1)
        private String deviceId;
    }

    @ApiModel(value = "多个设备查询Form", description = "单个设备查询Form")
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class SelectByIdsForm {
        @ApiModelProperty(value = "设备编码集合", required = true, position = 1)
        private List<String> deviceId;
    }

    @ApiModel(value = "单个设备查询Form(超维)", description = "单个设备查询Form")
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class SuperSelectByIdForm {
        @ApiModelProperty(value = "设备SN", required = true, position = 1)
        private List<String> sn;
    }

    @ApiModel(value = "设备Catalog订阅模式更新Form", description = "设备Catalog订阅模式更新Form")
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CatalogSubscribe {
        @NotNull(message = ValidateConstants.DEVICE_ID_REQUIRED)
        @Pattern(regexp = RegexConstants.DEVICE_ID_INVALID, message = ValidateConstants.DEVICE_ID_INVALID)
        @ApiModelProperty(name = "deviceId", value = "编码", required = true, position = 2, example = "34020000001110400001")
        private String deviceId;

        @NotBlank(message = "订阅状态不能为空")
        @Pattern(regexp = RegexConstants.STATE_STATUS_INVALID, message = ValidateConstants.STATE_STATUS_INVALID)
        @ApiModelProperty(value = "状态", allowableValues = "0,1", required = true, position = 2, notes = "1-订阅;0-取消订阅")
        private String status;
    }

}
