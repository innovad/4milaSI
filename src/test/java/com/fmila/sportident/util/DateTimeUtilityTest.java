package com.fmila.sportident.util;

import static org.junit.Assert.assertEquals;

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
	}

}
