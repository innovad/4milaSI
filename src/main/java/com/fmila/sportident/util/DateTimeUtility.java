package com.fmila.sportident.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateTimeUtility {

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

	public static Date addMilliSeconds(Date baseDate, Long milliseconds) {
		Date resultDate = null;
		Long ms = milliseconds;

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

	public static Long alignDateOfTime(Long millsecondsFromCard, Date baseDateFromCard, Date givenZeroDate) {
		if (millsecondsFromCard == null) {
			return null;
		}

		if (givenZeroDate == null) {
			// try to set absolute date/time, either from sysdate or from card
			return DateTimeUtility.addMilliSeconds(truncDate(baseDateFromCard == null ? new Date() : baseDateFromCard), millsecondsFromCard).getTime();
		} else {
			// set relative time to given zero date/time
			long baseDateCardDifference = 0;
			if (baseDateFromCard != null) {
				// if days from dateFromCard != days from evtZero => add difference
				long daysDifference = (truncDate(baseDateFromCard).getTime() - truncDate(givenZeroDate).getTime()) / 86400 / 1000;
				baseDateCardDifference = daysDifference * 86400 * 1000;
			}
			Long evtZeroHoursMinsSecsInMilliSecs = getDateDifferenceInMilliSeconds(truncDate(givenZeroDate), givenZeroDate);
			return millsecondsFromCard + baseDateCardDifference - evtZeroHoursMinsSecsInMilliSecs;
		}
	}

	public static Date truncDate(Date d) {
		if (d == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		truncCalendar(c);
		return c.getTime();
	}

	private static void truncCalendar(Calendar c) {
		if (c == null) {
			return;
		}
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
	}

	public static Long getDateDifferenceInMilliSeconds(Date zeroTime, Date overallTime) {
		Long timeDiff = null;

		if (overallTime != null && zeroTime != null) {

			timeDiff = (overallTime.getTime() - zeroTime.getTime());

			// if the dates are not in the same timezone (e.g. CET and CEST),
			// subtract the difference
			GregorianCalendar zeroTimeCal = new GregorianCalendar();
			zeroTimeCal.setTime(zeroTime);
			int zeroTimeOffset = zeroTimeCal.get(Calendar.ZONE_OFFSET) + zeroTimeCal.get(Calendar.DST_OFFSET);

			GregorianCalendar overallTimeCal = new GregorianCalendar();
			overallTimeCal.setTime(overallTime);
			int overallTimeOffset = overallTimeCal.get(Calendar.ZONE_OFFSET) + overallTimeCal.get(Calendar.DST_OFFSET);

			if (zeroTimeOffset != overallTimeOffset) {
				timeDiff = timeDiff + (overallTimeOffset - zeroTimeOffset);
			}
		}

		return timeDiff;
	}
}
