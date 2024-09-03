package cn.eroad.device.entity.enums;

/**
 * 异常提示枚举
 */
public enum DeviceMainEnum {

    ORACLE_INTER_FAIL("100000","数据库交互失败"),
    DEVICE_ID_NOT_ONLY("100001","设备编码不唯一"),
    DEVICE_NAME_NOT_ONLY("100002","设备名称不唯一"),
    DEVICE_NOT_EXIST("100003","设备信息不存在"),
    ID_NOT_ALLOW_UPDATE("100004","id不允许修改"),
    EXCEL_NOT_ALLOW_UPDATE("100005","EXCEL插入失败，设备编码/名称字段有重复行或者数据库中已存在"),
    EXCEL_FAIL("100006","其他excel插入失败情况"),
    REGISTER_ALREADY("100007","设备编码已存在，设备已注册"),
    STATE_ALREADY("100008","输入state状态错误"),
    ORACLE_SELECT_FAIL("100009","数据库查询失败,请检查数据是否正确");



    private String code;
    private String msg;

    DeviceMainEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {

        final StringBuffer sb = new StringBuffer("ErrorCodeEnum{");
        sb.append("code='").append(code).append('\'');
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');

        return sb.toString();
    }
}
