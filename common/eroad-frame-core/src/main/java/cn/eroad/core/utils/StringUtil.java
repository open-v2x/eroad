package cn.eroad.core.utils;


import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 */
public class StringUtil {
    /**
     * 空字符串
     */
    private static final String NULLSTR = "";

    /**
     * 下划线
     */
    private static final char SEPARATOR = '_';

    /**
     * 获取参数不为空值
     *
     * @param value defaultValue 要判断的value
     * @return value 返回值
     */
    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * * 判断一个Collection是否为空， 包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Collection<?> coll) {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * * 判断一个Collection是否非空，包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * * 判断一个对象数组是否为空
     *
     * @param objects 要判断的对象数组
     *                * @return true：为空 false：非空
     */
    public static boolean isEmpty(Object[] objects) {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * * 判断一个对象数组是否非空
     *
     * @param objects 要判断的对象数组
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Object[] objects) {
        return !isEmpty(objects);
    }

    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return isNull(map) || map.isEmpty();
    }

    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * * 判断一个字符串是否为空串
     *
     * @param str String
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(String str) {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    /**
     * * 判断一个字符串是否为非空串
     *
     * @param str String
     * @return true：非空串 false：空串
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * * 判断一个对象是否为空
     *
     * @param object Object
     * @return true：为空 false：非空
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * * 判断一个对象是否非空
     *
     * @param object Object
     * @return true：非空 false：空
     */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * * 判断一个对象是否是数组类型（Java基本型别的数组）
     *
     * @param object 对象
     * @return true：是数组 false：不是数组
     */
    public static boolean isArray(Object object) {
        return isNotNull(object) && object.getClass().isArray();
    }

    /**
     * 去空格
     */
    public static String trim(String str) {
        return (str == null ? "" : str.trim());
    }

    /**
     * 截取字符串
     *
     * @param str   字符串
     * @param start 开始
     * @return 结果
     */
    public static String substring(final String str, int start) {
        if (str == null) {
            return NULLSTR;
        }

        if (start < 0) {
            start = str.length() + start;
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return NULLSTR;
        }

        return str.substring(start);
    }

    /**
     * 截取字符串
     *
     * @param str   字符串
     * @param start 开始
     * @param end   结束
     * @return 结果
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return NULLSTR;
        }

        if (end < 0) {
            end = str.length() + end;
        }
        if (start < 0) {
            start = str.length() + start;
        }

        if (end > str.length()) {
            end = str.length();
        }

        if (start > end) {
            return NULLSTR;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }


    /**
     * 下划线转驼峰命名
     */
    public static String toUnderScoreCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 前置字符是否大写
        boolean preCharIsUpperCase = true;
        // 当前字符是否大写
        boolean curreCharIsUpperCase = true;
        // 下一字符是否大写
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0) {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            } else {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1)) {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase) {
                sb.append(SEPARATOR);
            } else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase) {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * 是否包含字符串
     *
     * @param str  验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase(trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String convertToCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) {
            // 没必要转换
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线，仅将首字母大写
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        // 用下划线将原始字符串分割
        String[] camels = name.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 首字母大写
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * 驼峰式命名法 例如：user_name->userName
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    //判断一个字符串是否为数字
    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    //判断一个字符串是否为中英文和数字和下划线
    public static boolean isLetterDigitOrChineseOrUnderline(String str) {
        String regex = "^[a-z_0-9A-Z\u4e00-\u9fa5]+$";
        return str.matches(regex);
    }

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
        String newUrlPath = org.springframework.util.StringUtils.collectionToDelimitedString(arrList, "", "/", "");
        return newUrlPath + "/heartbeat";
    }

    /**
     * ids集合转化为List<Long>
     *
     * @param ids
     * @return
     */
    public static List<Long> idsFormatOfLong(String ids) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        String[] arr = ids.split(",");
        return Arrays.asList(arr).stream().mapToLong(id -> Long.parseLong(id)).boxed().collect(Collectors.toList());
    }

    /**
     * ids集合转化为List<Long>
     *
     * @param ids
     * @return
     */
    public static List<String> idsFormatOfString(String ids) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        String[] arr = ids.split(",");
        return Arrays.asList(arr);
    }

    /**
     * 将内容以分隔符进行连接
     *
     * @param prefix  前缀
     * @param gap     间隔符
     * @param objects 连接内容
     * @return
     */
    public static String join(String prefix, String gap, Object... objects) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        builder.append(gap);
        for (int i = 0; i < objects.length; i++) {
            builder.append(objects[i]);
            if (i < objects.length - 1) {
                builder.append(gap);
            }
        }
        return builder.toString();
    }

    public static String trimQuota(String value) {
        if (value == null) return null;
        String finalValue = value;
        while (finalValue.endsWith("\n") || finalValue.endsWith("\r")) {//先去除结尾多余的空白行
            finalValue = finalValue.substring(0, finalValue.length() - 1);
        }
        while (finalValue.startsWith("\\\"")) finalValue = finalValue.substring(2);
        while (finalValue.startsWith("\"")) finalValue = finalValue.substring(1);
        while (finalValue.endsWith("\\\"")) finalValue = finalValue.substring(0, finalValue.length() - 2);
        while (finalValue.endsWith("\"")) {
            finalValue = finalValue.substring(0, finalValue.length() - 1);
        }
        return finalValue;
    }

    /**
     * 编码为unicode
     *
     * @param source
     * @return
     */
    public static String encode2Unicode(String source) {
        StringBuilder sb = new StringBuilder(source.length() * 3);
        for (char c : source.toCharArray()) {
            if (c < 256) {
                sb.append(c);
            } else {
                sb.append("\\u");
                sb.append(Character.forDigit((c >>> 12) & 0xf, 16));
                sb.append(Character.forDigit((c >>> 8) & 0xf, 16));
                sb.append(Character.forDigit((c >>> 4) & 0xf, 16));
                sb.append(Character.forDigit((c) & 0xf, 16));
            }
        }
        return sb.toString();
    }

    public static String unicode2String(String unicode) {
        if (unicode == null || "".equals(unicode)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int pos = 0;
        while ((i = unicode.indexOf("\\u", pos)) != -1) {
            sb.append(unicode.substring(pos, i));
            if (i + 5 < unicode.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16));
            }
        }
        return sb.toString();
    }

    //编码
    public static String string2Unicode(String string) {
        if (string == null || "".equals(string)) {
            return null;
        }
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            unicode.append("\\u" + Integer.toHexString(c));
        }
        return unicode.toString();
    }

    /**
     * 编码为unicode
     *
     * @param source
     * @return
     */
    public static String decode2Unicode(String source) {
        StringBuilder sb = new StringBuilder(source.length() * 3);
        for (char c : source.toCharArray()) {
            if (c > 256) {
                sb.append(c);
            } else {
                sb.append("\\u");
                sb.append(Character.forDigit((c >>> 12) & 0xf, 16));
                sb.append(Character.forDigit((c >>> 8) & 0xf, 16));
                sb.append(Character.forDigit((c >>> 4) & 0xf, 16));
                sb.append(Character.forDigit((c) & 0xf, 16));
            }
        }
        return sb.toString();
    }

    /**
     * 在字符串中从指定位置开始搜索指定关键字是否成对出现
     * <p>
     * 注意，统计双引号时，不包括带转义符的双引号
     *
     * @param source
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public static boolean isPairedQuota(String source, int beginIndex, int endIndex) {
        if (source == null) {
            return true;
        }

        //检查和处理起始结束索引值
        int fromIndex = beginIndex;
        if (fromIndex > source.length()) fromIndex = source.length();
        int toIndex = endIndex;
        if (toIndex > source.length()) toIndex = source.length();
        if (fromIndex > toIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

        int curIndex = source.indexOf("\"", fromIndex);
        int count = 0;
        while (curIndex > -1) {
            if (curIndex > toIndex) {
                break;
            }
            boolean validQuota = true;
            //过滤转义符
            if (curIndex > 0 && source.charAt(curIndex - 1) == '\\') {
                validQuota = false;
            }
            if (validQuota) count++;

            curIndex = source.indexOf("\"", curIndex + 1);
        }
        if (count % 2 == 0)
            return true;
        else
            return false;
    }


    /**
     * 将json字符串转成java对象
     *
     * @param jsonStr
     * @param obj
     * @return
     */
    public static <T> Object jsonToObj(String jsonStr, Class<T> obj) {
        T t = null;
        try {
            t = JSONObject.parseObject(jsonStr, obj);



        } catch (Exception e) {
            return null;
        }
        return t;
    }

    /**
     * 分析文本内容，转换成map（Key/Value）
     *
     * @param text
     * @param delimiter
     * @return
     */
    public static Map parseTextToMap(String text, String delimiter) {
        return null;
    }

    /**
     * 按照指定字符集对字符串进行编码处理
     *
     * @param source
     * @return
     */
    public static String encode(String source, String charSet) {
        if (source == null || charSet == null) {
            return null;
        }
        try {
            return new String(source.getBytes(), charSet);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * baise64编码字符串
     *
     * @param txt
     * @return
     */
    public static String base64Encode(String txt) {
        if (txt == null) return null;
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(txt.getBytes("UTF-8"));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * baise64编码字符串
     *
     * @param txt
     * @return
     */
    public static String base64Encode(byte[] txt) {
        if (txt == null) return null;
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(txt);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * baise64解码字符串
     *
     * @param txt
     * @return
     */
    public static String base64Decode(String txt) {
        if (txt == null) return null;
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            return new String(decoder.decode(txt), "UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * baise64解码字符串
     *
     * @param txt
     * @return
     */
    public static byte[] base64Decodebyte(String txt) {
        if (txt == null) return null;
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            return decoder.decode(txt);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 将DOS/Windows格式的路径转换为UNIX/Linux格式的路径。
     * 其实就是将路径中的"\"全部换为"/"，因为在某些情况下我们转换为这种方式比较方便，
     * 某中程度上说"/"比"\"更适合作为路径分隔符，而且DOS/Windows也将它当作路径分隔符。
     *
     * @param filePath -转换前的路径
     * @return -转换后的路径
     * @since -0.4
     */
    public static String toUNIXpath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.replaceAll("\\\\", "/").replaceAll("//", "/");
    }

    /**
     * 将DOS/Windows格式的路径转换为UNIX/Linux格式的路径。
     * 其实就是将路径中的"\"全部换为"/"，因为在某些情况下我们转换为这种方式比较方便，
     * 某中程度上说"/"比"\"更适合作为路径分隔符，而且DOS/Windows也将它当作路径分隔符。
     *
     * @param filePath -转换前的路径
     * @return -转换后的路径
     * @since -0.4
     */
    public static String toDosPath(String filePath) {
        if (filePath == null) {
            return null;
        }
        return filePath.replaceAll("/", "\\\\").replaceAll("\\\\\\\\", "\\\\");
    }

    /**
     * 判断字符串是否未空白串
     *
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
        if (str == null) return true;
        String des = str.replaceAll(" ", "").replaceAll("\t", "");
        if (des.length() == 0) return true;
        return false;
    }
}
