package com.slightsite.app.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A static class, global access for how to handle with date format.
 *
 *
 */
public class DateTimeStrategy {
	
	private static Locale locale;
	private static SimpleDateFormat dateFormat;
	
	private DateTimeStrategy() {
		// Static class
	}
	
	/**
	 * Set local of time for use in application.
	 * @param lang Language.
	 * @param reg Region.
	 */
	public static void setLocale(String lang, String reg) {
		locale = new Locale(lang, reg);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
	}
	
	/**
	 * Sets current time format.
	 * @param date date of this format.
	 * @return current time format.
	 */
	public static String format(String date) {
		return dateFormat.format(Calendar.getInstance(locale).getTime());
	}
	
	/**
	 * Returns current time.
	 * @return current time.
	 */
	public static String getCurrentTime() {
		return dateFormat.format(Calendar.getInstance().getTime()).toString();
	}
	
	/**
	 * Convert the calendar format to date format for adapt in SQL.
	 * @param instance calendar .
	 * @return date format.
	 */
	public static String getSQLDateFormat(Calendar instance) {
		return dateFormat.format(instance.getTime()).toString().substring(0,10);
	}

	public static String parseDate(String time, String outputPattern) {
		if (!time.contains(":")) {
			time += " 00:00:00";
		}
		String inputPattern = "yyyy-MM-dd HH:mm:ss";
		String[] separated = time.split("-");
		if (separated[0].length() == 2) {
			inputPattern = "dd-MM-yyyy HH:mm:ss";
		}
		SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
		SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

		Date date = null;
		String str = null;

		try {
			date = inputFormat.parse(time);
			str = outputFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return str;
	}
}
