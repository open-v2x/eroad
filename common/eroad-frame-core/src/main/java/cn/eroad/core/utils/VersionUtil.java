package cn.eroad.core.utils;

import java.util.regex.Pattern;

/**
 * @author binghan
 * @version 1.0
 * @description: 版本号工具类
 * @date 2022/8/26
 */
public class VersionUtil {

    /**
     * 版本号正则校验
     */
    public static final String VERSION_REGEX = "^v([1-9]\\d|[1-9])(.([1-9]\\d|\\d)){2}$";

    /**
     * 版本号非法提示消息
     */
    public static final String VERSION_INVALID = "版本号格式不正确，格式为：v主版本号.次版本号.修订版本号（主版本号为1-99之间的数字，次版本号为0-99之间的数字，修订版本号为0-99之间的数字，如v1.0.0）";

    /**
     * 校验版本号
     *
     * @param version
     * @return
     */
    public static boolean validateVersion(String version) {
        // 非空校验
        if (StringUtil.isEmpty(version)) {
            return false;
        }
        // 格式校验
        if (!Pattern.matches(VERSION_REGEX, version)) {
            return false;
        }
        // 满足
        return true;

    }

    /**
     * 修订版本号加一
     * 例如：v1.1.1 -> v1.1.2
     * 格式不符合返回 null
     *
     * @param oldVersion
     * @return newVersion
     */
    public static String addVersion(String oldVersion) {

        // 格式不符合
        if (!validateVersion(oldVersion)) {
            return null;
        }

        // 截取数字与点的部分
        String versionString = oldVersion.substring(1);
        // 用点分割获取字符数组
        String[] versionStrings = versionString.split("\\.");
        // 主版本号
        String majorVersion = versionStrings[0];
        // 次版本号
        String minorVersion = versionStrings[1];
        // 修订版本号
        String revisionVersion = versionStrings[2];
        // 版本号+1的操作 默认未传版本号 只增加修订版本号
        // 修订版本号不等于99
        if (!"99".equals(revisionVersion)) {
            return "v" + majorVersion + "." + minorVersion + "." + (Integer.parseInt(revisionVersion) + 1);
        }
        // 修订版本号等于99
        revisionVersion = "0";
        // 次版本号不等于99
        if (!"99".equals(minorVersion)) {
            return "v" + majorVersion + "." + (Integer.parseInt(minorVersion) + 1) + "." + revisionVersion;
        }
        // 次版本号等于99
        minorVersion = "0";
        // 主版本号不等于99
        if (!"99".equals(majorVersion)) {
            return "v" + (Integer.parseInt(majorVersion) + 1) + "." + minorVersion + "." + revisionVersion;
        }
        // 主版本号等于99
        return null;
    }

    /**
     * 次版本号号加一
     * 例如：v1.1.1 -> v1.2.1
     * 格式不符合返回 null
     *
     * @param oldVersion
     * @return newVersion
     */
    public static String addMinorVersion(String oldVersion) {

        // 格式不符合
        if (!validateVersion(oldVersion)) {
            return null;
        }

        // 截取数字与点的部分
        String versionString = oldVersion.substring(1);
        // 用点分割获取字符数组
        String[] versionStrings = versionString.split("\\.");
        // 主版本号
        String majorVersion = versionStrings[0];
        // 次版本号
        String minorVersion = versionStrings[1];
        // 修订版本号
        String revisionVersion = versionStrings[2];
        // 版本号+1的操作
        // 次版本号不等于99
        if (!"99".equals(minorVersion)) {
            return "v" + majorVersion + "." + (Integer.parseInt(minorVersion) + 1) + "." + revisionVersion;
        }
        // 次版本号等于99
        minorVersion = "0";
        // 主版本号不等于99
        if (!"99".equals(majorVersion)) {
            return "v" + (Integer.parseInt(majorVersion) + 1) + "." + minorVersion + "." + revisionVersion;
        }
        // 主版本号等于99
        return null;
    }

    /**
     * 主版本号号加一
     * 例如：v1.1.1 -> v2.1.1
     * 格式不符合返回 null
     *
     * @param oldVersion
     * @return newVersion
     */
    public static String addMajorVersion(String oldVersion) {

        // 格式不符合
        if (!validateVersion(oldVersion)) {
            return null;
        }

        // 截取数字与点的部分
        String versionString = oldVersion.substring(1);
        // 用点分割获取字符数组
        String[] versionStrings = versionString.split("\\.");
        // 主版本号
        String majorVersion = versionStrings[0];
        // 次版本号
        String minorVersion = versionStrings[1];
        // 修订版本号
        String revisionVersion = versionStrings[2];
        // 版本号+1的操作
        // 主版本号不等于99
        if (!"99".equals(majorVersion)) {
            return "v" + (Integer.parseInt(majorVersion) + 1) + "." + minorVersion + "." + revisionVersion;
        }
        // 主版本号等于99
        return null;
    }

    /**
     * 比较两个版本号大小
     *
     * @param v1
     * @param v2
     * @return 0 相等，正数 左边大，负数 右边大 null 格式不符合
     */
    public static Integer compareVersion(String v1, String v2) {
        // 格式不符合
        if (!validateVersion(v1) || !validateVersion(v2)) {
            return null;
        }
        // 截取数字与点的部分
        String v1String = v1.substring(1);
        String v2String = v2.substring(1);

        // 用点分割获取字符数组
        String[] v1Strings = v1String.split("\\.");
        String[] v2Strings = v2String.split("\\.");
        int result = 0;
        for (int i = 0; i < v1Strings.length; i++) {
            result = Integer.parseInt(v1Strings[i]) - Integer.parseInt(v2Strings[i]);
            if (0 != (result)) {
                return result;
            }
        }
        return result;
    }

    /**
     * 返回默认版本号 v1.0.0
     *
     * @return
     */
    public static String initVersion() {
        return "v1.0.0";
    }
}
