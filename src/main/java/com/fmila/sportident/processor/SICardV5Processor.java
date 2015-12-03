package com.fmila.sportident.processor;

import java.util.ArrayList;
import java.util.Date;

import com.fmila.sportident.DownloadCallback;
import com.fmila.sportident.bean.Punch;
import com.fmila.sportident.serial.FMilaSerialPort;
import com.fmila.sportident.util.ByteUtility;
import com.fmila.sportident.util.SICardUtility;

public class SICardV5Processor extends AbstractSICardProcessor {

	public SICardV5Processor(FMilaSerialPort port, Date currentEvtZero, DownloadCallback session) {
		super(port, currentEvtZero, session);
	}

	@Override
	public long readSICardNrOfInsertedCard(byte[] data) {
		return ByteUtility.getLongFromBytes(data[5], data[6]) * 100000 +
				ByteUtility.getLongFromBytes(data[7], data[8]);
	}

	@Override
	protected String readSICardNoFromData(byte[] data) {
		long cardNr = ByteUtility.getLongFromBytes(data[4], data[5]);
		long nCardSeries = ByteUtility.getLongFromByte(data[6]);
		if (nCardSeries != 1) {
			cardNr += nCardSeries * 100000L;
		}
		return Long.toString(cardNr);
	}

	@Override
	protected byte[] getRequestDataCommand(int messageNr) {
		return new byte[] { (byte) 0x02, (byte) 0xb1, (byte) 0x00, (byte) 0xb1, (byte) 0x00, (byte) 0x03 };
	}

	@Override
	public int getNumberOfDataMessages() {
		return 1;
	}

	@Override
	protected ArrayList<Punch> readControlsFromData(byte[] data) {
		ArrayList<Punch> punches = new ArrayList<Punch>();

		int numPunches = (data[23] & 0xff) - 1;
		int numFullPunches = numPunches;

		/*
		 * x.y - punching records x.1 control stations code number x.2, x.3
		 * punching time in 12h format 31-36 - punching records controls
		 * stations code number only
		 */

		// 30 full punches
		if (numFullPunches > 30) {
			numFullPunches = 30;
		}
		for (int i = 0; i < numFullPunches; i++) {
			int add = (i / 5) * 16 + (i % 5) * 3;
			Punch punch = SICardUtility.readV5Punch(data, add + 33, i + 1, getCurrentEvtZero());
			punches.add(punch);
		}

		// 6 punches without time
		if (numPunches > 30) {
			int numPartialPunches = numPunches - 30;
			if (numPartialPunches > 6) {
				numPartialPunches = 6;
			}
			for (int i = 0; i < numPartialPunches; i++) {
				int add = (i + 1) * 16;
				long code = ByteUtility.getLongFromByte(data[add]);
				Punch punch = new Punch();
				punch.setControlNo("" + code);
				punch.setSortCode(new Long(31 + i));
				punches.add(punch);
			}
		}
		return punches;
	}

	@Override
	protected Long readFinishTime(byte[] data) {
		Punch punch = SICardUtility.readV5Punch(data, 20, 0, getCurrentEvtZero());
		return punch.getRawTime();
	}

	@Override
	protected Long readStartTime(byte[] data) {
		Punch punch = SICardUtility.readV5Punch(data, 18, 0, getCurrentEvtZero());
		return punch.getRawTime();
	}

	@Override
	protected Long readCheckTime(byte[] data) {
		Punch punch = SICardUtility.readV5Punch(data, 24, 0, getCurrentEvtZero());
		return punch.getRawTime();
	}

	@Override
	protected Long readClearTime(byte[] data) {
		Punch punch = SICardUtility.readV5Punch(data, 24, 0, getCurrentEvtZero());
		return punch.getRawTime();
	}

}
