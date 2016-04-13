package com.fmila.sportident.port;

import java.io.IOException;

import com.fmila.sportident.DownloadException;
import com.fmila.sportident.DownloadSession;
import com.fmila.sportident.serial.FMilaSerialEventListener;
import com.fmila.sportident.serial.FMilaSerialPort;
import com.fmila.sportident.util.ByteUtility;
import com.fmila.sportident.util.CRCCalculatorUtility;

public class SISerialPortListener implements FMilaSerialEventListener {

	private final DownloadSession session;
	private FMilaSerialPort port;
	private AbstractSISerialPortHandler handler;
	private byte[] lastPartialMessage;

	public SISerialPortListener(DownloadSession session) {
		this.session = session;
	}

	public void installHandler(AbstractSISerialPortHandler serialPortHandler) {
		this.handler = serialPortHandler;
	}

	@Override
	public void serialEvent(byte[] data) throws IOException {
		System.out.println("ActivityAtECardStation");
		try {
			if (data.length > 137 && data.length % 137 == 0) {
				// message size 137 or multiples
				for (int k = 0; k < data.length; k = k + 137) {
					checkCRC(ByteUtility.getSubarray(data, k, 137));
				}
			} else if (data.length % 137 > 12 && data.length != 136) {
				// with SI-Cards 8/9 sometimes only partial messages are sent
				// if a message size is 'in between', we wait for the rest of
				// the message
				if (lastPartialMessage != null) {
					data = ByteUtility.concatArrays(lastPartialMessage, data);
					lastPartialMessage = null;
					for (int k = 0; k < data.length; k = k + 137) {
						checkCRC(ByteUtility.getSubarray(data, k, 137));
					}
				} else {
					System.out.println("Partial Read, waiting for another message)");
					lastPartialMessage = data;
					return;
				}
			} else {
				// message size 12 or 136
				checkCRC(data);
			}
			handler.handleData(data);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: " + e.getMessage());
		}
	}

	private void checkCRC(byte[] data) throws DownloadException {
		if (data == null || data.length < 4) {
			if (data != null) {
				if (data.length == 1 && data[0] == 21) {
					showInfoWindowError();
					throw new DownloadException("ReadOutError" + ": " + "NAK");
				}
			}
			showInfoWindowError();
			throw new DownloadException("ReadOutError" + ": " + "Message empty or too short");
		}

		long crcCalculated = CRCCalculatorUtility.crc(ByteUtility.getSubarray(data, 1, data.length - 4));
		long crcRead = ByteUtility.getLongFromBytes(data[data.length - 3], data[data.length - 2]);

		if (!(crcCalculated == crcRead)) {
			showInfoWindowError();
			throw new DownloadException("ReadOutError" + ": " + "CRC Error " +
					ByteUtility.dumpBytes(new byte[] { new Long(crcCalculated).byteValue() }) + " != " +
					ByteUtility.dumpBytes(new byte[] { new Long(crcRead).byteValue() }));
		}
	}

	private void showInfoWindowError() {
		// if (InfoDisplayUtility.isActive()) {
		// new InfoDisplayIdleJob(TEXTS.get(session.getLocale(),
		// "ReadOutError").replace("({0})", ""), session).schedule();
		// }
	}

	public FMilaSerialPort getPort() {
		return port;
	}

	public DownloadSession getSession() {
		return session;
	}

}
