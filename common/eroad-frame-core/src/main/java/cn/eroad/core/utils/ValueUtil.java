package cn.eroad.core.utils;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @create 2021-03-24 12:26
 */

public class ValueUtil {
    /**
     * 判断空白
     *
     * @param cs
     * @return
     */
    public static boolean isBlank(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isBlank(cs);
    }

    /**
     * 判断非空白
     *
     * @param cs
     * @return
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 判断空
     *
     * @param cs
     * @return
     */
    public static boolean isEmpty(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isEmpty(cs);
    }

    /**
     * 判断非空
     *
     * @param cs
     * @return
     */
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * 首字母大写
     *
     * @param str
     * @return
     */
    public static String capitalize(final String str) {
        return org.apache.commons.lang3.StringUtils.capitalize(str);
    }

    /**
     * 首字母小写
     *
     * @param str
     * @return
     */
    public static String unCapitalize(final String str) {
        return org.apache.commons.lang3.StringUtils.uncapitalize(str);
    }

    /**
     * 驼峰转换为下划线
     *
     * @param camelCaseName
     * @return
     */
    public static String underscoreName(String camelCaseName) {
        StringBuilder result = new StringBuilder();
        if (camelCaseName != null && camelCaseName.length() > 0) {
            result.append(camelCaseName.substring(0, 1).toLowerCase());
            for (int i = 1; i < camelCaseName.length(); i++) {
                char ch = camelCaseName.charAt(i);
                if (Character.isUpperCase(ch)) {
                    result.append("_");
                    result.append(Character.toLowerCase(ch));
                } else {
                    result.append(ch);
                }
            }
        }
        return result.toString();
    }

    /**
     * 下划线转换为驼峰
     *
     * @param underscoreName
     * @return
     */
    public static String camelCaseName(String underscoreName) {
        StringBuilder result = new StringBuilder();
        if (underscoreName != null && underscoreName.length() > 0) {
            boolean flag = false;
            for (int i = 0; i < underscoreName.length(); i++) {
                char ch = underscoreName.charAt(i);
                if ("_".charAt(0) == ch) {
                    flag = true;
                } else {
                    if (flag) {
                        result.append(Character.toUpperCase(ch));
                        flag = false;
                    } else {
                        result.append(ch);
                    }
                }
            }
        }
        return result.toString();
    }

    public static String prefixTransform(String prefix, String str, Integer offset) {
        str = prefix + str.substring(offset);
        return str;
    }

    public static String replaceAllSpecial(String str) {
        return str == null ? null : str.replaceAll("_", "\\\\_").replaceAll("%", "\\\\%");
    }

    public static boolean isCNENNumUnderscore(String word) {
        boolean sign = true;
        for (int i = 0; i < word.length(); i++) {
            if (!isENChar(word.charAt(i)) &&
                    !isCNChar(word.charAt(i)) &&
                    !Character.isDigit(word.charAt(i))
                    && !isUnderscoreChar(word.charAt(i))
            ) {
                sign = false;
            }
        }
        return sign;
    }

    public static boolean isENChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    public static boolean isCNChar(char c) {
        return (c >= 0x4e00) && (c <= 0x9fbb);
    }

    public static boolean isUnderscoreChar(char c) {
        return c == '_';
    }

    public static boolean isCNENNum(String word) {
        boolean sign = true;
        for (int i = 0; i < word.length(); i++) {
            if (!isENChar(word.charAt(i)) &&
                    !isCNChar(word.charAt(i)) &&
                    !Character.isDigit(word.charAt(i))) {
                sign = false;
            }
        }
        return sign;
    }

    public static List<String> getCascadeList(Map<String, List<String>> dataMap, String key, List<String> resultList) {
        resultList.add(key);
        if (dataMap.containsKey(key)) {
            List<String> childList = dataMap.get(key);
            for (String k : childList) {
                resultList.addAll(getCascadeList(dataMap, k, new ArrayList<>()));
            }
        }
        return resultList;
    }

    public static <T> List<T> getCascadeListObject(Map<String, List<T>> dataMap, T obj, Function<T, String> func, List<T> resultList) {
        resultList.add(obj);
        String key = func.apply(obj);
        if (dataMap.containsKey(key)) {
            List<T> childList = dataMap.get(key);
            for (T v : childList) {
                resultList.addAll(getCascadeListObject(dataMap, v, func, new ArrayList<>()));
            }
        }
        return resultList;
    }

    /**
     * 提取心跳检测接口地址
     *
     * @param contextPath
     * @return
     */
    public static String heartbeatUrlPathFormat(String contextPath) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(contextPath)) {
            return "/heartbeat";
        }
        String path = contextPath.replaceAll("\\\\", "/");
        String[] arr = path.split("/");
        if (arr == null || arr.length == 0) {
            return "/heartbeat";
        }
        List<String> arrList = Arrays.asList(arr).stream().filter(str -> org.apache.commons.lang3.StringUtils.isNotEmpty(str)).collect(Collectors.toList());
        String newUrlPath = StringUtils.collectionToDelimitedString(arrList, "", "/", "");
        return newUrlPath + "/heartbeat";
    }

    public static void main(String[] args) {
        System.out.println(heartbeatUrlPathFormat("\\//"));
    }
}
