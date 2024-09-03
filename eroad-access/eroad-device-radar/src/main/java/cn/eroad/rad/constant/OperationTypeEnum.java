package cn.eroad.rad.constant;

/**
 * 操作类型的枚举类
 *
 * @author mrChen
 * @date 2022/7/7 15:59
 */
public enum OperationTypeEnum {

    QUERY("80", "查询请求"),

    SET("81", "设置请求"),

    REPORT("82", "主动上传"),

    QUERY_ANSWER("83", "查询应答"),

    SET_ANSWER("84", "设置应答"),

    REPORT_ANSWER("85", "主动上传应答"),

    ERROR_ANSWER("86", "出错应答"),

    MANAGEMENT_REQUEST("87", "维护管理发送"),

    MANAGEMENT_ANSWER("88", "维护管理请求");

    private String code;

    private String type;

    OperationTypeEnum(String code, String type) {
        this.code = code;
        this.type = type;
    }

    public static String getType(String codeValue) {
        OperationTypeEnum[] approvalProcessEnums = values();
        for (OperationTypeEnum approvalProcessEnum : approvalProcessEnums) {
            if (approvalProcessEnum.code().equals(codeValue)) {
                return approvalProcessEnum.type();
            }
        }
        return null;
    }

    public String code() {
        return this.code;
    }

    public String type() {
        return this.type;
    }
}
