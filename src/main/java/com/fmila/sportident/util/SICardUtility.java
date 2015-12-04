package com.fmila.sportident.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.fmila.sportident.DownloadException;
import com.fmila.sportident.ECardType;
import com.fmila.sportident.bean.Punch;

public final class SICardUtility {

	private SICardUtility() {
	}

	public static final long INVALID_TIME = 61166000L;

	public static Punch readTCardPunch(byte[] bytes, int index, long sortcode, Date evtZero) {
		Punch punch = new Punch();
		/**
		8 byte punching record

		indirect and direct addressing mode
		record structure: CN-STD1-STD0-DATE1-DATE0-PTH-PTL-MS
		CN - control station code number, 0...255
		STD1             bit 17…9 -  Part of 24Bit SI-Station ID
		STD0             bit  8…2  -  Part of 24Bit SI-Station ID
		DATE1  bit 7-6  bit 1-0   -  Part of 24Bit SI-Station ID
		            bit 5-2  4bit year  0-16 Part of year
		            bit 1-0   bit 3-2 Part of 4bit Month 1-12
		DATE0  bit 7-6   bit 1-0 Part of 4bit Month 1-12
		            bit 5-1 5bit of Day in Month 1-31
		            bit 0 - am/pm halfday
		PTH, PTL - 12h binary punching time
		MS       8bit 1/256 of seconds
		**/
		long code = ByteUtility.getLongFromByte(bytes[index]); // CN
		punch.setSortCode(sortcode);
		punch.setControlNo(Long.toString(code));
		
		return punch;
	}
	
	public static Punch readV6Punch(byte[] bytes, int index, long sortcode, Date evtZero) throws DownloadException {
		if (bytes == null || bytes.length < 4) {
			throw new DownloadException("SI-Card punch must be 4 bytes.");
		}

		Punch punch = new Punch();

		/*
		 * record structure: PTD - CN - PTH - PTL CN - control station code
		 * number, 0...255 or subsecond value PTD - day of week / halfday bit 0
		 * - am/pm bit 3...1 - day of week, 000 = Sunday, 110 = Saturday bit
		 * 5...4 - week counter 0…3, relative bit 7...6 - control station code
		 * number high (...1023) (reserved) punching time PTH, PTL - 12h binary
		 * 
		 * 1 subsecond value only for “start” and “finish” possible new from
		 * sw5.49: bit7=1 in PTD-byte indicates a subsecond value in CN byte
		 * (use always code numbers <256 for start/finish)
		 */

		long byte0 = ByteUtility.getLongFromByte(bytes[index]); // PTD
		long code = ByteUtility.getLongFromByte(bytes[index + 1]); // CN
		Long punchTime = ByteUtility.getLongFromBytes(bytes[index + 2], bytes[index + 3]) * 1000L; // PTH,
																									// PTL
		if (punchTime == INVALID_TIME) {
			punchTime = null;
		} else if ((byte0 & 1) != 0) { // PTD am/pm (am=0, pm=1)
			punchTime += 43200000L; // 12 hours in milliseconds
		}

		punch.setSortCode(sortcode);
		punch.setControlNo(Long.toString(code));
		punch.setRawTime(alignToEvtZeroTime(punchTime, evtZero));

		return punch;
	}

	public static Punch readV5Punch(byte[] data, int pos, int sortcode, Date evtZero) {
		Long time;
		long code = ByteUtility.getLongFromByte(data[pos]);
		time = ByteUtility.getLongFromBytes(data[1 + pos], data[2 + pos]) * 1000L;
		if (time == SICardUtility.INVALID_TIME) {
			time = null;
		}
		Punch punch = new Punch();
		punch.setControlNo("" + code);
		punch.setSortCode(Long.valueOf(sortcode));
		punch.setRawTime(alignToEvtZeroTimeV5(time, evtZero));
		return punch;
	}

	private static Long alignToEvtZeroTimeV5(Long time, Date evtZero) {
		time = SICardUtility.alignToEvtZeroTime(time, evtZero);
		if (time != null && time < 0) {
			// siV5 is 12h only
			return time + 12 * 60 * 60 * 1000; // 12h
		} else {
			return time;
		}
	}

	private static Long alignToEvtZeroTime(Long time, Date evtZero) {
		if (time == null) {
			return null;
		}
		Long evtZeroHoursMinsSecsInMilliSecs = getDateDifferenceInMilliSeconds(truncDate(evtZero), evtZero);
		return time - evtZeroHoursMinsSecsInMilliSecs;
	}

	private static Date truncDate(Date d) {
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

	private static Long getDateDifferenceInMilliSeconds(Date zeroTime, Date overallTime) {
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
	
	public static ECardType getType(String eCardNo) throws DownloadException {

		// SI-Card5 SI-Card6 SI-Card6* SI-Card8 SI-Card9 pCard tCard
		// (upgrade to SI-6* possible)
		// card number range 1 500'000 16'711'680 2'000'000 1'000'000 4'000'000
		// 6'000'000
		// 499'999 999'999 16'777'215 2'999'999 1'999'999 4'999'999 6'999'999

		Long eCardId = null;
		try {
			eCardId = Long.parseLong(eCardNo);
		} catch (NumberFormatException e) {
			throw new DownloadException("InvalidSICardNumber");
		}

		if (eCardId >= 0 && eCardId <= 499999) {
			return ECardType.SICARD5;
		} else if (eCardId >= 500000 && eCardId <= 999999) {
			return ECardType.SICARD6;
		}
		// special case: SI Cards for OL WM 2003
		else if (eCardId >= 2003000 && eCardId <= 2003799) {
			return ECardType.SICARD6;
		} else if (eCardId >= 16711680 && eCardId <= 16777215) {
			return ECardType.SICARD6Star;
		} else if (eCardId >= 2000000 && eCardId <= 2999999) {
			return ECardType.SICARD8;
		} else if (eCardId >= 1000000 && eCardId <= 1999999) {
			return ECardType.SICARD9;
		} else if (eCardId >= 4000000 && eCardId <= 4999999) {
			return ECardType.pCARD;
		} else if (eCardId >= 6000000 && eCardId <= 6999999) {
			return ECardType.tCARD;
		} else if (eCardId >= 7000001 && eCardId <= 7999999) {
			return ECardType.SICARD10;
		} else if (eCardId >= 8000001 && eCardId <= 8999999) {
			return ECardType.SIAC1;
		} else if (eCardId >= 9000001 && eCardId <= 9999999) {
			return ECardType.SICARD11;
		} else {
			throw new DownloadException("UnknownSICardType" + ": " + eCardNo);
		}
	}

	private static long getWeekday(long byte0) {
		// TODO use this method
		String bin = StringUtility.lpad(Long.toBinaryString(byte0), "0", 8);
		String weekday = StringUtility.substring(bin, 4, 3);
		long day = Long.parseLong(weekday, 2) + 1; // add +1 for java notation
		System.out.println("Weekday: " + day);

		String week = StringUtility.substring(bin, 2, 2);
		System.out.println("Week: " + week);

		return day;
	}
}
