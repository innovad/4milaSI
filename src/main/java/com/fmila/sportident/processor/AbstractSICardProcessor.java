package com.fmila.sportident.processor;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.fmila.sportident.DownloadException;
import com.fmila.sportident.DownloadSession;
import com.fmila.sportident.bean.Punch;
import com.fmila.sportident.serial.FMilaSerialPort;
import com.fmila.sportident.util.ByteUtility;

public abstract class AbstractSICardProcessor {

	private final DownloadSession session;
	private final FMilaSerialPort port;
	private final Date currentEvtZero;

	private String eCardNo;
	private List<Punch> controlData; // punches with control no. and time
	private Long clearTime; // clear time if available
	private Long checkTime; // check time if available
	private Long startTime; // start time if available
	private Long finishTime; // finish time if available
	private byte[] rawData; // raw data in blocks
	private int dataMessagesCounter = 0;

	public AbstractSICardProcessor(FMilaSerialPort port, Date currentEvtZero, DownloadSession session) {
		super();
		this.session = session;
		this.port = port;
		this.currentEvtZero = currentEvtZero;
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

			boolean remove = session.handleData(port.getStationNo(), getECardNo(), controlData);
			if (remove) {
				if (session.enableSiacAirMode() != null) {
					byte[] command = getSiacAirModeCommand(session.enableSiacAirMode());
					if (command != null) {
						port.write(command);
					} else {
						removeSICard();
					}
				} else {
					removeSICard();
				}
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
		System.out.println("ECardRemovedMessage");
		session.handleCardRemoved();
	}

	protected abstract long readSICardNrOfInsertedCard(byte[] data);

	protected abstract String readSICardNoFromData(byte[] data);

	protected abstract Long readClearTime(byte[] data) throws DownloadException;

	protected abstract Long readCheckTime(byte[] data) throws DownloadException;

	protected abstract Long readStartTime(byte[] data) throws DownloadException;

	protected abstract Long readFinishTime(byte[] data) throws DownloadException;

	protected abstract byte[] getRequestDataCommand(int messageNr) throws DownloadException;

	public abstract int getNumberOfDataMessages() throws DownloadException;

	protected abstract List<Punch> readControlsFromData(byte[] data) throws DownloadException;
	
	public void handleSiacAirModeAnswer(byte[] data) throws IOException {
		this.removeSICard();
	}

	protected abstract byte[] getSiacAirModeCommand(boolean enabled) throws IOException;

	public String getECardNo() {
		return this.eCardNo;
	}

	public void setECardNo(String number) {
		this.eCardNo = number;
	}

	public List<Punch> getControlData() {
		return controlData;
	}

	public void setControlData(List<Punch> controlData) {
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
