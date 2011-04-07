package com.ecollege.android.util;

import java.util.Calendar;
import java.util.Date;

import android.text.format.DateFormat;

public class DateTimeUtil {

	public static String getShortFriendlyDate(Calendar cal) {
		return getShortFriendlyDate(cal.getTime());
	}
	
	public static String getShortFriendlyDate(long timeInMillis) {
		return getShortFriendlyDate(new Date(timeInMillis));
	}
	
	public static String getShortFriendlyDate(Date dt) {
		String dtStr = DateFormat.format("MMM d, yyyy", dt).toString();
		
		Date today = getToday();
		String todayStr = DateFormat.format("MMM d, yyyy", today).toString();
		
		if (dtStr.equals(todayStr)) {
			return "Today";
		}
		
		Date yesterday = getYesterday();
		String yesterdayStr = DateFormat.format("MMM d, yyyy", yesterday).toString();
		
		if (dtStr.equals(yesterdayStr)) {
			return "Yesterday";
		}
		
		if (dt.getYear() != today.getYear()) {
			return dtStr;
		} else {
			return DateFormat.format("MMM d", dt).toString();
		}
	}

	public static String getLongFriendlyDate(Calendar cal) {
		return getLongFriendlyDate(cal.getTime());
	}
	
	public static String getLongFriendlyDate(long timeInMillis) {
		return getLongFriendlyDate(new Date(timeInMillis));
	}
	
	public static String getLongFriendlyDate(Date dt) {
		String dtStr = DateFormat.format("MMM d, yyyy", dt).toString();
		
		Date today = getToday();
		String todayStr = DateFormat.format("MMM d, yyyy", today).toString();
		
		if (dtStr.equals(todayStr)) {
			return "Today " + DateFormat.format("h:mm aa", dt);
		}
		
		Date yesterday = getYesterday();
		String yesterdayStr = DateFormat.format("MMM d, yyyy", yesterday).toString();
		
		if (dtStr.equals(yesterdayStr)) {
			return "Yesterday " + DateFormat.format("h:mm aa", dt);
		}
		
		if (dt.getYear() != today.getYear()) {
			return DateFormat.format("MMM d, yyyy h:mm aa", dt).toString();
		} else {
			return DateFormat.format("MMM d h:mm aa", dt).toString();
		}
	}
	
	public static Date getToday() {
		Date now = new Date();
		Date today = new Date(now.getYear(),now.getMonth(),now.getDate()); //beginning of today
		return today;
	}
	
	public static Date getYesterday() {
		Date today = getToday();
		Date yesterday = new Date(today.getYear(),today.getMonth(),today.getDate()-1); //beginning of yesterday
		return yesterday;
	}
	
}
