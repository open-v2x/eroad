package cn.eroad.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


@Deprecated
public final class DateTimeUtils {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final ZoneOffset zoneOffset = ZoneOffset.of("+8");
    private static final ZoneId zoneId = ZoneId.of("Asia/Shanghai");

    public static long getCurrentEpoch() {
        return LocalDateTime.now().toEpochSecond(zoneOffset);
    }

    public static long getCurrentEpochMilli() {
        return LocalDateTime.now().toInstant(zoneOffset).toEpochMilli();
    }

    public static String format(long now) {
        check(now);
        return dtf.format(LocalDateTime.ofEpochSecond(now / 1000, 0, zoneOffset));
    }

    public static String format(long now, Formatter formatter) {
        check(now);
        switch (formatter) {
            case SECOND:
                return format(now);
            case MILES:
                return dtf1.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(now), zoneId));
            default:
                throw new RuntimeException("the formatter text is not support");
        }
    }

    public static long parseToEpochMilli(String dateTime, Formatter formatter) {
        switch (formatter) {
            case SECOND:
                return LocalDateTime.parse(dateTime, dtf).toInstant(zoneOffset).toEpochMilli();
            case MILES:
                return LocalDateTime.parse(dateTime, dtf1).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            default:
                throw new RuntimeException("the formatter text is not support");
        }
    }

    @Deprecated
    public static String transYYYYMMDDHHMMssSSS(long now) {
        check(now);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf1.format(now);
    }

    @Deprecated
    public static long parse(String dateTime) {
        return LocalDateTime.parse(dateTime, dtf).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    @Deprecated
    public static long parseSSS(String dateTime) {
        return LocalDateTime.parse(dateTime, dtf1).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    @Deprecated
    public static String getCurrentYYYYMMDDHHMMSSTime() {
        return dtf.format(LocalDateTime.now());
    }

    private static void check(long now) {
        if (now / 1000_000_000L < 10)
            throw new RuntimeException("只支持13位时间戳");
    }

    public enum Formatter {
        SECOND,
        MILES
    }

    private DateTimeUtils() {

    }
}
