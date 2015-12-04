package com.fmila.sportident;

import java.io.IOException;

import com.fmila.sportident.serial.FMilaSerialPort;

public abstract class AbstractSISerialPortHandler {

	private final DownloadSession downloadSession;
	private final FMilaSerialPort port;

	public AbstractSISerialPortHandler(DownloadSession downloadSession, FMilaSerialPort port) {
		this.downloadSession = downloadSession;
		this.port = port;
	}

	protected abstract void handleData(byte[] data) throws IOException, DownloadException;

	public FMilaSerialPort getPort() {
		return port;
	}

	public DownloadSession getDownloadSession() {
		return downloadSession;
	}

}
