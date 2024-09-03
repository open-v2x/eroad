package cn.eroad.videocast.util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @Auther zhaohanqing
 * @Date 2022/7/29
 */
public class RandomNumber {
    public static String getGUID() {
        StringBuilder buffer = new StringBuilder();
        Random secureRandom = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            int type = secureRandom.nextInt(9);
            buffer.append(type);
        }
        return buffer.toString();
    }
}
