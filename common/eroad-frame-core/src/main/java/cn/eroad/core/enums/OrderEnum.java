package cn.eroad.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 排序枚举
 *
 * @create 2021-03-22 13:46
 */

@Getter
@AllArgsConstructor
public enum OrderEnum {
    ASC("ascend", "升序"),
    DESC("descend", "降序");

    private String value;
    private String desc;

    public static OrderEnum getEnum(String value) {
        for (OrderEnum orderEnum : OrderEnum.values()) {
            if (value.equals(orderEnum.getValue())) {
                return orderEnum;
            }
        }
        return OrderEnum.ASC;
    }
}
