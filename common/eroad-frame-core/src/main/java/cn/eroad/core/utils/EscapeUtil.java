package cn.eroad.core.utils;

import org.springframework.util.StringUtils;

public class EscapeUtil {

    public static String escapeStr(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        StringBuffer temp = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '%' || str.charAt(i) == '_') {
                temp.append("\\").append(str.charAt(i));
            } else {
                temp.append(str.charAt(i));
            }
        }
        return temp.toString();
    }

}
