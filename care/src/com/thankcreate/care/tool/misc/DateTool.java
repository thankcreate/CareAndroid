package com.thankcreate.care.tool.misc;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTool {
	public static long convertDateToLong(Date date)
	{
		if(date == null)
			return -1;
		return date.getTime();		
	}
	
	public static Date convertLongToDate(Long lDate)
	{
		return new Date(lDate);
	}
	
	public static String convertDateToStringInShow(Date date)
	{
		if(date == null)
			return "";
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd   HH:mm:ss", Locale.ENGLISH);
		return sdf.format(date);		
	}
	
	public static String getDateSig(Date date)
	{
		if(date == null)
			return "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmmss", Locale.ENGLISH);
		return sdf.format(date);	
	}
}
