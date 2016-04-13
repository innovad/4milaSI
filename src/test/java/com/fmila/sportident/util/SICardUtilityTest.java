package com.fmila.sportident.util;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertEquals("Control No", "33", punch.getControlNo());
	}

	@Test
	public void testReadV6RawTime() throws Exception {
		String evtZeroString = "10-11-2012 08:00:00.000";
		String punchTimeString = "10-11-2012 16:18:14.000";
		Punch punch = doTestV6(evtZeroString, punchTimeString);
		Assert.assertEquals("Raw Time", punch.getRawTime().longValue(), 29894000);
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
	public void testReadTCardPunch() throws Exception {
		byte[] bytes = { 18, 75, -18, 120, -22, -22, -22, -22, 0, 31, 8, 64, -18, -18, -18, -18, -18, -18, -18, -18, 0, 31, 10, 103, 6, 93, 27, 60, 12, 10, 50, 124, 54, 49, 48, 49, 56, 50, 48, 59, 70, 76, 65, 83, 75, 65, 77, 80, 32, 65, 71, 59, 0, 0, 0, 0, -18, -18, -18, -18, -18, -18, -18, -18, 31, -127, -116, -63, 25, 21, -24, 114, 31, -127, -116, -63, 25, 21, -12, -55, 31, -127, -116, -63, 25, 36, -57, 83, 31, -127, -116, -63, 25, 36, -43, -100, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18,
				-18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18 };
		Punch punch = SICardUtility.readTCardPunch(bytes, 56, 1L, null);
		assertEquals("238", punch.getControlNo());
	}

	private Punch doTestV5(String evtZeroString, String punchTimeString) throws DownloadException {
		byte[] bytes = new byte[] { 34, 35, -116 };
		Date evtZero = DateUtility.parse(evtZeroString, "dd-MM-yyyy HH:mm:ss.SSS");
		Punch punch = SICardUtility.readV5Punch(bytes, 0, 0, evtZero);
		Assert.assertEquals("Control No", "34", punch.getControlNo());
		Date punched = DateUtility.addMilliSeconds(evtZero, punch.getRawTime());
		Assert.assertEquals("Time", punchTimeString, DateUtility.format(punched, "dd-MM-yyyy HH:mm:ss.SSS"));
		return punch;
	}

	private Punch doTestV6(String evtZeroString, String punchTimeString) throws DownloadException {
		byte[] bytes = new byte[] { 13, 54, 60, -122 };
		Date evtZero = DateUtility.parse(evtZeroString, "dd-MM-yyyy HH:mm:ss.SSS");
		Punch punch = SICardUtility.readV6Punch(bytes, 0, 0, evtZero);
		Assert.assertEquals("Control No", "54", punch.getControlNo());
		Date punched = DateUtility.addMilliSeconds(evtZero, punch.getRawTime());
		Assert.assertEquals("Time", punchTimeString, DateUtility.format(punched, "dd-MM-yyyy HH:mm:ss.SSS"));
		return punch;
	}

}
