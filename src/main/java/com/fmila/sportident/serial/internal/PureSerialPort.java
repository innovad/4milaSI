package com.fmila.sportident.serial.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fmila.sportident.DownloadStationException;
import com.fmila.sportident.serial.FMilaSerialEventListener;
import com.fmila.sportident.serial.FMilaSerialPort;

import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;

/**
 * 
 */
public class PureSerialPort extends AbstractFMilaSerialPort implements FMilaSerialPort {

	private final static Logger LOGGER = Logger.getLogger(PureSerialPort.class.getName());

	private final SerialPort serialPort;
	private final ByteArrayOutputStream baos;
	private final byte[] readBuffer;

	private static final int READ_TIMEOUT = 3000;

	public PureSerialPort(SerialPort serialPort) {
		this.serialPort = serialPort;
		if (serialPort == null) {
			throw new IllegalArgumentException("Port must not be null");
		}
		baos = new ByteArrayOutputStream(1024);
		readBuffer = new byte[1024];
	}

	@Override
	public void write(byte... data) throws IOException {
		serialPort.getOutputStream().write(data);
	}

	@Override
	public void close() {
		try {
			serialPort.removeEventListener();
			serialPort.close();
		} catch (IllegalStateException e) {
			// do not print exceptions if port was already closed (-1)
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error closing PureSerialPort", e);
		}
	}

	@Override
	public void addEventListener(final FMilaSerialEventListener listener) {
		SerialPortEventListener eventListener = new SerialPortEventListener() {
			@Override
			public void serialEvent(SerialPortEvent e) {
				SerialPort port = ((SerialPort) e.getSource());
				InputStream is = null;
				try {
					is = port.getInputStream();
					switch (e.getEventType()) {
					case SerialPortEvent.DATA_AVAILABLE: {
						baos.reset();

						boolean endoffile = false;
						long timeout = System.currentTimeMillis();
						while (is.available() > 0 && !endoffile) {
							int numBytes = is.read(readBuffer);
							baos.write(readBuffer, 0, numBytes);
							if (baos.toByteArray()[baos.toByteArray().length - 1] == -1) {
								endoffile = true;
							}
							if (System.currentTimeMillis() > timeout + READ_TIMEOUT) {
								throw new IOException("Read error, timeout occured.");
							}
						}
						byte[] data = baos.toByteArray();
						listener.serialEvent(data);
						break;
					}
					default: // nop
					}
				} catch (Exception e1) {
					throw new DownloadStationException("Error adding event listener", e1);
				}
			}
		};
		try {
			serialPort.addEventListener(eventListener);
		} catch (TooManyListenersException e) {
			throw new DownloadStationException("Error adding event listener", e);
		}
	}

}
