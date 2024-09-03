package cn.eroad.device.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @project: eroad-frame
 * @ClassName: IpUtil
 * @author: liyongqiang
 * @creat: 2022/7/18 16:36
 * 描述:
 */
public class IpUtil {

    /**
     * 将 非简写的IPv6 转换成 简写的IPv6
     *
     * @param fullIPv6 非简写的IPv6
     * @return 简写的IPv6
     */
    public static String parseFullIPv6ToAbbreviation(String fullIPv6) {
        String abbreviation = "";

        // 1,校验 ":" 的个数 不等于7  或者长度不等于39  直接返回空串
        int count = fullIPv6.length() - fullIPv6.replaceAll(":", "").length();
        if (fullIPv6.length() != 39 || count != 7) {
            return abbreviation;
        }

        // 2,去掉每一位前面的0
        String[] arr = fullIPv6.split(":");

        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].replaceAll("^0{1,3}", "");
        }

        // 3,找到最长的连续的0
        String[] arr2 = arr.clone();
        for (int i = 0; i < arr2.length; i++) {
            if (!"0".equals(arr2[i])) {
                arr2[i] = "-";
            }
        }

        Pattern pattern = Pattern.compile("0{2,}");
        Matcher matcher = pattern.matcher(StringUtils.join(arr2, ""));
        String maxStr = "";
        int start = -1;
        int end = -1;
        while (matcher.find()) {
            if (maxStr.length() < matcher.group().length()) {
                maxStr = matcher.group();
                start = matcher.start();
                end = matcher.end();
            }
        }

        // 3,合并
        if (maxStr.length() > 0) {
            for (int i = start; i < end; i++) {
                arr[i] = ":";
            }
        }
        abbreviation = StringUtils.join(arr, ":");
        abbreviation = abbreviation.replaceAll(":{2,}", "::");

        return abbreviation;

    }

    public static String parseAbbreviationToFullIP(String abbreviation) {
        if (abbreviation.contains(":")) {
            return parseAbbreviationToFullIPv6(abbreviation);
        } else {
            return abbreviation;
        }
    }

    /**
     * 将 简写的IPv6 转换成 非简写的IPv6
     *
     * @param abbreviation 简写的IPv6
     * @return 非简写的IPv6
     */
    public static String parseAbbreviationToFullIPv6(String abbreviation) {
        String fullIPv6 = "";

        if ("::".equals(abbreviation)) {
            return "0:0:0:0:0:0:0:0";
        }

        String[] arr = new String[]
                {"0", "0", "0", "0", "0", "0", "0", "0"};

        if (abbreviation.startsWith("::")) {
            String[] temp = abbreviation.substring(2, abbreviation.length()).split(":");
            for (int i = 0; i < temp.length; i++) {
                String tempStr = temp[i];
                if (tempStr.length() == 0) {
                    arr[i + 8 - temp.length] = "0";
                } else if (tempStr.length() == 1) {
                    arr[i + 8 - temp.length] = tempStr;
                } else {
                    arr[i + 8 - temp.length] = tempStr.replaceFirst("^0*", "");
                }

            }

        } else if (abbreviation.endsWith("::")) {
            String[] temp = abbreviation.substring(0, abbreviation.length() - 2).split(":");
            for (int i = 0; i < temp.length; i++) {
                String tempStr = temp[i];
                if (tempStr.length() == 0) {
                    arr[i] = "0";
                } else if (tempStr.length() == 1) {
                    arr[i] = tempStr;
                } else {
                    arr[i] = tempStr.replaceFirst("^0*", "");
                }

            }

        } else if (abbreviation.contains("::")) {
            String[] tempArr = abbreviation.split("::");

            String[] temp0 = tempArr[0].split(":");
            for (int i = 0; i < temp0.length; i++) {
                String tempStr = temp0[i];
                if (tempStr.length() == 0) {
                    arr[i] = "0";
                } else if (tempStr.length() == 1) {
                    arr[i] = tempStr;
                } else {
                    arr[i] = tempStr.replaceFirst("^0*", "");
                }

            }

            String[] temp1 = tempArr[1].split(":");
            for (int i = 0; i < temp1.length; i++) {
                String tempStr = temp1[i];
                if (tempStr.length() == 0) {
                    arr[i + 8 - temp1.length] = "0";
                } else if (tempStr.length() == 1) {
                    arr[i + 8 - temp1.length] = tempStr;
                } else {
                    arr[i + 8 - temp1.length] = tempStr.replaceFirst("^0*", "");
                }

            }

        } else {
            return abbreviation.toLowerCase();












        }

        fullIPv6 = StringUtils.join(arr, ":");

        return fullIPv6.toLowerCase();
    }

    public static void main(String[] args) {
        String ip = "FD20:D1A9:3C94:2001::2C7";
        String ip0 = "fd20:d1a9:3c94:2001:0:0:0:1db";
        System.out.println(parseAbbreviationToFullIPv6(ip));
        System.out.println(parseAbbreviationToFullIPv6(ip0));
    }
}
