package cn.example.radder;


import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

import static cn.eroad.rad.util.SendMessageEncode.encodeSendAndRecieve;


class RadarApplicationTests {
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    void contextLoads() {
    }

    String register = "c00000000000070001000000000a00010010820101353030303031313431333500000000000000000044656775726f6f6e00000000000000000000000043697452616461722d54443635300000000000008cd651d5048e5c40969350fa4204434000000000dbdca80201ffffff00dbdca80255dbdca80256000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005c1210270f273c006433dbddee96be4bcdc0";

    @Test
    public void t() {
        System.out.println(formatter.format(new Date()));
        System.out.println(hexStrToDouble("BF0003638F00C11A"));

        String ip = "FD20:D1A9:3C94:2001::217";
        String[] ips = ip.split("::");
        if (ips.length != 2) {
            System.out.println("录入ip:异常");
        }
        System.out.println((ips[0] + ":0:0:0:" + ips[1]).toLowerCase());
    }

    @Test
    public void a() {
        System.out.println(encodeSendAndRecieve("1"));
    }

    public static Double hexStrToDouble(String hexStr) {
        if (StringUtils.isEmpty(hexStr)) {
            return null;
        }
        BigInteger integer = new BigInteger(hexStr.trim(), 16);
        // long longBits = Long.valueOf(hexStr,16).longValue();
        long longBits = integer.longValue();
        return Double.longBitsToDouble(longBits);
    }


    @Test
    void test1() {
        String a = "XA/mwraddar/WJKJ/127.0.0.1/127.0.0.1_20210912215500_20210912220000.txt";
        String b = "127.0.0.1";
        String c = a.substring(0, a.indexOf(b) + b.length());
        System.out.println(c);
    }
}
