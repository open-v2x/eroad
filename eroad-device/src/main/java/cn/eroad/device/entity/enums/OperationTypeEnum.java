package cn.eroad.device.entity.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举类（本体模型、厂商模型接口使用）
 */
@Getter
@AllArgsConstructor
public enum OperationTypeEnum {

    INTERFACE_INPUT_PARAMETER("interfaceInputParameter","接口-入参"),

    INTERFACE_OUTPUT_PARAMETER("interfaceOutputParameter","接口-出参"),

    COLLECT_HANDLE("collectHandle","采集-处理"),

    COLLECT_REPORT("collectReport","采集-上报"),

    COLLECT_TRANSMIT("collectTransmit","采集-转发");

    private String code;
    private String name;

    public static OperationTypeEnum getEnum(String code) {
        for (OperationTypeEnum operationTypeEnum : OperationTypeEnum.values()) {
            if (code.equals(operationTypeEnum.getCode())) {
                return operationTypeEnum;
            }
        }
        return null;
    }
}
