package cn.eroad.core.constants;

/**
 * @ClassName: MessageConstants
 * @Description: 校验消息常量
 * @Date: 2021/1/22 15:02
 */
public class ValidateConstants {
    // 设备编码不能为空
    public static final String DEVICE_ID_REQUIRED = "设备编码不能为空";
    // 设备SN不能为空
    public static final String SN_REQUIRED = "设备SN不能为空";
    // 设备仅支持40位数字
    public static final String DEVICE_ID_INVALID = "编码仅支持40位数字";
    // 设备编码必须为空
    public static final String DEVICE_ID_MUST_NULL = "设备编码必须为空";
    //设备编码不能超过40位
    public static final String DEVICE_ID_INVALID_MAX = "设备编码不能超过40位";
    //设备SN不能超过40位
    public static final String SN_INVALID_MAX = "设备SN不能超过40位";
    // 设备名称不能为空
    public static final String DEVICE_NAME_REQUIRED = "设备名称不能为空";
    // 设备名称不能超过40位
    public static final String DEVICE_NAME_INVALID = "设备名称不能超过40位";
    // 设备密码不能为空
    public static final String DEVICE_PASSWD_REQUIRED = "设备密码不能为空";
    // 设备类型不能为空
    public static final String DEVICE_TYPE_REQUIRED = "设备类型不能为空";
    // 设备类型只能为1或2
    public static final String DEVICE_TYPE_INVALID = "设备类型只能为1或2";
    // 设备厂家不能为空
    public static final String DEVICE_MANUFACTURER_REQUIRED = "设备厂家不能为空";
    // 设备密码不能超过12位
    public static final String DEVICE_PASSWD_INVALID = "设备密码不能超过12位";
    // 设备备注不能超过200位
    public static final String DEVICE_REMARK_INVALID = "设备备注不能超过200位";
    // 平台编码不能为空
    public static final String SUB_DEVICE_ID_REQUIRED = "平台编码不能为空";
    // 序号不能为空
    public static final String ID_REQUIRED = "序号不能为空";
    // 经度方向不正确，请重新选择
    public static final String GPS_LG_INVALID = "经度方向不正确，请重新选择";
    // 纬度方向不正确，请重新选择
    public static final String GPS_LA_INVALID = "纬度方向不正确，请重新选择";
    // 经度数值不正确，仅支持小数点后6位浮点数
    public static final String GPS_LG_VALUE_INVALID = "经度数值不正确，仅支持小数点后6位浮点数";
    // 纬度数值不正确，仅支持小数点后6位浮点数
    public static final String GPS_LA_VALUE_INVALID = "纬度数值不正确，仅支持小数点后6位浮点数";
    // 高度数值不正确，仅支持数字或小数
    public static final String GPS_AL_VALUE_INVALID = "高度数值不正确，仅支持数字或小数, 不支持负数";
    // 设备编码不能为空
    public static final String DEVICE_IDS_REQUIRED = "设备编码不能为空";
    // 编码仅支持20位数字
    public static final String DEVICE_IDS_INVALID = "编码仅支持20位数字";
    // 不支持的录制类型
    public static final String RECORD_TYPE_INVALID = "不支持的录制类型";
    // 不支持的云存储配置状态
    public static final String CLOUD_CONFIG_STATUS_INVALID = "不支持的云存储配置状态";
    // 不支持的集合格式
    public static final String IDS_INVALID = "ID集合格式错误";
    // 不支持的上下架状态
    public static final String STATE_STATUS_INVALID = "不支持的上下架状态";
    // 预置位数组
    public static final String PRESETS_INVALID = "预置位只能输入1-255之间的整数数组，数字之间用英文逗号分隔。";
    // 设备编码仅支持40位数字
    public static final String DEVICE_PARENT_INVALID = "父设备编码仅支持40位数字";
    // 设备区划仅支持12位数字
    public static final String DEVICE_AREA_INVALID = "设备区划仅支持12位数字";
    //端口号范围为0-65535范围内数字
    public static final String PORT_INVALID = "端口号范围为0-65535范围内数字";

}
