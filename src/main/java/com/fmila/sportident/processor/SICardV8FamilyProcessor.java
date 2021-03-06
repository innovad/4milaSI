package com.fmila.sportident.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fmila.sportident.DownloadException;
import com.fmila.sportident.DownloadSession;
import com.fmila.sportident.SICardType;
import com.fmila.sportident.bean.Punch;
import com.fmila.sportident.serial.FMilaSerialPort;
import com.fmila.sportident.util.ByteUtility;
import com.fmila.sportident.util.SICardUtility;

public class SICardV8FamilyProcessor extends AbstractSICardProcessor {

	private static final byte[] COMMAND_REQUEST_DATAV8_0 = { (byte) 0x02, (byte) 0xef, (byte) 0x01, (byte) 0x00, (byte) 0xe2, (byte) 0x09, (byte) 0x03 };
	private static final byte[] COMMAND_REQUEST_DATAV8_1 = { (byte) 0x02, (byte) 0xef, (byte) 0x01, (byte) 0x01, (byte) 0xe3, (byte) 0x09, (byte) 0x03 };
	private static final byte[] COMMAND_REQUEST_DATAV10_04567 = { (byte) 0x02, (byte) 0xef, (byte) 0x01, (byte) 0x08, (byte) 0xea, (byte) 0x09, (byte) 0x03 };

	public SICardV8FamilyProcessor(FMilaSerialPort port, Date currentEvtZero, DownloadSession session) {
		super(port, currentEvtZero, session);
	}

	@Override
	public long readSICardNrOfInsertedCard(byte[] data) {
		return ByteUtility.getLongFromBytes(data[5], data[6], data[7], data[8]) & 16777215L; // hidden secrets :-)
	}

	@Override
	protected byte[] getRequestDataCommand(int messageNr) throws DownloadException {
		if (SICardUtility.getType(getECardNo()) == SICardType.SICARD8 ||
				SICardUtility.getType(getECardNo()) == SICardType.SICARD9) {
			if (messageNr == 1) {
				return COMMAND_REQUEST_DATAV8_0;
			}
			return COMMAND_REQUEST_DATAV8_1;
		} else {
			if (messageNr == 1) {
				return COMMAND_REQUEST_DATAV10_04567;
			}
			return null;
		}
	}

	@Override
	protected String readSICardNoFromData(byte[] data) {
		return Long.toString(ByteUtility.getLongFromBytes(data[24], data[25], data[26], data[27]) & 16777215L); // hidden secrets :-)
	}

	@Override
	public int getNumberOfDataMessages() throws DownloadException {
		if (SICardUtility.getType(getECardNo()) == SICardType.SICARD8) {
			return 2;
		} else if (SICardUtility.getType(getECardNo()) == SICardType.SICARD9) {
			return 2;
		} else if (SICardUtility.getType(getECardNo()) == SICardType.pCARD) {
			return 2;
		} else if (SICardUtility.getType(getECardNo()) == SICardType.tCARD) {
			return 2;
		}
		return 5;
	}

	@Override
	protected List<Punch> readControlsFromData(byte[] data) throws DownloadException {
		List<Punch> punches = new ArrayList<Punch>();

		int cardType = data[24] & 0xff;
		int numberOfPunches = data[22] & 0xff;
		int firstPunch = 56;

		if (cardType == 15) {
			// SI Card 10/11/SIAC
			firstPunch = 128;
		} else if (cardType == 2) {
			// SI Card 8 30 Records
			firstPunch = 136;
		} else if (cardType == 4) {
			// pCard
			firstPunch = 176;
		} else if (cardType == 6) {
			// tCard
			numberOfPunches = numberOfPunches / 2;
		}

		for (int i = 0; i < numberOfPunches; i++) {
			if (cardType == 6) {
				punches.add(SICardUtility.readTCardPunch(data, firstPunch + i * 8, i + 1, getCurrentEvtZero()));
			} else {
				// same punch structure as V6
				punches.add(SICardUtility.readV6Punch(data, firstPunch + i * 4, i + 1, getCurrentEvtZero()));
			}
		}

		return punches;
	}

	@Override
	protected Long readFinishTime(byte[] data) throws DownloadException {
		// same punch structure as V6
		Punch punch = SICardUtility.readV6Punch(data, 16, 0, getCurrentEvtZero());
		return punch.getRawTime();
	}

	@Override
	protected Long readStartTime(byte[] data) throws DownloadException {
		// same punch structure as V6
		Punch punch = SICardUtility.readV6Punch(data, 12, 0, getCurrentEvtZero());
		return punch.getRawTime();
	}

	@Override
	protected Long readCheckTime(byte[] data) throws DownloadException {
		// same punch structure as V6
		Punch punch = SICardUtility.readV6Punch(data, 8, 0, getCurrentEvtZero());
		return punch.getRawTime();
	}

	@Override
	protected Long readClearTime(byte[] data) throws DownloadException {
		// same punch structure as V6
		Punch punch = SICardUtility.readV6Punch(data, 8, 0, getCurrentEvtZero());
		return punch.getRawTime();
	}

	@Override
	protected byte[] getSiacAirModeCommand(boolean enabled) throws IOException {
		byte[] command = null;
		if (SICardType.SIAC1.equals(SICardUtility.getType(getECardNo()))) {
			if (enabled) {
				command = new byte[] { (byte) 0xFF, (byte) 0x02, (byte) 0xEA, (byte) 0x05, (byte) 0x7E, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x29, (byte) 0x04, (byte) 0x03 };
				System.out.println("enable SIAC/Air");
			} else {
				command = new byte[] { (byte) 0xFF, (byte) 0x02, (byte) 0xEA, (byte) 0x05, (byte) 0x7E, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA9, (byte) 0x13, (byte) 0x03 };
				System.out.println("disable SIAC/Air");
			}
		} 
		return command;
	}

}
