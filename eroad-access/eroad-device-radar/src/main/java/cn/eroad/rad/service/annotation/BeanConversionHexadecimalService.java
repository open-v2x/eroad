package cn.eroad.rad.service.annotation;

import org.springframework.stereotype.Component;

/**
 * @author mrChen
 * @date 2022/4/11 16:38
 */
@Component
public class BeanConversionHexadecimalService {
    /**
     * LONG转十六进制
     *
     * @param i
     * @return
     */
    public static String longConversionHexadecimal(Long i) {
        return Long.toHexString(i);
    }

    /**
     * 整数转十六进制
     *
     * @param i
     * @return
     */
    public static String intConversionHexadecimal(Integer i) {
        if (i == null) {
            return "";
        }
        return Integer.toHexString(i);
    }

    /**
     * 直接相加
     *
     * @param i
     * @return
     */
    public static String unchanged(String i) {
        return i;
    }

    public String protocolVersion(String str) {
        String[] split = str.split("\\.");
        String[] vs = split[0].split("V");
        String s1 = Integer.toHexString(Integer.valueOf(vs[1]));
        String s2 = Integer.toHexString(Integer.valueOf(split[1]));
        if (s2.length() == 1) {
            s2 = "0" + s2;
        }
        return s1 + s2;
    }







}
