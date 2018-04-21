package com.cmcciot.platform.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author
 */
public class DateUtil {
    /**
     * 默认的日期格式 yyyy-MM-dd
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认的时间格式 HH:mm:ss
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 只有时:分
     */
    public static final String TIME_NO_SECOND_FORMAT = "HH:mm";

    /**
     * 默认的日期时间格式 yyyy-MM-dd HH:mm:ss
     */
    public static final String DATETIME_FORMAT = DateUtil.DATE_FORMAT + " "
            + DateUtil.TIME_FORMAT;

    /**
     * 无秒的日期时间格式 yyyy-MM-dd HH:mm
     */
    public static final String DATETIME_NO_SECOND_FORMAT = DateUtil.DATE_FORMAT
            + " " + DateUtil.TIME_NO_SECOND_FORMAT;

    /**
     * 带毫秒的日期时间格式 yyyy-MM-dd HH:mm:ss.ttt
     */
    public static final String DATETIME_MS_FORMAT = DATETIME_FORMAT + ".SSS";

    /**
     * 所有格式
     */
    public static final String[] ALL_FORMAT = new String[]{
            DateUtil.DATETIME_FORMAT, DATETIME_NO_SECOND_FORMAT, DATE_FORMAT,
            TIME_FORMAT, TIME_NO_SECOND_FORMAT};
    /**
     * 一天的毫秒数
     */
    public static final int DAY = 24 * 60 * 60 * 1000;

    /**
     * 将日期字符串date按指定的格式dateformat转换为Date类型
     *
     * @param date
     * @param dateformat SimpleDateFormat 日期格式
     * @return Date 转换后的日期
     * @throws ParseException 如果无法转换
     */
    public static Date toDate(String date, SimpleDateFormat dateformat)
            throws ParseException {
        return dateformat.parse(date);
    }

    /**
     * 将日期字符串date按指定的格式dateformat转换为Date类型
     *
     * @param date
     * @param dateformat String 日期格式字符串
     * @return Date 转换后的日期
     * @throws ParseException 如果无法转换
     * @see #toDate(String, SimpleDateFormat)
     */
    public static Date toDate(String date, String dateformat)
            throws ParseException {
        return (dateformat == null) ? DateUtil.toDate(date)
                : DateUtil.toDate(date, new SimpleDateFormat(dateformat));
    }

    /**
     * 转换date为日期类型,尝试所有默认的日期格式
     *
     * @param date 日期字符串
     * @return Date 转换后的日期
     * @throws ParseException 如果无法转换
     * @see #toDate(String, String)
     */
    public static Date toDate(String date) throws ParseException {
        int i = 0;
        int len = DateUtil.ALL_FORMAT.length;
        while (i < len) {
            try {
                return DateUtil.toDate(date, DateUtil.ALL_FORMAT[i++]);
            } catch (ParseException e) {
            }
        }
        throw new ParseException("指定的日期/时间字符串[" + date + "]不能解析,请指定正确的格式!", 0);
    }

    /**
     * 将日期/时间date转换为指定的格式dateformat
     *
     * @param datet      Date 要转换日期
     * @param dateformat SimpleDateFormat 日期格式
     * @return String 转换后的日期/时间字符串
     */
    public static String getDate(Date date, SimpleDateFormat dateformat) {
        if (date == null)
            return null;
        if (dateformat == null)
            dateformat = new SimpleDateFormat(DateUtil.DATE_FORMAT);
        return dateformat.format(date);
    }

    /**
     * 将时间date转换为指定的格式dateformat
     *
     * @param date       Date 日期
     * @param dateformat String 日期时间格式
     * @return String 转换后的日期时间字符串
     * @throws ParseException 若转换出现错误,如:格式有误或者日期时间有误
     * @see #{@link #getDate(Date, SimpleDateFormat)}
     */
    public static String getDate(Date date, String dateformat) {
        if (dateformat == null)
            dateformat = DateUtil.DATE_FORMAT;
        return DateUtil.getDate(date, new SimpleDateFormat(dateformat));
    }

    /**
     * 将时间字符串date转换为指定的格式dateformat
     *
     * @param date       String 日期时间字符串
     * @param dateformat SimpleDateFormat 日期时间格式
     * @return String 转换后的日期时间字符串
     * @throws ParseException 若转换出现错误,如:格式有误或者日期时间有误
     * @see #{@link #getDate(Date, SimpleDateFormat)}
     */
    public static String getDate(String date, SimpleDateFormat dateformat)
            throws ParseException {
        return DateUtil.getDate(DateUtil.toDate(date), dateformat);
    }

    /**
     * 将日期/时间字符串date转换为指定的格式dateformat
     *
     * @param date       String 日期/时间字符串
     * @param dateformat String 日期/时间字符串
     * @return String 转换后的日期/时间字符串
     * @throws ParseException 若转换出现错误,如:格式有误或者日期/时间有误
     * @see #{@link #getDate(String, SimpleDateFormat)}
     */
    public static String getDate(String date, String dateformat)
            throws ParseException {
        if (dateformat == null)
            dateformat = DateUtil.DATE_FORMAT;
        return DateUtil.getDate(date, new SimpleDateFormat(dateformat));
    }

    /**
     * 将日期/时间字符串date转换为默认的日期格式
     *
     * @param date String 日期字符串
     * @return String 转换后的日期字符串
     * @throws ParseException 若转换出现错误,如:格式有误或者日期时间有误
     * @see DateTime#{@link #getDateTime(String, String)}
     * @see DateTime#{@link #DATE_FORMAT}
     */
    public static String getDate(String date) throws ParseException {
        return DateUtil.getDate(date, DateUtil.DATE_FORMAT);
    }

    /**
     * 将日期/时间date转换为默认的日期格式
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static String getDate(Date date) throws ParseException {
        return DateUtil.getDate(date, DateUtil.DATE_FORMAT);
    }

    /**
     * 将时间字符串datetime转换为默认的日期时间格式
     *
     * @param datetime String 日期时间字符串
     * @return String 转换后的日期时间字符串
     * @throws ParseException 若转换出现错误,如:格式有误或者日期时间有误
     * @see #{@link #getDate(String, String)}
     * @see #{@link #DATETIME_FORMAT}
     */
    public static String getDateTime(String datetime) throws ParseException {
        return DateUtil.getDate(datetime, DateUtil.DATETIME_FORMAT);
    }

    /**
     * 将时间datetime转换为默认的时间格式
     *
     * @param datetime
     * @return
     * @throws ParseException
     */
    public static String getDateTime(Date datetime) throws ParseException {
        return DateUtil.getDate(datetime, DateUtil.DATETIME_FORMAT);
    }

    /**
     * 将时间字符串datetime转换为默认的日期时间格式
     *
     * @param datetime String 日期时间字符串
     * @return String 转换后的日期时间字符串
     * @throws ParseException 若转换出现错误,如:格式有误或者日期时间有误
     * @see #{@link #getDate(String, String)}
     * @see #{@link #DATETIME_NO_SECOND_FORMAT}
     */
    public static String getDateTime_No_Second(String datetime)
            throws ParseException {
        return DateUtil.getDate(datetime, DateUtil.DATETIME_NO_SECOND_FORMAT);
    }

    /**
     * 取当前日期时间,指定日期时间格式dateformat
     *
     * @param datetimeformat String 日期字符串
     * @return String 转换后的日期字符串
     */
    public static String getNowDate(String dateformat) {
        return DateUtil.getDate(new Date(), dateformat);
    }

    public static String nowDate(String dateformat) {
        return DateUtil.getNowDate(dateformat);
    }

    /**
     * 取当前日期,默认的日期格式
     *
     * @return String 转换后的日期字符串
     */
    public static String getNowDate() {
        return DateUtil.getNowDate(DateUtil.DATE_FORMAT);
    }

    public static String nowDate() {
        return DateUtil.getNowDate();
    }

    /**
     * 取当前日期,默认的日期时间格式
     *
     * @return String 转换后的日期时间字符串
     */
    public static String getNowDateTime() {
        return DateUtil.getNowDate(DateUtil.DATETIME_FORMAT);
    }

    /**
     * 取当前日期,默认的日期时间格式
     *
     * @return String 转换后的日期字符串
     */
    public static String now() {
        return DateUtil.getNowDateTime();
    }

    public static String nowMs() {
        return DateUtil.getNowDate(DateUtil.DATETIME_MS_FORMAT);
    }

    /**
     * 返回当前年份
     *
     * @return
     */
    public static int getYear() {
        return get(Calendar.YEAR);
    }

    /**
     * 当前月份
     *
     * @return
     */
    public static int getMonth() {
        return get(Calendar.MONTH) + 1;
    }

    /**
     * 当前月份
     *
     * @return 当前月份两位，如06
     */
    public static String getMonthDouble() {
        String str = "0" + (get(Calendar.MONTH) + 1);
        return str.substring(str.length() - 2, str.length());
    }

    /**
     * 当前日
     *
     * @return
     */
    public static int getDay() {
        return get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 当前日
     *
     * @return 当前日两位，如06
     */
    public static String getDayDouble() {
        String str = "0" + get(Calendar.DAY_OF_MONTH);
        return str.substring(str.length() - 2, str.length());
    }

    /**
     * 时
     *
     * @return
     */
    public static int getHour() {
        return get(Calendar.HOUR_OF_DAY);
    }

    public static int get(int field) {
        return get(null, field);
    }

    /**
     * 得到日期date中的key数据段
     *
     * @param date
     * @param key  参考Calendar.get()的参数
     * @return
     */
    public static int get(Date date, int field) {
        Calendar c = Calendar.getInstance();
        c.setTime(date == null ? new Date() : date);
        return c.get(field);
    }

    /**
     * 在日期对象上增加天数等.
     *
     * @param date   要改变的日期
     * @param field  表示增加年/月/日/时/分/秒等
     * @param amount 可以为负数
     * @return
     */
    public static Date add(Date date, int field, int amount) {
        date = date == null ? new Date() : date;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(field, amount);
        return date = c.getTime();
    }

    /**
     * 在date上增加1天
     *
     * @param date
     * @return
     */
    public static Date addDay(Date date) {
        return addDays(date, 1);
    }

    public static Date addDays(Date date, int days) {
        return add(date, Calendar.DAY_OF_MONTH, days);
    }

    /**
     * 得到在当前日期基础上增加后的日期
     *
     * @param field
     * @param amount
     * @return
     */
    public static Date add(int field, int amount) {
        return add(null, field, amount);
    }

    public static Date set(Date date, int field, int amount) {
        date = ((date == null) ? new Date() : date);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(field, amount);
        return date = c.getTime();
    }

    public static Date set(int field, int amount) {
        return set(null, field, amount);
    }

    public static Date clear(Date date, int field) {
        date = ((date == null) ? new Date() : date);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.clear(field);
        return date = c.getTime();
    }

    /**
     * 判断指定的日期date与日期格式dateformat是否匹配
     *
     * @param date
     * @param dateformat
     * @return ture 如果date能解析为dataformat的日期格式
     */
    public static boolean isDate(String date, String dateformat) {
        Date d = null;
        try {
            d = DateUtil.toDate(date, dateformat);
        } catch (ParseException e) {
            // e.printStackTrace();
        }
        return d != null;
    }

    /**
     * date是否能解析为日期
     *
     * @param date
     * @return true 如果date为已知的日期格式
     * @see #isDate(String, String)
     */
    public static boolean isDate(String date) {
        return DateUtil.isDate(date, null);
    }

    /**
     * year, month, day是否日期
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static boolean isDate(int year, int month, int day) {
        if ((month < 0 || month > 12) || (day < 0 || day > 31)) {
            return false;
        }
        return day < getDays(year, month);
    }

    /**
     * 是否闰年
     *
     * @param year
     * @return
     */
    public static boolean isLeap(int year) {
        return (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0));
    }

    /**
     * 返回一个月的天数
     *
     * @param year
     * @param month
     * @return
     */
    public static int getDays(int year, int month) {
        return (month == 2) ? (isLeap(year) ? 29 : 28)
                : ((((month < 7) && (month % 2 == 0)) || ((month > 8) && (month % 2 == 1))) ? 30
                : 31);
    }

    /**
     * 取较小的日期
     *
     * @param date1
     * @param date2
     * @return
     */
    public static Date min(Date date1, Date date2) {
        Date d1 = date1 == null ? new Date() : date1;
        Date d2 = date2 == null ? new Date() : date2;
        return d1.compareTo(d2) < 0 ? date1 : date2;
    }

    /**
     * 取较大的日期
     *
     * @param date1
     * @param date2
     * @return
     */
    public static Date max(Date date1, Date date2) {
        Date d1 = date1 == null ? new Date() : date1;
        Date d2 = date2 == null ? new Date() : date2;
        return d1.compareTo(d2) < 0 ? date2 : date1;
    }

    /**
     * date1与date2相差的天数,不足一天（>0）按一天计算.
     *
     * @param date1
     * @param date2
     * @return
     */
    public static long getDays(Date date1, Date date2) {
        Date d1 = date1 == null ? new Date() : date1;
        Date d2 = date2 == null ? new Date() : date2;
        double day = d1.getTime() - d2.getTime();
        return Math.round(day / DAY);
    }

    /**
     * 获取字符串日期的秒数
     *
     * @param DATE
     * @return int
     */
    public static int getIntSecondFromDate() {
        Date date = new Date();
        // String转换为日期类型并得到秒数long型
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dat = sdf.format(date);
        try {
            long second = sdf.parse(dat).getTime() / 1000;
            return (int) second;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public static String trunsLogTime(int time) {
        // long转换为日期类型
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long millSec = time;
        Date date = new Date(millSec * 1000);
        return sdf.format(date);
    }

    public static void main(String[] args) throws ParseException {
        int time = getIntSecondFromDate();
        System.out.println(time);
    }
}
