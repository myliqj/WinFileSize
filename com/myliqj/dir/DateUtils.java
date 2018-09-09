package com.myliqj.dir;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	/** 标准日期格式 */
	public static final String PATTERN_DATE = "yyyy-MM-dd";
	/** 标准时间格式 */
	public static final String PATTERN_TIME = "HH:mm:ss";
	/** 标准日期时间格式，精确到�?yyyy-MM-dd HH:mm */
	/** 标准日期时间格式，精确到�? yyyy-MM-dd HH:mm:ss */
	/** 标准日期时间格式，精确到毫秒 */
	public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss:SSS";
//	public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
	
	/** 标准日期（不含时间）格式化器 */
	//public static final java.text.SimpleDateFormat formater_date = new java.text.SimpleDateFormat(PATTERN_DATE);
	private static ThreadLocal<SimpleDateFormat> NORM_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>(){
		synchronized protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(PATTERN_DATE);
		};
	};
	/** 标准时间格式化器 */
	//public static final java.text.SimpleDateFormat formater_time = new java.text.SimpleDateFormat(PATTERN_TIME);
	private static ThreadLocal<SimpleDateFormat> NORM_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>(){
		synchronized protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(PATTERN_TIME);
		};
	};
	/** 标准日期时间格式化器 */
	//public static final java.text.SimpleDateFormat formater_datetime = new java.text.SimpleDateFormat(PATTERN_DATETIME);
	private static ThreadLocal<SimpleDateFormat> NORM_DATETIME_FORMAT = new ThreadLocal<SimpleDateFormat>(){
		synchronized protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(PATTERN_DATETIME);
		};
	};
	/**
	 * 格式 yyyy-MM-dd
	 * 
	 * @param date 被格式化的日�?
	 * @return 格式化后的字符串
	 */
	public static String formatDate(Date date) {
		return NORM_DATE_FORMAT.get().format(date);
	}
	/**
	 * 格式 HH:mm:ss
	 * 
	 * @param time 被格式化的时�?
	 * @return 格式化后的字符串
	 */
	public static String formatTime(Date date) {
		return NORM_TIME_FORMAT.get().format(date);
	}
	/**
	 * 格式 yyyy-MM-dd HH:mm:ss:SSS
	 * 
	 * @param date 被格式化的日�?
	 * @return 格式化后的日�?
	 */
	public static String formatDateTime(Date date) {
		return NORM_DATETIME_FORMAT.get().format(date);
	}
}
