package com.fmila.sportident.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class DateTimeUtilityTest {

	@Test
	public void testTruncDate() {
		GregorianCalendar date = new GregorianCalendar();
		Date resultDate = DateTimeUtility.truncDate(date.getTime());
		GregorianCalendar result = new GregorianCalendar();
		result.setTime(resultDate);

		assertEquals(date.get(Calendar.MONTH), result.get(Calendar.MONTH));
		assertEquals(date.get(Calendar.DAY_OF_MONTH), result.get(Calendar.DAY_OF_MONTH));
		assertEquals(date.get(Calendar.YEAR), result.get(Calendar.YEAR));
		assertEquals(0, result.get(Calendar.MINUTE));
		assertEquals(0, result.get(Calendar.HOUR));
		assertEquals(0, result.get(Calendar.SECOND));
		assertEquals(0, result.get(Calendar.MILLISECOND));
	}

	@Test
	public void testAlignDateOfTime1() throws Exception {
		Long result = DateTimeUtility.alignDateOfTime(null, null, null);
		assertNull(result);
	}

	@Test
	public void testAlignDateOfTime2() throws Exception {
		Long result = DateTimeUtility.alignDateOfTime(32001L, null, null);
		assertEquals("00:00:32.001", DateTimeUtility.format(new Date(result), "HH:mm:ss.SSS"));
	}

	@Test
	public void testAlignDateOfTime3() throws Exception {
		Long result = DateTimeUtility.alignDateOfTime(32001L, DateTimeUtility.parse("21-03-1977", "dd-MM-yyyy"), null);
		assertEquals("21-03-1977 00:00:32.001", DateTimeUtility.format(new Date(result), "dd-MM-yyyy HH:mm:ss.SSS"));
	}

	@Test
	public void testAlignDateOfTime4() throws Exception {
		Long result = DateTimeUtility.alignDateOfTime(32001L, null, DateTimeUtility.parse("21-03-1977", "dd-MM-yyyy"));
		assertEquals(32001L, result.longValue());
	}
	
	@Test
	public void testAlignDateOfTime5() throws Exception {
		Long result = DateTimeUtility.alignDateOfTime(21632001L, null, DateTimeUtility.parse("21-03-1977 06:00:00.000", "dd-MM-yyyy HH:mm:ss.SSS"));
		assertEquals(32001L, result.longValue());
	}

}
