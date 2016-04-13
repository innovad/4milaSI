package com.fmila.sportident.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;

import com.fmila.sportident.DownloadException;
import com.fmila.sportident.bean.Punch;

public class SICardUtilityTest {

	@Test(expected = DownloadException.class)
	public void testReadV6PunchNull() throws Exception {
		SICardUtility.readV6Punch(null, 0, 0, new Date());
	}

	@Test(expected = DownloadException.class)
	public void testReadV6PunchTooShort() throws Exception {
		SICardUtility.readV6Punch(new byte[] { 0, 1 }, 0, 0, new Date());
	}

	@Test
	public void testReadV6ControlNo() throws Exception {
		byte[] bytes = new byte[] { 0, 33, 2, 3 };
		Punch punch = SICardUtility.readV6Punch(bytes, 0, 0, new Date());
		assertEquals("Control No", "33", punch.getControlNo());
	}

	@Test
	public void testReadV6RawTime() throws Exception {
		String evtZeroString = "10-11-2012 08:00:00.000";
		String punchTimeString = "10-11-2012 16:18:14.000";
		Punch punch = doTestV6(evtZeroString, punchTimeString);
		assertEquals("Raw Time", punch.getRawTime().longValue(), 29894000);
	}

	@Test
	public void testReadV6EvtZero1() throws Exception {
		String evtZeroString = "10-11-2012 16:00:00.000";
		String punchTimeString = "10-11-2012 16:18:14.000";
		doTestV6(evtZeroString, punchTimeString);
	}

	@Test
	public void testReadV6EvtZero2() throws Exception {
		String evtZeroString = "10-11-2012 17:00:00.000";
		String punchTimeString = "10-11-2012 16:18:14.000";
		doTestV6(evtZeroString, punchTimeString);
	}

	@Test
	public void testReadV6EvtZero3() throws Exception {
		String evtZeroString = "10-11-2012 17:00:00.000";
		String punchTimeString = "10-11-2012 16:18:14.000";
		doTestV6(evtZeroString, punchTimeString);
	}

	@Test
	public void testReadV6EvtZero4() throws Exception {
		String evtZeroString = "10-11-2012 00:00:00.000";
		String punchTimeString = "10-11-2012 16:18:14.000";
		doTestV6(evtZeroString, punchTimeString);
	}

	@Test
	public void testReadV6EvtZero5() throws Exception {
		// TODO
		String evtZeroString = "10-11-2012 23:59:59.999";
		String punchTimeString = "10-11-2012 16:18:14.000";
		doTestV6(evtZeroString, punchTimeString);
	}

	@Test
	public void testReadV5EvtZero1() throws Exception {
		String evtZeroString = "10-11-2012 08:00:00.000";
		String punchTimeString = "10-11-2012 14:31:40.000";
		doTestV5(evtZeroString, punchTimeString);
	}

	@Test
	public void testReadV5EvtZero2() throws Exception {
		String evtZeroString = "10-11-2012 02:00:00.000";
		String punchTimeString = "10-11-2012 02:31:40.000";
		doTestV5(evtZeroString, punchTimeString);
	}

	@Test
	public void testReadV5EvtZero3() throws Exception {
		String evtZeroString = "10-11-2012 23:59:59.999";
		String punchTimeString = "10-11-2012 14:31:40.000";
		doTestV5(evtZeroString, punchTimeString);
	}

	@Test
	public void testReadTCardPunch1() throws Exception {
		byte[] bytes = { -18, -18, -18, -18, -18, -18, -18, -18, 31, -127, -116, -63, 25, 21, -24, 114, 31, -127, -116, -63, 25, 21, -12, -55, 31, -127, -116, -63, 25, 36, -57, 83, 31, -127, -116, -63, 25, 36, -43, -100, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18,
				-18, -18, -18, -18, -18 };
		Punch punch = SICardUtility.readTCardPunch(bytes, 0, 1L, null);
		assertEquals("238", punch.getControlNo());
		assertNull(punch.getRawTime());
	}

	@Test
	public void testReadTCardPunch2() throws Exception {
		byte[] bytes = { 31, -127, -116, -63, 27, 59, 26, 68, 32, -125, -117, -127, 27, 59, 7, -16, 34, -124, 55, 65, 27, 59, 5, -96, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18,
				-18, -18, -18 };
		Punch punch = SICardUtility.readTCardPunch(bytes, 0, 1L, null);
		assertEquals("31", punch.getControlNo());
		assertEquals("16:12:10.192", DateTimeUtility.format(new Date(punch.getRawTime()),"HH:mm:ss.SSS"));
	}

	private Punch doTestV5(String evtZeroString, String punchTimeString) throws DownloadException {
		byte[] bytes = new byte[] { 34, 35, -116 };
		Date evtZero = DateTimeUtility.parse(evtZeroString, "dd-MM-yyyy HH:mm:ss.SSS");
		Punch punch = SICardUtility.readV5Punch(bytes, 0, 0, evtZero);
		assertEquals("Control No", "34", punch.getControlNo());
		Date punched = DateTimeUtility.addMilliSeconds(evtZero, punch.getRawTime());
		assertEquals("Time", punchTimeString, DateTimeUtility.format(punched, "dd-MM-yyyy HH:mm:ss.SSS"));
		return punch;
	}

	private Punch doTestV6(String evtZeroString, String punchTimeString) throws DownloadException {
		byte[] bytes = new byte[] { 13, 54, 60, -122 };
		Date evtZero = DateTimeUtility.parse(evtZeroString, "dd-MM-yyyy HH:mm:ss.SSS");
		Punch punch = SICardUtility.readV6Punch(bytes, 0, 0, evtZero);
		assertEquals("Control No", "54", punch.getControlNo());
		Date punched = DateTimeUtility.addMilliSeconds(evtZero, punch.getRawTime());
		assertEquals("Time", punchTimeString, DateTimeUtility.format(punched, "dd-MM-yyyy HH:mm:ss.SSS"));
		return punch;
	}

}
