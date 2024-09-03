package cn.eroad.core.constants;

/**
 * @ClassName: RegexConstants
 * @Description: 正则表达式常量
 * @Date: 2021/1/22 15:13
 */
public class RegexConstants {
    // 设备编码校验。{仅支持40位数字}
    public static final String DEVICE_ID_INVALID = "^$|^\\d{40}$";

    // 设备编码校验。{最大不超过40位}
    public static final String DEVICE_ID_INVALID_MAX = "^$|^[\\S|\\s]{1,40}$";
    // 设备SN校验。{最大不超过40位}
    public static final String SN_INVALID_MAX = "^$|^[\\S|\\s]{1,40}$";

    // 父设备编码校验。{最大不超过40位}
    public static final String DEVICE_PARENT_INVALID = "^$|^[\\S|\\s]{1,40}$";

    // 设备名称校验。{名称长度不超过40位}
    public static final String DEVICE_NAME_INVALID = "^$|^[\\S|\\s]{1,40}$";

    // 端口号范围为0-65535范围内数字
    public static final String PORT_INVALID = "^\\s{0}$|^([0-9](\\d{0,3}))$|^([1-5]\\d{4})$|^(6[0-4]\\d{3})$|^(65[0-4]\\d{2})$|^(655[0-2]\\d)$|^(6553[0-5])$";
    //public static final String PORT_INVALIDS="^(\\d{1,4}|([1-5]\\d{4})|([1-6][1-4]\\d{3})|([1-6][1-4][1-4]{2})|([1-6][1-4][1-4][1-2]\\d)|([1-6][1-5][1-5][1-3][1-5]))$";

    // 经度校验。{经度方向不正确，请重新选择}
    public static final String GPS_LG_INVALID = "^$|^[EW]{1}$";
    // 纬度校验。{纬度方向不正确，请重新选择}
    public static final String GPS_LA_INVALID = "^$|^[NS]{1}$";
    // 经度数值校验，仅支持小数点后6位浮点数
    public static final String GPS_LG_VALUE_INVALID = "^$|^([0-9]+)\\.[0-9]{6}$";
    // 纬度数值校验，仅支持小数点后6位浮点数
    public static final String GPS_LA_VALUE_INVALID = "^$|^([0-9]+)\\.[0-9]{6}$";
    // 高度数值校验，仅支持数字或小数, 不支持负数
    public static final String GPS_AL_VALUE_INVALID = "^$|^[0-9]+(\\.[0-9]+)?$";
    // 设备编码校验。{20位数字}
    public static final String DEVICE_IDS_INVALID = "^$|^\\d{20}(,\\d{20})*$";

    // 设备类型校验。{1或2}
    public static final String DEVICE_TYPE_INVALID = "^(1|2){1}$";
    // 录制类型校验。{1或2或3}
    public static final String RECORD_TYPE_INVALID = "^(1|2|3){1}$";

    // {8位或10位数字}
    public static final String DEVICE_DOMAIN_INVALID = "^\\d{8}|\\d{10}$";

    // IP校验
    public static final String DEVICE_IP_INVALID = "^$|^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
    // 云存储配置状态校验
    public static final String CLOUD_CONFIG_STATUS_INVALID = "^(0|1){1}$";
    // 整形数组字符串校验
    public static final String IDS_INVALID = "^\\d+(,\\d+)*$";

    // 上下架状态校验
    public static final String STATE_STATUS_INVALID = "^(0|1){1}$";

    // 1-255之间的整形数组字符串校验
    public static final String PRESETS_INVALID = "^$|^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])(,(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9]))*$";

    // 设备区划{最大不超过12位}
    public static final String DEVICE_AREA_INVALID = "^$|^[\\S|\\s]{1,12}$";

}
