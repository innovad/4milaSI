package com.fmila.sportident.port;

import java.io.IOException;
import java.util.Date;

import com.fmila.sportident.DownloadException;
import com.fmila.sportident.DownloadSession;
import com.fmila.sportident.bean.Punch;
import com.fmila.sportident.processor.AbstractSICardProcessor;
import com.fmila.sportident.processor.SICardV5Processor;
import com.fmila.sportident.processor.SICardV6Processor;
import com.fmila.sportident.processor.SICardV6Processor.MODEL;
import com.fmila.sportident.processor.SICardV8FamilyProcessor;
import com.fmila.sportident.serial.FMilaSerialPort;
import com.fmila.sportident.util.ByteUtility;
import com.fmila.sportident.util.SICardUtility;

public final class SICardSerialPortHandler extends AbstractSISerialPortHandler {

	private final Date currentEvtZero;
	private AbstractSICardProcessor currentSICard;

	private static final int LENGTH_CARD_INSERT = 12;
	private static final int LENGTH_BLOCK = 128;
	private static final int LENGTH_DATA_V5 = 136;
	private static final int LENGTH_DATA_V6_8_9 = 137;
	private static final int LENGTH_CONFIGURATION_V6 = 10;

	private static final int CARD_V5 = 229;
	private static final int CARD_V6 = 230;
	private static final int CARD_V8_9 = 232;
	private static final int CARD_REMOVED = 231;

	private static final int CARD_V6STAR_TYPE = 255;
	private static final int CARD_V6_TYPE = 193;
	private static final int CARD_V6_CONFIGURATION = 131;

	private static final int DATA_V5 = 177;
	private static final int DATA_V6 = 225;
	private static final int DATA_V8_9 = 239;

	private static final int LOCATION_COMMAND = 1;
	private static final int LOCATION_CARD_V6_TYPE = 6;

	public SICardSerialPortHandler(Date currentEvtZero, DownloadSession downloadSession, FMilaSerialPort port) {
		super(downloadSession, port);
		this.currentEvtZero = currentEvtZero;
	}

	/**
	 * handle data on port
	 */
	@Override
	protected void handleData(byte[] data) throws IOException, DownloadException {
		if (data.length == LENGTH_CARD_INSERT) {
			handleCardInsertedOrRemoved(data); // card inserted or removed
		} else if (data.length == LENGTH_DATA_V5 && (data[LOCATION_COMMAND] & 0xff) == DATA_V5) {
			currentSICard.handleProcessData(extractDataBlock(data, 5)); // V5
																		// data
		} else if (data.length == LENGTH_CONFIGURATION_V6 && (data[LOCATION_COMMAND] & 0xff) == CARD_V6_CONFIGURATION) {
			handleV6Configuration(data); // V6/6* data configuration
		} else if (data.length >= LENGTH_DATA_V6_8_9 && ((data[LOCATION_COMMAND] & 0xff) == DATA_V6 ||
				(data[LOCATION_COMMAND] & 0xff) == DATA_V8_9)) {
			handleV6DataBlock(data); // handle V6, V6*, V8, V9 data block
		} else if (data.length == 9) {
			currentSICard.handleSiacAirModeAnswer(data);
		} else if (data.length > 1 && ((data[1] & 0xff) == 0xD3)) {
			// auto send
			Long controlNo = ByteUtility.getLongFromBytes(data[3], data[4]);
			Long cardNo = ByteUtility.getLongFromBytes(data[5], data[6], data[7], data[8]) & 16777215L;
			// auto send punch is very similar to V6 punch
			Punch punch = SICardUtility.readV6Punch(new byte[] { data[9], data[0], data[10], data[11] }, 0, 1, currentEvtZero);
			punch.setControlNo(controlNo.toString()); // control no is not sent in punch data
			getDownloadSession().handleAutoSend(cardNo.toString(), controlNo.toString(), punch);
		} else if (data.length > 2 && ((data[2] & 0xff) == 0xD3)) {
			// srr dongle - currently only 1 control supported
			data[4] &= ~(1 << 7); // remove srr flag
			Long controlNo = ByteUtility.getLongFromBytes(data[4], data[5]); // check CNH
			Long cardNo = ByteUtility.getLongFromBytes(data[6], data[7], data[8], data[9]) & 16777215L;
			// auto send punch is very similar to V6 punch
			Punch punch = SICardUtility.readV6Punch(new byte[] { data[10], data[1], data[11], data[12] }, 0, 1, currentEvtZero);
			punch.setControlNo(controlNo.toString()); // control no is not sent in punch data
			getDownloadSession().handleAutoSend(cardNo.toString(), controlNo.toString(), punch);
		} else {
			// unknown
			throw new DownloadException("Unsupported Operation, length " + data.length + ", content: " + ByteUtility.dumpBytes(data));
		}
	}

	private void handleV6Configuration(byte[] data) throws DownloadException, IOException {
		SICardV6Processor processor = (SICardV6Processor) currentSICard;
		if ((data[LOCATION_CARD_V6_TYPE] & 0xff) == CARD_V6_TYPE) {
			// SI Card 6
			processor.setModel(MODEL.SIX);
			processor.setNumberOfDataMessages(3 + 1);
		} else if ((data[LOCATION_CARD_V6_TYPE] & 0xff) == CARD_V6STAR_TYPE) {
			// SI Card 6*
			processor.setModel(MODEL.SIXSTAR);
			processor.setNumberOfDataMessages(7 + 1);
		} else {
			throw new DownloadException("Unsupported SI Card 6 Type: " + ByteUtility.dumpBytes(new byte[] { data[6] }));
		}
		currentSICard.handleProcessData(null);
	}

	private void handleV6DataBlock(byte[] data) throws DownloadException, IOException {
		int commandByte = data[LOCATION_COMMAND] & 0xff;

		if (commandByte == DATA_V6) {
			// V6/6* data block 0 - 7
			byte[] carddataByte = extractDataBlock(data, 6);
			if (currentSICard.getRawData() == null) {
				// we are reading the first block
				double punchCount = ((SICardV6Processor) currentSICard).getNumberOfPunches(carddataByte);
				int numOfBlocks = (int) Math.round(Math.ceil(punchCount / 32));
				((SICardV6Processor) currentSICard).setNumberOfDataMessages(1 + 1 + numOfBlocks);
			}
			currentSICard.handleProcessData(carddataByte);
		} else if (commandByte == DATA_V8_9) {
			if (data.length % LENGTH_DATA_V6_8_9 != 0) {
				throw new DownloadException("Illegal data block size, size: " + data.length);
			}
			for (int k = 0; k < data.length; k = k + LENGTH_DATA_V6_8_9) {
				currentSICard.handleProcessData(ByteUtility.getSubarray(data, k + 6, LENGTH_BLOCK));
			}
		} else {
			throw new DownloadException("Unknown data block, data: " + ByteUtility.dumpBytes(data));
		}
	}

	/**
	 * handle data message when card was inserted or removed
	 * 
	 * @param data
	 * @throws IOException
	 * @throws ProcessingException
	 */
	private void handleCardInsertedOrRemoved(byte[] data) throws IOException, DownloadException {
		int commandByte = data[LOCATION_COMMAND] & 0xff;

		if (commandByte == CARD_V5) {
			// V5 inserted
			currentSICard = new SICardV5Processor(getPort(), currentEvtZero, getDownloadSession());
			currentSICard.handleCardInserted(data);
		} else if (commandByte == CARD_V6) {
			// V6 inserted
			currentSICard = new SICardV6Processor(getPort(), currentEvtZero, getDownloadSession());
			currentSICard.handleCardInserted(data);
		} else if (commandByte == CARD_V8_9) {
			// V8-9 inserted
			currentSICard = new SICardV8FamilyProcessor(getPort(), currentEvtZero, getDownloadSession());
			currentSICard.handleCardInserted(data);
		} else if (commandByte == CARD_REMOVED) {
			// any card removed
			if (currentSICard != null) {
				currentSICard.handleCardRemoved(data);
			}
		} else {
			throw new DownloadException("Unsupported E-Card, command: " + ByteUtility.dumpBytes(data));
		}
	}

	/**
	 * @param data
	 * @param start
	 *            start of block
	 * @return extracted 128 byte block
	 */
	private byte[] extractDataBlock(byte[] data, int start) {
		byte[] carddataByte = new byte[LENGTH_BLOCK];
		System.arraycopy(data, start, carddataByte, 0, LENGTH_BLOCK);
		return carddataByte;
	}

	public AbstractSICardProcessor getProcessor() {
		return currentSICard;
	}

}
