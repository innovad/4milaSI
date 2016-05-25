package com.fmila.sportident.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.fmila.sportident.DownloadException;
import com.fmila.sportident.SICardType;
import com.fmila.sportident.bean.Punch;

public final class SICardUtility {

	public static final long INVALID_TIME = 61166000L;

	private SICardUtility() {
	}

	/**
	 * read 8 byte punching record
	 * 
	 * indirect and direct addressing mode record structure: CN-STD1-STD0-DATE1-DATE0-PTH-PTL-MS<br>
	 * <br>
	 * 00 CN - control station code number, 0...255 <br>
	 * 08 STD1 bit 17-9 - Part of 24Bit SI-Station ID <br>
	 * 16 STD0 bit 8-2 - Part of 24Bit SI-Station ID <br>
	 * 24 DATE1 bit 7-6 bit 1-0 - Part of 24Bit SI-Station ID bit 5-2 4bit year 0-16 Part of year bit 1-0 bit 3-2 Part of 4bit Month 1-12 <br>
	 * 32 DATE0 bit 7-6 bit 1-0 Part of 4bit Month 1-12 bit 5-1 5bit of Day in Month 1-31 bit 0 - am/pm halfday <br>
	 * 40 PTH, 48 PTL - 12h binary punching time <br>
	 * 56 MS 8bit 1/256 of seconds
	 * 
	 **/
	public static Punch readTCardPunch(byte[] bytes, int index, long sortcode, Date evtZero) {
		Punch punch = new Punch();
		long code = ByteUtility.getNumberFromBits(bytes, index * 8, 8); // CN

		// calculate time
		Long punchTime = ByteUtility.getLongFromBytes(bytes[index + 5], bytes[index + 6]) * 1000L; // PTH - PTL
		if (punchTime == INVALID_TIME) {
			punchTime = null;
		} else if (ByteUtility.getBit(bytes[index + 4], 0) == 1) { // am/pm (am=0, pm=1)
			punchTime += 43200000L; // 12 hours in milliseconds
		}
		if (punchTime != null) {
			punchTime += ByteUtility.getNumberFromBits(bytes, index * 8 + 7, 8);
		}

		// calculate date
		long month = ByteUtility.getNumberFromBits(bytes, index * 8 + 30, 4) - 1; // java months start with 0
		long day = ByteUtility.getNumberFromBits(bytes, index * 8 + 34, 5);
		GregorianCalendar greg = new GregorianCalendar();
		greg.setTime(evtZero == null ? new Date() : evtZero);
		long year = greg.get(Calendar.YEAR);
		if (greg.get(Calendar.MONTH) < month && greg.get(Calendar.DAY_OF_MONTH) < day) {
			year = year - 1;
		}
		greg.set((int) year, (int) month, (int) day);

		punch.setSortCode(sortcode);
		punch.setControlNo(Long.toString(code));
		punch.setRawTime(DateTimeUtility.alignDateOfTime(punchTime, greg.getTime(), evtZero));

		if (punchTime != null) {
			System.out.println(code);
			System.out.println(DateTimeUtility.format(new Date(punch.getRawTime()), "dd-MM-yyyy HH:mm:ss.SSS"));
		}

		return punch;
	}

	/**
	 * read 4 byte punching record
	 * 
	 * record structure: PTD - CN - PTH - PTL<br>
	 * <br>
	 * CN - control station code number, 0...255 or subsecond value<br>
	 * PTD
	 * - day of week / halfday
	 * bit 0 - am/pm
	 * bit 3...1 - day of week, 000 = Sunday, 001 = Monday, 010 = Tuesday, 011 = Wednesday, ... , 110 = Saturday
	 * bit 5...4 - week counter 0-3, relative
	 * bit 7...6 - control station code number high (...1023) (reserved) punching time<br>
	 * PTH, PTL - 12h binary<br>
	 * <br>
	 * 1 subsecond value only for "start" and "finish" possible new from sw5.49: bit7=1 in PTD-byte indicates a subsecond value in CN byte (use always code numbers <256 for start/finish)<br>
	 **/
	public static Punch readV6Punch(byte[] bytes, int index, long sortcode, Date evtZero) throws DownloadException {
		if (bytes == null || bytes.length < 4) {
			throw new DownloadException("SI-Card punch must be 4 bytes.");
		}

		Punch punch = new Punch();

		byte byte0 = bytes[index]; // PTD
		long code = ByteUtility.getLongFromByte(bytes[index + 1]); // CN
		Long punchTime = ByteUtility.getLongFromBytes(bytes[index + 2], bytes[index + 3]) * 1000L; // PTH, PTL
		if (punchTime == INVALID_TIME) {
			punchTime = null;
		}
		if (ByteUtility.getBit(byte0, 0) == 1) { // PTD am/pm (am=0, pm=1)
			punchTime += 43200000L; // 12 hours in milliseconds
		}
		
		// analyze day of week
		int cardWeekday = getWeekday(ByteUtility.getBit(byte0, 3), ByteUtility.getBit(byte0, 2), ByteUtility.getBit(byte0, 1));
		GregorianCalendar greg = new GregorianCalendar();
		greg.setTime(evtZero == null ? new Date() : evtZero);
		int currentWeekday = greg.get(Calendar.DAY_OF_WEEK);
		int offset = currentWeekday - cardWeekday;
		if (offset < 0) {
			offset += 7;
		}
		greg.add(Calendar.DAY_OF_YEAR, -offset);
		
		punch.setSortCode(sortcode);
		punch.setControlNo(Long.toString(code));
		punch.setRawTime(DateTimeUtility.alignDateOfTime(punchTime, greg.getTime(), evtZero));

		return punch;
	}

	public static Punch readV5Punch(byte[] data, int pos, int sortcode, Date evtZero) {
		Long time;
		long code = ByteUtility.getLongFromByte(data[pos]);
		time = ByteUtility.getLongFromBytes(data[1 + pos], data[2 + pos]) * 1000L;
		if (time == INVALID_TIME) {
			time = null;
		}
		Punch punch = new Punch();

		Long rawTime = DateTimeUtility.alignDateOfTime(time, null, evtZero);
		if (rawTime != null && rawTime < 0) {
			// siV5 is 12h only
			rawTime = rawTime + 12 * 60 * 60 * 1000; // 12h
		}

		punch.setControlNo("" + code);
		punch.setSortCode(Long.valueOf(sortcode));
		punch.setRawTime(rawTime);

		return punch;
	}

	public static SICardType getType(String eCardNo) throws DownloadException {

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
			return SICardType.SICARD5;
		} else if (eCardId >= 500000 && eCardId <= 999999) {
			return SICardType.SICARD6;
		}
		// special case: SI Cards for OL WM 2003
		else if (eCardId >= 2003000 && eCardId <= 2003799) {
			return SICardType.SICARD6;
		} else if (eCardId >= 16711680 && eCardId <= 16777215) {
			return SICardType.SICARD6Star;
		} else if (eCardId >= 2000000 && eCardId <= 2999999) {
			return SICardType.SICARD8;
		} else if (eCardId >= 1000000 && eCardId <= 1999999) {
			return SICardType.SICARD9;
		} else if (eCardId >= 4000000 && eCardId <= 4999999) {
			return SICardType.pCARD;
		} else if (eCardId >= 6000000 && eCardId <= 6999999) {
			return SICardType.tCARD;
		} else if (eCardId >= 7000001 && eCardId <= 7999999) {
			return SICardType.SICARD10;
		} else if (eCardId >= 8000001 && eCardId <= 8999999) {
			return SICardType.SIAC1;
		} else if (eCardId >= 9000001 && eCardId <= 9999999) {
			return SICardType.SICARD11;
		} else {
			throw new DownloadException("UnknownSICardType" + ": " + eCardNo);
		}
	}

	private static int getWeekday(int bit0, int bit1, int bit2) {
		if (bit0 == 0 && bit1 == 0 && bit2 == 0) {
			return Calendar.SUNDAY;
		} else if (bit0 == 0 && bit1 == 0 && bit2 == 1) {
			return Calendar.MONDAY;
		} else if (bit0 == 0 && bit1 == 1 && bit2 == 0) {
			return Calendar.TUESDAY;
		} else if (bit0 == 0 && bit1 == 1 && bit2 == 1) {
			return Calendar.WEDNESDAY;
		} else if (bit0 == 1 && bit1 == 0 && bit2 == 0) {
			return Calendar.THURSDAY;
		} else if (bit0 == 1 && bit1 == 0 && bit2 == 1) {
			return Calendar.FRIDAY;
		} else if (bit0 == 1 && bit1 == 1 && bit2 == 0) {
			return Calendar.SATURDAY;
		}
		return 0;
	}
}
