package com.fmila.sportident;

import java.io.IOException;

import com.fmila.sportident.serial.FMilaSerialPort;

public abstract class AbstractSISerialPortHandler {

	private final DownloadCallback callback;
	private final FMilaSerialPort port;

	public AbstractSISerialPortHandler(DownloadCallback callback, FMilaSerialPort port) {
		this.callback = callback;
		this.port = port;
	}

	protected abstract void handleData(byte[] data) throws IOException, DownloadException;

	public FMilaSerialPort getPort() {
		return port;
	}

	public DownloadCallback getCallback() {
		return callback;
	}

}
