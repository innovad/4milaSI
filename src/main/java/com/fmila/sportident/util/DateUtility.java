package com.fmila.sportident.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateUtility {

	public static String format(Date d, String pattern) {
		if (d == null || pattern == null || pattern.length() == 0) {
			return "";
		}
		Locale loc = Locale.getDefault();
		return new SimpleDateFormat(pattern, loc).format(d);
	}

	public static Date parse(String s, String pattern) {
		if (s == null) {
			return null;
		}
		try {
			Locale loc = Locale.getDefault();
			return new SimpleDateFormat(pattern, loc).parse(s);
		} catch (ParseException e) {
			throw new IllegalArgumentException("parse(\"" + s + "\",\"" + pattern + "\") failed", e);
		}
	}
	
	  public static Date addMilliSeconds(Date baseDate, Long ms) {
		    Date resultDate = null;

		    if (baseDate != null && ms != null) {
		      resultDate = new Date(baseDate.getTime() + ms);

		      // if the dates are not in the same timezone (e.g. CET and CEST), add the difference
		      GregorianCalendar baseDateCal = new GregorianCalendar();
		      baseDateCal.setTime(baseDate);
		      int baseDateOffset = baseDateCal.get(Calendar.ZONE_OFFSET) + baseDateCal.get(Calendar.DST_OFFSET);

		      GregorianCalendar resultDateCal = new GregorianCalendar();
		      resultDateCal.setTime(resultDate);
		      int resultTimeOffset = resultDateCal.get(Calendar.ZONE_OFFSET) + resultDateCal.get(Calendar.DST_OFFSET);

		      if (baseDateOffset != resultTimeOffset) {
		        ms = ms - (resultTimeOffset - baseDateOffset);
		      }

		      resultDate = new Date(baseDate.getTime() + ms);
		    }
		    return resultDate;
		  }	
}
