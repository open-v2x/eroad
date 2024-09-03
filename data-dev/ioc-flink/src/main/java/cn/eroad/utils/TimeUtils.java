package cn.eroad.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public class TimeUtils {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final DateTimeFormatter DATE_TIME_FORMATTER_WITH_MILE = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public static final DateTimeFormatter DATE_TIME_FORMATTER_WITH_SECOND = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //默认上海时区
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC+8");


    //获取第二天零点的13位时间戳
    public static long getNextDayEpochTime() {
        LocalDate nextDate = LocalDate.now().plusDays(1);
        return nextDate.atStartOfDay(DEFAULT_ZONE_ID).toEpochSecond() * 1000;
    }

    //将日期时间格式的字符串转换成13位的时间戳
    public static long convertToTimestamp(String dateTime) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER_WITH_MILE);
            return localDateTime.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        } catch (Exception e) {
            return 0L;
        }

    }

    //将13位的时间戳转换成日期时间格式的字符串
    public static String convertToString(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
        return localDateTime.format(DATE_TIME_FORMATTER_WITH_MILE);
    }

    //将13位的时间戳转换成日期时间格式的字符串
    public static String convertToString(long timestamp, DateTimeFormatter dtf) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
        return localDateTime.format(dtf);
    }

}
