package cn.eroad.core.utils;

import cn.hutool.core.date.DateTime;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @Description: 时间处理工具类
 * @Title: DateUtil
 * @Date 2021/4/23 9:56
 */
public final class DateUtil {

    public static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    private static long hourLong = 60 * 60 * 1000;

    /**
     * new SimpleDateFormat("yyyy-MM-dd")
     */
    public static final String YYYYMMDD_LINE = "yyyy-MM-dd";

    /**
     * new SimpleDateFormat("yy-MM-dd")
     */
    public static final String YYMMDD_LINE = "yy-MM-dd";

    /**
     * new SimpleDateFormat("yyyy/MM/dd")
     */
    public static final String YYYYMMDD_SLASH = "yyyy/MM/dd";

    /**
     * new SimpleDateFormat("yyyyMMdd")
     */
    public static final String YYYYMMDD = "yyyyMMdd";

    /**
     * new SimpleDateFormat("yyyyMMddHH")
     */
    public static final String YYYYMMDDHH = "yyyyMMddHH";

    /**
     * new SimpleDateFormat("yyyy-MM-dd HH:mm")
     */
    public static final String YYYYMMDDHHMM_LINE = "yyyy-MM-dd HH:mm";

    /**
     * new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
     */
    public static final String YYYYMMDDHHMMSS_LINE = "yyyy-MM-dd HH:mm:ss";

    /**
     * new SimpleDateFormat("yyyyMMddHHmm")
     */
    public static final String YYYYMMDDHHMM = "yyyyMMddHHmm";

    /**
     * new SimpleDateFormat("yyyyMMddHHmmss")
     */
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    /**
     * 时间格式化对象
     */
    private static final String TIME_SEC_FORMAT = "HH:mm:ss";

    public static final String HHMM = "HH:mm";

    /**
     * "MMdd" 0723 月日
     */
    public static final String MMDD = "MMdd";

    private static final Locale DEFAULT_LOCAL = Locale.getDefault();

    private DateUtil() {
        super();
    }

    /**
     * 获取当前时间的时间对象
     *
     * @return 时间对象
     */
    public static final Date nowDate() {
        return new Date();
    }

    /**
     * 系统最小时间
     *
     * @return 时间对象
     */
    public static final Date minDate() {
        return dateBegin(getDate(1900, 1, 1));
    }

    /**
     * 系统最大时间
     *
     * @return 时间对象
     */
    public static final Date maxDate() {
        return dateEnd(getDate(2079, 1, 1));
    }

    /**
     * 获取指定时间的年
     *
     * @param date 时间对象
     * @return 年数
     */
    public static final int year(Date date) {
        if (date == null) {
            return 0;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 获取指定时间的月
     *
     * @param date 时间对象
     * @return 月数
     */
    public static final int month(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取指定时间的日
     *
     * @param date 时间对象
     * @return 日数
     */
    public static final int day(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DATE);
    }

    /**
     * 获取指定时间的日
     *
     * @param date 时间对象
     * @return 小时数
     */
    public static final int hour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取指定时间的分钟
     *
     * @param date 时间对象
     * @return 小时数
     */
    public static final int minute(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

    /**
     * 获取一个时间对象
     *
     * @param year  格式为：2004
     * @param month 从1开始
     * @param date  从1开始
     * @return 时间对象
     */
    public static final Date getDate(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date);
        return calendar.getTime();
    }

    /**
     * 获取一个时间对象
     *
     * @param year   格式为：2004
     * @param month  从1开始
     * @param date   从1开始
     * @param hour   小时
     * @param minute 分钟
     * @param second 秒钟
     * @return 日期对象
     */
    public static final Date getDateTime(int year, int month, int date, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date, hour, minute, second);
        return calendar.getTime();
    }

    /**
     * 在一个已知时间的基础上增加指定的时间,负数表示减少
     *
     * @param oldDate 已知时间对象
     * @param year    年
     * @param month   月
     * @param date    日
     * @return 时间对象
     */
    public static final Date addDate(Date oldDate, int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldDate);
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DATE, date);
        return calendar.getTime();
    }

    /**
     * 在一个已知时间的基础上增加指定的时间,负数表示减少
     *
     * @param oldDate
     * @param year
     * @param month
     * @param date
     * @return
     */
    public static final Date addDate(Date oldDate, int year, int month, int week, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldDate);
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DAY_OF_WEEK, week);
        calendar.add(Calendar.DATE, date);
        return (Date) calendar.getTime().clone();
    }

    public static int constDateSub = -36500;

    /**
     * 返回两个时间相差的天数
     *
     * @param a 时间对象a
     * @param b 时间对象b
     * @return 相差天数
     */
    public static final int dateSub(Date a, Date b) {
        if (a == null || b == null) {
            return constDateSub;
        }

        int date = (int) (a.getTime() / (24 * 60 * 60 * 1000) - b.getTime() / (24 * 60 * 60 * 1000));
        return date;
    }

    /**
     * 返回两个日期相差的日期数（只判断日期不判断时间跨度），later为更后面的日期
     *
     * @param later  更晚时间对象
     * @param former 更早时间对象
     * @return 相差天数
     */
    public static int dateDifference(Date later, Date former) {
        if (later == null || former == null) {
            return 0;
        }

        Calendar calendarA = Calendar.getInstance();
        Calendar calendarB = Calendar.getInstance();
        calendarA.setTime(later);
        calendarB.setTime(former);

        calendarA.set(Calendar.HOUR_OF_DAY, 0);
        calendarA.set(Calendar.MINUTE, 0);
        calendarA.set(Calendar.SECOND, 0);
        calendarB.set(Calendar.HOUR_OF_DAY, 0);
        calendarB.set(Calendar.MINUTE, 0);
        calendarB.set(Calendar.SECOND, 0);

        return (int) ((calendarA.getTime().getTime() - calendarB.getTime().getTime()) / (24 * 60 * 60 * 1000L));
    }

    /**
     * 返回两个日期相差的年数（只判断日期不判断时间跨度），later为更后面的日期
     *
     * @param later  更晚时间对象
     * @param former 更早时间对象
     * @return 相差年数
     */
    public static int yeadDifference(Date later, Date former) {
        if (later == null || former == null) {
            return 0;
        }

        Calendar calendarA = Calendar.getInstance();
        Calendar calendarB = Calendar.getInstance();
        calendarA.setTime(later);
        calendarB.setTime(former);

        calendarA.set(Calendar.HOUR_OF_DAY, 0);
        calendarA.set(Calendar.MINUTE, 0);
        calendarA.set(Calendar.SECOND, 0);
        calendarB.set(Calendar.HOUR_OF_DAY, 0);
        calendarB.set(Calendar.MINUTE, 0);
        calendarB.set(Calendar.SECOND, 0);

        int result = 0;
        do {
            calendarA.add(Calendar.YEAR, -1);
            if (calendarA.compareTo(calendarB) >= 0) {
                result++;
            }
        } while (calendarA.compareTo(calendarB) > 0);

        return result;
    }

    /**
     * 判断两个日期是否相等
     *
     * @param a 时间对象a
     * @param b 时间对象b
     * @return 是都相同，0：不同；1：相同
     */
    public static final int dateSubAddOne(Date a, Date b) {
        int date = (int) (a.getTime() / (24 * 60 * 60 * 1000) - b.getTime() / (24 * 60 * 60 * 1000));
        return date <= 0 ? 1 : date + 1;
    }

    /**
     * 判断某一时间是否介于两个时间之内
     *
     * @param beginDate 开始时间
     * @param nowDate   需检测的时间
     * @param endDate   结束时间
     * @return
     */
    public static final boolean isBetweenDateS(Date beginDate, Date nowDate, Date endDate) {
        if (beginDate != null && nowDate != null && endDate != null) {
            return (beginDate.getTime() / (24 * 60 * 60 * 1000)) <= (nowDate.getTime() / (24 * 60 * 60 * 1000))
                    && (nowDate.getTime() / (24 * 60 * 60 * 1000)) <= (endDate.getTime() / (24 * 60 * 60 * 1000));
        } else if (beginDate != null && nowDate != null) {
            return (beginDate.getTime() / (24 * 60 * 60 * 1000)) <= (nowDate.getTime() / (24 * 60 * 60 * 1000));
        } else if (beginDate == null && nowDate != null && endDate != null) {
            return (nowDate.getTime() / (24 * 60 * 60 * 1000)) <= (endDate.getTime() / (24 * 60 * 60 * 1000));
        }
        return false;
    }

    /**
     * 返回两个时间相差多少秒
     *
     * @param a 时间对象a
     * @param b 时间对象b
     * @return 相差秒
     */
    public static final int subSecond(Date a, Date b) {
        return (int) (a.getTime() / (1000) - b.getTime() / (1000));
    }


    public static final int subMinute(Date big, Date small) {
        return (int) (big.getTime() / (1000 * 60) - small.getTime() / (1000 * 60));
    }

    /**
     * 判断两个时间相差秒数
     *
     * @param str 时间字符串
     * @param b   时间对象
     * @return 相差秒数
     */
    public static final int subSecond(String str, Date b) {
        Date a = null;
        try {
            a = new SimpleDateFormat(HHMM).parse(str);
        } catch (ParseException e) {

            return 0;
        }
        return (int) ((a.getTime() % (24 * 60 * 60 * 1000)) / 1000 - (b.getTime() % (24 * 60 * 60 * 1000)) / 1000);
    }

    /**
     * 一天的开始时间
     *
     * @param date 时间对象
     * @return 开始时间对象
     */
    public static final Date dateBegin(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dateBegin(calendar);
        return calendar.getTime();
    }

    /**
     * 一天的结束时间
     *
     * @param date 时间对象
     * @return 结束时间对象
     */
    public static final Date dateEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dateEnd(calendar);
        return calendar.getTime();
    }

    /**
     * 一天的结束时间
     *
     * @param calendar 日历对象
     * @return 结束时间日历对象
     */
    public static final Calendar dateEnd(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 一天的开始时间
     *
     * @param calendar 日历对象
     * @return 开始时间日历对象
     */
    public static final Calendar dateBegin(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 小时的开始时间
     *
     * @param date 时间对象
     * @return 开始时间对象
     */
    public static final Date dateHourBegin(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dateHourBegin(calendar);
        return calendar.getTime();
    }

    /**
     * 小时的结束时间
     *
     * @param date 时间对象
     * @return 开始时间对象
     */
    public static final Date dateHourEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dateHourEnd(calendar);
        return calendar.getTime();
    }

    /**
     * 小时的开始时间
     *
     * @param calendar 日历对象
     * @return 开始时间日历对象
     */
    public static final Calendar dateHourBegin(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 小时的结束时间
     *
     * @param calendar 日历对象
     * @return 开始时间日历对象
     */
    public static final Calendar dateHourEnd(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 一月的开始时间
     *
     * @param date 时间对象
     * @return 开始时间对象
     */
    public static final Date monthBegin(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DATE, day);
        dateBegin(calendar);
        return calendar.getTime();
    }

    /**
     * 一月的结束时间
     *
     * @param date 时间对象
     * @return 结束时间对象
     */
    public static final Date monthEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DATE, day);
        dateEnd(calendar);
        return calendar.getTime();
    }

    /**
     * 一年的开始时间
     *
     * @param date 时间对象
     * @return 开始时间对象
     */
    public static final Date yearBegin(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.getActualMinimum(Calendar.DAY_OF_YEAR);
        calendar.set(Calendar.DATE, month);
        dateBegin(calendar);
        return calendar.getTime();
    }

    /**
     * 一年的结束时间
     *
     * @param date 时间对象
     * @return 结束时间对象
     */
    public static final Date yearEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
        calendar.set(Calendar.DATE, day);
        dateEnd(calendar);
        return calendar.getTime();
    }

    /**
     * 从字符串转换为date 默认格式为 "yyyy-MM-dd"
     *
     * @param source 需转换的字符串
     * @return 时间对象
     */
    public static final Date parseDate(String source) {
        if (source == null || source.trim().length() == 0) {
            return null;
        }
        int eight = 8;
        Date returnDate = new Date();
        // 判断日期格式
        try {
            if (source.trim().length() == 8) {
                returnDate = new SimpleDateFormat(YYYYMMDD).parse(source);
            } else if (source.trim().length() == 10) {
                returnDate = new SimpleDateFormat(YYYYMMDD_LINE).parse(source);
            } else if (source.trim().length() == 16) {
                returnDate = new SimpleDateFormat(YYYYMMDDHHMM_LINE).parse(source);
            } else if (source.trim().length() >= 19) {
                returnDate = new SimpleDateFormat(YYYYMMDDHHMMSS_LINE).parse(source);
            }
        } catch (ParseException e) {
            logger.error("DateUtil parseDate error", e);
            return null;
        }
        return returnDate;
    }

    /**
     * 从字符串转换为date 默认格式为 "yyyy-MM-dd HH:mm"
     *
     * @param source 需转换的字符串
     * @return 日期对象
     */
    public static final Date parseDateTime(String source) {
        if (source == null || source.length() == 0) {
            return null;
        }
        try {
            return new SimpleDateFormat(YYYYMMDDHHMM_LINE).parse(source);
        } catch (ParseException e) {
            logger.error("DateUtil parseDate error", e);
            return null;
        }
    }

    /**
     * 从字符串转换为date
     *
     * @param source    需转换的字符串
     * @param formatStr 格式类型
     * @return 日期对象
     */
    public static final Date parseDateTime(String source, String formatStr) {
        if (source == null || source.length() == 0) {
            return null;
        }
        try {
            return new SimpleDateFormat(formatStr).parse(source);
        } catch (ParseException e) {
            logger.error("DateUtil parseDate error", e);
            return null;
        }
    }

    /**
     * 从字符串转换为date 默认格式为 "yyyy-MM-dd HH:mm:ss"
     *
     * @param source 需转换的字符串
     * @return 日期对象
     */
    public static final Date parseDateTimes(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(source);
        } catch (ParseException e) {
            logger.error("DateUtil parseDate error", e);
        }
        return null;
    }

    /**
     * 格式化输出（只读的时候） 默认格式为 "yyyy-MM-dd"
     *
     * @param date 时间对象
     * @return 格式化后的时间字符串
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(YYYYMMDD_LINE).format(date);
    }

    /**
     * 格式化输出（只读的时候） 默认格式为 "yy-MM-dd"
     *
     * @param date 时间独享
     * @return 格式化后的时间字符串
     */
    public static String formatDateYY(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(YYMMDD_LINE).format(date);
    }

    /**
     * 格式化输出显示（填写的时候） yyyyMMdd
     *
     * @param date 时间的对象
     * @return 格式化后的时间字符串
     */
    public static String formatDateInput(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(YYYYMMDD).format(date);
    }

    /**
     * 格式化输出显示（填写的时候） yyyy/MM/dd
     *
     * @param date 时间对象
     * @return 格式化后的时间字符串
     */
    public static String formatDateSlide(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(YYYYMMDD_SLASH).format(date);
    }

    /**
     * 格式化输出 默认格式为 "yyyy-MM-dd HH:mm"
     *
     * @param date 时间对象
     * @return 格式化后的时间字符串
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(YYYYMMDDHHMM_LINE).format(date);
    }

    /**
     * 格式化输出 默认格式为 "yyyy-MM-dd HH:mm:ss"
     *
     * @param date 时间对象
     * @return 格式化后的时间字符串
     */
    public static String formatDateTimeS(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(YYYYMMDDHHMMSS_LINE).format(date);
    }

    /**
     * 格式化输出 默认格式为 "yyyy-MM-dd HH:mm"
     *
     * @param date 时间对象
     * @return 格式化后的时间字符串
     */
    public static String getDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(YYYYMMDDHHMM_LINE).format(date).substring(5, 10).replaceAll("_", "/");
    }

    /**
     * 格式化输出 默认格式为 "yyyyMMddHHmm"
     *
     * @param date 时间对象
     * @return 格式化后的时间字符串
     */
    public static String formatDateTimeInput(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(YYYYMMDDHHMM).format(date);
    }

    /**
     * 格式化输出 默认格式为 "yyyyMMddHHmmss"
     *
     * @param date 时间对象
     * @return 格式化后的时间字符串
     */
    public static String formatDateTimeSecondInput(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(YYYYMMDDHHMMSS).format(date);
    }

    /**
     * 格式化输出显示（填写的时候） HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String formatDateNoday(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(TIME_SEC_FORMAT).format(date);
    }

    /**
     * 时间戳转换成整天时间
     *
     * @param timestamp 时间戳
     * @return 整天时间
     * @throws ParseException
     */
    public static Date parse2Day(Long timestamp) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYYMMDD_LINE);
        return sdf.parse(sdf.format(new Date(timestamp)));
    }

    /**
     * 判断是否是闰年
     *
     * @param yearInt 年份
     * @return 是否为闰年
     */
    public static boolean isLeapYear(int yearInt) {
        boolean flag = false;
        boolean isMultipleOfFourAndOneHundred = (yearInt % 4 == 0) && (yearInt % 100 != 0);
        boolean isMultipleOfFourAndFourHundred = (yearInt % 4 == 0) && (yearInt % 400 == 0);
        if (isMultipleOfFourAndOneHundred || isMultipleOfFourAndFourHundred) {
            return true;
        }
        return flag;
    }

    /**
     * 在指定的时间内增加天数。负数表示为减
     *
     * @param date 时间对象
     * @param days 增加的天数
     * @return 增加后的时间对象
     */
    public static Date addDays(Date date, int days) {
        Date newdate = new Date();
        long newtimems = date.getTime() + ((long) days * 24 * 60 * 60 * 1000);
        newdate.setTime(newtimems);
        return newdate;
    }

    /**
     * 根据日期判断今天 昨天 前天3个时间段，如果不是返回String类型
     *
     * @param date 时间对象
     * @return 返回判断结果
     */
    public static String cnDate(Date date) {
        if (date == null) {
            return "";
        }
        Date newdate = new Date();
        Long newTimes = newdate.getTime();
        Long cdTimes = date.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String dateStr1 = DateUtil.formatDate(DateUtil.addDays(newdate, -1)) + " 23:59:59";
        String dateStr2 = DateUtil.formatDate(DateUtil.addDays(newdate, -2)) + " 23:59:59";
        String dateStr3 = DateUtil.formatDate(DateUtil.addDays(newdate, -3)) + " 23:59:59";
        Date date1 = DateUtil.parseDateTimes(dateStr1);
        Date date2 = DateUtil.parseDateTimes(dateStr2);
        Date date3 = DateUtil.parseDateTimes(dateStr3);
        if (newTimes >= cdTimes && cdTimes > date1.getTime()) {
            return "今天 " + sdf.format(date);
        } else if (cdTimes < date1.getTime() && cdTimes > date2.getTime()) {
            return "昨天 " + sdf.format(date);
        } else if (cdTimes < date2.getTime() && cdTimes > date3.getTime()) {
            return "前天 " + sdf.format(date);
        }

        return DateUtil.formatDateTime(date);
    }

    /**
     * 昨天  开始  时间 long型
     *
     * @return
     */
    public static long getYesterDayStartTimeLong() {
        DateTime beginOfDay = cn.hutool.core.date.DateUtil.beginOfDay(new Date());
        long time = beginOfDay.getTime() - 24 * hourLong;

        return time;
    }


    /**
     * 昨天   结束  时间 long型
     *
     * @return
     */
    public static long getYesterDayEndTimeLong() {
        DateTime beginOfDay = cn.hutool.core.date.DateUtil.endOfDay(new Date());
        long time = beginOfDay.getTime() - 24 * hourLong;
        return time;
    }

    /**
     * 在一个已知时间的基础上增加指定的时间,负数表示减少
     *
     * @param oldDate 已知时间
     * @param year    年
     * @param month   月
     * @param date    日
     * @param hour    时
     * @param minute  分
     * @param second  秒
     * @return 增加后的时间对象
     */
    public static final Date addDate(Date oldDate, int year, int month, int date, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldDate);
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DATE, date);
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        calendar.add(Calendar.MINUTE, minute);
        calendar.add(Calendar.SECOND, second);
        return calendar.getTime();
    }

    /**
     * 返回“yyyy-MM”格式的String日期
     *
     * @param d “yyyy-MM” 时间对象
     * @return 格式化后的时间字符串
     */
    public static String toDateStr(Date d) {
        if (d == null) {
            return "";
        } else {
            return (new SimpleDateFormat("yyyy-MM")).format(d);
        }
    }

    /**
     * 按照指定格式将时间转化为str
     *
     * @param date   时间对象
     * @param format 转换的格式
     * @return 格式化后的时间字符串
     */
    public static String formatDate(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    /**
     * 判断是否在同一天
     *
     * @param date1
     * @param date2
     * @return
     * @throws ParseException
     */
    public static Boolean isSameDate(Date date1, Date date2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int year1 = calendar.get(Calendar.YEAR);
        int day1 = calendar.get(Calendar.DAY_OF_YEAR);

        calendar.setTime(date2);
        int year2 = calendar.get(Calendar.YEAR);
        int day2 = calendar.get(Calendar.DAY_OF_YEAR);

        return (year1 == year2) && (day1 == day2);
    }

    /**
     * sql语句中起始日期处理
     *
     * @param date 时间字符串
     * @return sql字符串
     */
    public static String sqlDateS(String date) {
        return "str_to_date('" + date + " 00:00:00','%Y-%m-%d %h:%i:%s')";
    }

    /**
     * sql语句中结束日期处理
     *
     * @param date 时间字符串
     * @return sql字符串
     */
    public static String sqlDateE(String date) {
        return "str_to_date('" + date + " 23:59:59','%Y-%m-%d %h:%i:%s')";
    }

    /**
     * 解析java.util.Date to java.sql.Date(used for ps.setDate(x,xxx))
     *
     * @param dateStr 要转换的java.util.date
     * @return java.sql.Date
     */
    public static java.sql.Date parse2SqlDate(String dateStr) {
        return StringUtils.isBlank(dateStr) ? null : new java.sql.Date(parseDate(dateStr).getTime());
    }

    /**
     * 身份证有效日期效验
     *
     * @param beginStr 开始时间
     * @param endStr   结束时间
     * @param birthday 生日
     * @return String 返回1时为效验通过
     * @throws ParseException
     */
    public static String validateDate(String beginStr, String endStr, String birthday) {
        try {
            // 验证日期是否有效
            String regx = "^(?:19|20|30)[0-9][0-9](?:(?:0[1-9])|(?:1[0-2]))(?:(?:[0-2][1-9])|(?:[1-3][0-1]))$";
            if (!Pattern.matches(regx, endStr)) {
                return "证件有效截止日期无效";
            }
            if (!Pattern.matches(regx, beginStr)) {
                return "证件有效起始日期无效";
            }
            if (!Pattern.matches(regx, birthday)) {
                return "出生日期无效";
            }

            Long begin = new SimpleDateFormat(YYYYMMDD).parse(beginStr).getTime();
            Long end = new SimpleDateFormat(YYYYMMDD).parse(endStr).getTime();
            Long birth = new SimpleDateFormat(YYYYMMDD).parse(birthday).getTime();

            Long now = System.currentTimeMillis();
            if (begin < birth) {
                return "开始日期早于出生日期";
            }
            if (begin > now) {
                return "开始日期晚于今天";
            }
            if (end < now) {
                return "您的身份证已到期";
            }
        } catch (ParseException e) {
            logger.warn("日期格式转换错误：begin:" + beginStr + ",end:" + endStr + ",birthday:" + birthday);
            return "日期格式不符合规则。";

        }
        return "1";
    }

    public static String timeStamp2Date(String time) {
        Long timeLong = Long.parseLong(time);
        SimpleDateFormat sdf = new SimpleDateFormat(YYYYMMDDHHMMSS_LINE);
        Date date;
        try {
            date = sdf.parse(sdf.format(timeLong));
            return sdf.format(date);
        } catch (ParseException e) {
            logger.error("时间戳转换失败", e);
            return null;
        }
    }

    /**
     * 严格将日期字符串 转化为第2个参数指定的格式, 转换失败返回NULL
     */
    public static Date parse2DateStrictly(String date, String... dateFormat) {
        try {
            return parse2Date(date, dateFormat, false);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 非严格将日期字符串 转化为第2个参数指定的格式, 转换失败返回NULL
     *
     * @param date       欲格式化的日期
     * @param dateFormat 日期格式化参数
     */
    public static Date parse2Date(String date, String... dateFormat) {
        try {
            if (dateFormat == null || dateFormat.length < 1) {
                return parse2Date(date, new String[]{YYYYMMDD_LINE, YYYYMMDD, YYYYMMDDHHMMSS_LINE, TIME_SEC_FORMAT},
                        true);
            }

            return parse2Date(date, dateFormat, true);
        } catch (ParseException e) {
            return null;
        }
    }

    private static Date parse2Date(String date, String[] dateFormat, boolean lenient) throws ParseException {
        if (date == null || dateFormat == null) {
            throw new IllegalArgumentException("date and dateFormat 必须不能为空");
        }

        SimpleDateFormat parser = new SimpleDateFormat();
        // 指定日期/时间解析是否不严格
        parser.setLenient(lenient);
        ParsePosition pos = new ParsePosition(0);
        for (int i = 0, len = dateFormat.length; i < len; i++) {
            String pattern = dateFormat[i];

            if (dateFormat[i].endsWith("ZZ")) {
                pattern = pattern.substring(0, pattern.length() - 1);
            }

            parser.applyPattern(pattern);
            pos.setIndex(0);

            String str2 = date;

            if (dateFormat[i].endsWith("ZZ")) {
                str2 = date.replaceAll("([-+][0-9][0-9]):([0-9][0-9])$", "$1$2");
            }

            Date dateR = parser.parse(str2, pos);
            if (dateR != null && pos.getIndex() == str2.length()) {
                return dateR;
            }
        }
        throw new ParseException("无法转换: " + date, -1);
    }

    /**
     * 隔天凌晨到当前时间的秒数
     *
     * @return
     */
    public static int tomorrowBegin() {
        // 初始化日历
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);

        // 设置为凌晨0点
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long seconds = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
        return (int) seconds;
    }

    /**
     * 按日加/减.
     *
     * @param amount of date or time to be added to the field.
     * @return Convert Calendar to String like "yyyyMMdd".
     */
    public static String addDay(int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, amount);
        return formatDate(calendar);
    }

    /**
     * Convert date to String like "yyyyMMdd".
     */
    public static String formatDate(Calendar calendar) {
        return new SimpleDateFormat(YYYYMMDD).format(calendar.getTime());
    }

    /**
     * 返回当前日期字符串.根据一定格式
     *
     * @param locale 语言环境
     */
    public static String getCurrentDate(String dateFormat, Locale locale) {
        return FastDateFormat.getInstance(dateFormat, locale).format(new Date());
    }

    /**
     * 返回当前日期字符串.根据一定格式 默认语言环境
     */
    public static String getCurrentDate(String dateFormat) {
        return getCurrentDate(dateFormat, DEFAULT_LOCAL);
    }

    /**
     * 返回当前日期字符串.默认格式为 "yyyy-MM-dd"
     */
    public static String getCurrentDate() {
        return getCurrentDate(YYYYMMDD_LINE);
    }

    /**
     * 返回的当前秒级时间戳字符串 根据一定格式
     *
     * @param locale 语言环境
     */
    public static String getCurrentTimestamp(String timeFormat, Locale locale) {
        return FastDateFormat.getInstance(timeFormat, locale).format(System.currentTimeMillis());
    }

    /**
     * 返回的当前秒级时间戳字符串 根据一定格式 默认语言环境
     */
    public static String getCurrentTimestamp(String timeFormat) {
        return getCurrentTimestamp(timeFormat, DEFAULT_LOCAL);
    }

    /**
     * 返回的当前秒级时间戳字符串 默认格式为'yyyy-MM-dd hh:mm:ss'
     */
    public static String getCurrentTimestamp() {
        return getCurrentTimestamp(YYYYMMDDHHMMSS_LINE);
    }

    /**
     * 返回Internate标准格式的当前秒级时间戳
     */
    public static Timestamp getCurrentTimestampT() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.minute(new Date()));
    }
}
