package com.fmila.sportident.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.fmila.sportident.DownloadCallback;
import com.fmila.sportident.DownloadException;
import com.fmila.sportident.bean.DownloadStation;
import com.fmila.sportident.bean.Punch;
import com.fmila.sportident.serial.FMilaSerialPort;
import com.fmila.sportident.util.ByteUtility;

public abstract class AbstractSICardProcessor {

	private final DownloadCallback session;
	private final FMilaSerialPort port;
	private final Long mode;
	private final Date currentEvtZero;
	private final long eventNr;
	private final DownloadStation station;

	private String eCardNo;
	private ArrayList<Punch> controlData; // punches with control no. and time
	private Long clearTime; // clear time if available
	private Long checkTime; // check time if available
	private Long startTime; // start time if available
	private Long finishTime; // finish time if available
	private byte[] rawData; // raw data in blocks
	private int dataMessagesCounter = 0;

	public AbstractSICardProcessor(DownloadStation station, FMilaSerialPort port, Date currentEvtZero, long eventNr, DownloadCallback session) {
		super();
		this.session = session;
		this.station = station;
		this.port = port;
		this.mode = station.getMode();
		this.currentEvtZero = currentEvtZero;
		this.eventNr = eventNr;
	}

	/**
	 * SI Card was inserted into station
	 * 
	 * @param data
	 * @throws IOException
	 */
	public final void handleCardInserted(byte[] data) throws IOException, DownloadException {
		long cardNr = readSICardNrOfInsertedCard(data);
		String cardNo = String.valueOf(cardNr);
		setECardNo(cardNo);
		
		boolean requestMoreData = session.handleCardInserted(cardNo);
		
		if (requestMoreData) {
			// TODO
			System.out.println("RequestECardData" + ": " + eCardNo);
			port.write(getRequestDataCommand(dataMessagesCounter + 1));
		}
	}

	/**
	 * SI Card data is processed (in case of newer cards, this step may be
	 * repeated)
	 * 
	 * @param data
	 * @throws IOException
	 */
	public final void handleProcessData(byte[] data) throws IOException, DownloadException {
		if (getRawData() == null) {
			setRawData(data);
		} else {
			data = ByteUtility.concatArrays(getRawData(), data);
			setRawData(data);
		}
		dataMessagesCounter++;
		// block 0
		if (dataMessagesCounter == getNumberOfDataMessages()) {
			// TODO
			System.out.println("ProcessData");
			setECardNo(readSICardNoFromData(data));
			setControlData(readControlsFromData(data));
			setClearTime(readClearTime(data));
			setCheckTime(readCheckTime(data));
			setStartTime(readStartTime(data));
			setFinishTime(readFinishTime(data));
			
			boolean remove = session.handleData(getECardNo(), controlData);
			if (remove) {
				removeSICard();
			}
		}
		// request next block
		else {
			byte[] requestDataCommand = getRequestDataCommand(dataMessagesCounter + 1);
			if (requestDataCommand != null) {
				port.write(requestDataCommand);
			}
		}
	}

	private void removeSICard() throws IOException {
		port.write((byte) 6);
		// TODO
		System.out.println("RemoveECardMessage");
	}

	/**
	 * SI Card was removed from the station
	 * 
	 * @param data
	 */
	public final void handleCardRemoved(byte[] data) {
		// TODO
		System.out.println("ECardRemovedMessage");
	}

	protected abstract long readSICardNrOfInsertedCard(byte[] data);

	protected abstract String readSICardNoFromData(byte[] data);

	protected abstract Long readClearTime(byte[] data) throws DownloadException;

	protected abstract Long readCheckTime(byte[] data) throws DownloadException;

	protected abstract Long readStartTime(byte[] data) throws DownloadException;

	protected abstract Long readFinishTime(byte[] data) throws DownloadException;

	protected abstract byte[] getRequestDataCommand(int messageNr) throws DownloadException;

	public abstract int getNumberOfDataMessages() throws DownloadException;

	protected abstract ArrayList<Punch> readControlsFromData(byte[] data) throws DownloadException;

	public String getECardNo() {
		return this.eCardNo;
	}

	public void setECardNo(String number) {
		this.eCardNo = number;
	}

	public ArrayList<Punch> getControlData() {
		return controlData;
	}

	public void setControlData(ArrayList<Punch> controlData) {
		this.controlData = controlData;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Long finishTime) {
		this.finishTime = finishTime;
	}

	public byte[] getRawData() {
		return rawData;
	}

	public void setRawData(byte[] rawData) {
		this.rawData = rawData;
	}

	public Date getCurrentEvtZero() {
		return currentEvtZero;
	}

	public Long getClearTime() {
		return clearTime;
	}

	public void setClearTime(Long clearTime) {
		this.clearTime = clearTime;
	}

	public Long getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(Long checkTime) {
		this.checkTime = checkTime;
	}

}
