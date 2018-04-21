package com.cmcciot.platform.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTools {

	/**
	 * 日期格式化
	 *
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(Date date, String pattern) {
		String returnValue = "";
		if (date != null) {
			SimpleDateFormat df = new SimpleDateFormat(pattern);
			returnValue = df.format(date);
		}
		return (returnValue);
	}

	public static String format(String srcDate, String srcFmt, String targetFmt) {
		SimpleDateFormat srcSdf = new SimpleDateFormat(srcFmt);
		try {
			Date date = srcSdf.parse(srcDate);
			SimpleDateFormat targetSdf = new SimpleDateFormat(targetFmt);
			String result = targetSdf.format(date);
			return result;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 将字符串类型的日期转化成Date类型
	 *
	 * @param strDate
	 * @param pattern
	 * @return
	 */
	public static Date parse(String strDate, String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern);
		Date date = null;
		try {
			date = df.parse(strDate);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取时间戳
	 */
	public static String getTimeString() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
		Calendar calendar = Calendar.getInstance();
		return df.format(calendar.getTime());
	}

	public static String getTimeString1() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		return df.format(calendar.getTime());
	}
	
	public static String getTimeString(String dateStr){  
		if(dateStr == null || "".equals(dateStr)) return null;
        long times = parse(dateStr, "yyyy-MM-dd").getTime()/1000;  
        
        return String.valueOf(times);
    } 

	/**
	 * 获取日期年份
	 *
	 * @param date
	 *            日期
	 * @return
	 */
	public static String getYear(Date date) {
		return format(date, "yyyy-MM-dd HH:mm:ss").substring(0, 4);
	}

	/**
	 * 按默认格式的字符串距离今天的天数
	 *
	 * @param date
	 *            日期字符串
	 * @return
	 */
	public static int countDays(String date) {
		long t = Calendar.getInstance().getTime().getTime();
		Calendar c = Calendar.getInstance();
		c.setTime(parse(date, "yyyy-MM-dd HH:mm:ss"));
		long t1 = c.getTime().getTime();
		return (int) (t / 1000 - t1 / 1000) / 3600 / 24;
	}

	/**
	 * 按默认格式的字符串距离今天的秒数
	 *
	 * @param date
	 *            日期字符串
	 * @return
	 */
	public static int countSeconds(String date) {
		long t = Calendar.getInstance().getTime().getTime();
		Calendar c = Calendar.getInstance();
		c.setTime(parse(date, "yyyy-MM-dd HH:mm:ss"));
		long t1 = c.getTime().getTime();
		return (int) (t / 1000 - t1 / 1000);
	}

	/**
	 * 按用户格式字符串距离今天的天数 日期比今天大时，返回负值
	 * 
	 * @param date
	 *            日期字符串
	 * @param format
	 *            日期格式
	 * @return
	 */
	public static int countDays(String date, String format) {
		// t是当前时间
		long t = Calendar.getInstance().getTime().getTime();
		Calendar c = Calendar.getInstance();
		c.setTime(parse(date, format));
		// t1是传入时间
		long t1 = c.getTime().getTime();
		return (int) (t / 1000 - t1 / 1000) / 3600 / 24;
	}

	/**
	 * 获取某天凌晨时间 <一句话功能简述> <功能详细描述>
	 *
	 * @param date
	 * @return Date [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static Date getMorning(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		return calendar.getTime();
	}

	/**
	 * 获取字符串日期的秒数
	 *
	 * @param DATE
	 * @return int
	 */
	public static int getIntSecondFromDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
		Calendar calendar = Calendar.getInstance();
		String dat = df.format(calendar.getTime());
		try {
			long second = df.parse(dat).getTime() / 1000;
			return (int) second;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 根据传入时间 获取秒 <功能详细描述>
	 *
	 * @param value
	 *            分钟值
	 * @return int [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static int getSecond(String value) {
		if (value==null||value.equals("")) {
			return 0;
		}
		String secondValue = value.split("\\.")[0];
		int second = 0;
		try {
			second = Integer.valueOf(secondValue) * 60;
		} catch (Exception e) {
			return 0;
		}
		return second;
	}

	/**
	 * 获取多少天之后的日期 <功能详细描述>
	 *
	 * @param startDate
	 * @param days
	 * @return String [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static String getAfterDate(String startDate, int days) {
		Date date;
		Calendar cal = Calendar.getInstance();
		try {
			date = (new SimpleDateFormat("yyyy-MM-dd")).parse(startDate);

			cal.setTime(date);
			cal.add(Calendar.DATE, days);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return (new SimpleDateFormat("yyyy-MM-dd")).format(cal.getTime());
	}

	/**
	 * 获取多少天之后的日期的毫秒数 <功能详细描述>
	 *
	 * @param startDate
	 * @param days
	 * @return String [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static long getAfterDateMillis(String startDate, int days) {
		Date date;
		Calendar cal = Calendar.getInstance();
		try {
			date = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(startDate);

			cal.setTime(date);
			cal.add(Calendar.DATE, days);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return cal.getTimeInMillis();
	}

	/**
	 * 日期比较 date1 > date2返回true.否则返回false <功能详细描述>
	 *
	 * @param startDate
	 * @param endDate
	 * @return String [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean compareDate(String startDate, String endDate, String format) {
		Date newDate = strDateToDate(startDate, format);
		Date newDate2 = strDateToDate(endDate, format);
		return compareDate(newDate, newDate2);
	}

	/**
	 * 若时间相等，返回true，否则为false <功能详细描述>
	 *
	 * @param startDate
	 * @param endDate
	 * @return String [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean isDateEqual(String startDate, String endDate, String format) {
		Date newDate = strDateToDate(startDate, format);
		Date newDate2 = strDateToDate(endDate, format);
		return isDateEqual(newDate, newDate2);
	}

	
	/**
     * yyyy-MM-dd HH:mm:ss 转   yyyyMMddHHmmss
     * <功能详细描述>
     * @param dateStr
     * @return [参数说明]
     * 
     * @return String [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String dateForm(String dateStr)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = null;
        try
        {
            Date temp = sdf.parse(dateStr);
            date = sdf2.format(temp);
        }
        catch (ParseException e)
        {
        }
        return date;
    }
	
	
	/**
	 * 与当前时间进行比较,若大于当前时间，返回true <功能详细描述>
	 *
	 * @param date
	 * @return boolean [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean compareDate(String date, String format) {
		Date dateBeCompared = strDateToDate(date, format);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.format(new Date());// new Date()为获取当前系统时间
		Date nowDate = strDateToDate(df.format(new Date()), format);
		return compareDate(dateBeCompared, nowDate);
	}

	/**
	 * 将字符转换为日期类型 <功能详细描述>
	 *
	 * @param strDate
	 * @param sourceFormat
	 * @return Date [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static Date strDateToDate(String strDate, String sourceFormat) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(sourceFormat);
		Date date = null;
		try {
			date = dateFormat.parse(strDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 判断时间大小 date1 > date2返回true.否则返回false <功能详细描述>
	 *
	 * @param date1
	 * @param date2
	 * @return boolean [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean compareDate(Date date1, Date date2) {
		if (date1.compareTo(date2) > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断时间是否相等 <功能详细描述>
	 *
	 * @param date1
	 * @param date2
	 * @return boolean [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean isDateEqual(Date date1, Date date2) {
		if (0 == date1.compareTo(date2)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取当前时间 <功能详细描述>
	 *
	 * @return String [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static String now() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(new Date());
	}

	public static void main(String[] args) {
		System.out.println(getTimeString("2017-06-27 00:00:00"));
	}

	/**
	 * 功能：时间增加分钟数。
	 *
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date addMinutes(Date date, int minutes) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + minutes);
		return new Date(c.getTimeInMillis());
	}

	// public static void main(String[] args) {
	// Date date = new Date();
	// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//
	// System.out.println(df.format(date));
	// System.out.println(df.format(DateTools.addMinutes(date, 45)));
	//
	// System.out.println(now());
	// }

	/**
	 * 获取当前时间 时间格式为 yyyy-mm-dd hh:mm:ss <功能详细描述>
	 * 
	 * @param calendar
	 * @return [参数说明]
	 * 
	 * @return String [返回类型说明]
	 * @exception throws
	 *                [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static String transCalendar() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE) + " 00:00:00";
	}

	/**
	 * 获取几个月后的时间 <功能详细描述>
	 * 
	 * @param monthNum
	 *            月数
	 * @return [参数说明]
	 * 
	 * @return String [返回类型说明]
	 * @exception throws
	 *                [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static String getAfterSomeMonthDate(int monthNum) {
		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + monthNum);
		return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE) + " 00:00:00";
	}
	/**
     * 获取当前时间格式yyyyMMddHHmmss
     * <功能详细描述>
     *
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String nowyyyyMMddHHmmss() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(new Date());
    }

    /**
     * 获取当前时间戳
     * <功能详细描述>
     * 
     * @return String [返回类型说明]
     * @throws throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static String getTimeStamp(){
    	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();
        return df.format(calendar.getTime());
    }
	/**
	 * 获取当前时间 <功能详细描述>
	 *
	 * @return String [返回类型说明]
	 * @throws throws
	 *             [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static String getNowByYMD() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(new Date());
	}
	 public static String getAfterSomeMonthFromDate(Date date,int monthNum)
	    {
	        Calendar calendar = Calendar.getInstance();
	        calendar.setTime(date);
	        calendar.set(Calendar.MONTH,
	                calendar.get(Calendar.MONTH)
	                        + monthNum);
	        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH)+1)
	                + "-" + calendar.get(Calendar.DATE)+" 00:00:00";
	    }

}
