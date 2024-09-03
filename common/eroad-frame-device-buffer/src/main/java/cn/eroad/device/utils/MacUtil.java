package cn.eroad.device.utils;

/**
 * @project: eroad-frame
 * @ClassName: MacUtil
 * @author: liyongqiang
 * @creat: 2022/8/26 14:36
 * 描述: 用于转换设备mac
 */
public class MacUtil {

    /**
     * 去掉mac地址的-并转换为大写
     *
     * @param mac
     * @return
     */
    public static String toUpperCase(String mac) {
        return mac.replaceAll("-", "").toUpperCase();
    }

    public static void main(String[] args) {
        System.out.println(toUpperCase("f8b5-6890-72ca"));
    }
}
