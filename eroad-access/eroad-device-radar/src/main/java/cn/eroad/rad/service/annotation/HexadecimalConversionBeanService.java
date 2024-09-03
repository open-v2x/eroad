package cn.eroad.rad.service.annotation;


import cn.eroad.rad.constant.ObjectIdentificationEnum;
import cn.eroad.rad.constant.OperationTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @author mrChen
 * @date 2022/4/14 10:24
 */
@Component
public class HexadecimalConversionBeanService {
    /**
     * Hex 转 Long
     *
     * @param str
     * @return
     */
    public static Long hexToLong(String str) {
        return Long.parseLong(str, 16);
    }

    /**
     * Hex 转 Integer
     *
     * @param str
     * @return
     */
    public static String operateType(String str) {
        return OperationTypeEnum.getType(str);
    }

    public static String objectIdentification(String str) {
        return ObjectIdentificationEnum.getType(str);
    }

    /**
     * Hex 转 Integer
     *
     * @param str
     * @return
     */
    public static Integer hexToInt(String str) {
        return Integer.parseInt(str, 16);
    }


    public static String protocolVersion(String str) {
        String s1 = str.substring(0, 2);
        String s2 = str.substring(2, 4);
        Integer int_1 = Integer.parseInt(s1, 16);
        Integer int_2 = Integer.parseInt(s2, 16);
        return "V" + int_1.toString() + "." + int_2.toString();
    }

    /**
     * hex 转 ipv6
     *
     * @param str
     * @return
     */
    public static String noChange(String str) {
        return str;
    }


    public static void main(String[] args) {
        String number = "000123000456000";
        String regex = "(^0*)|(0*$)";
        System.out.println(number.replaceAll(regex, ""));
    }
}
