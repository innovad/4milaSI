package com.fmila.sportident.port;

import java.io.IOException;

import com.fmila.sportident.DownloadException;
import com.fmila.sportident.DownloadSession;
import com.fmila.sportident.serial.FMilaSerialPort;
import com.fmila.sportident.util.ByteUtility;
import com.fmila.sportident.util.CRCCalculatorUtility;

public final class SIStationSerialPortHandler extends AbstractSISerialPortHandler {

	private final Object lock;
	private boolean isInitialized = false;

	public SIStationSerialPortHandler(DownloadSession session, Object lock, FMilaSerialPort port) {
		super(session, port);
		this.lock = lock;
	}

	@Override
	protected void handleData(byte[] data) throws IOException, DownloadException {

		if (data.length == 9 && (data[1] == -16) && (data[5] == 77)) {
			// complete answer is [2, -16, 3, 2, 69, 77, -128, -118, 3]
			// request station info
			int crc = CRCCalculatorUtility.crc(new byte[] { (byte) 0x83, (byte) 0x02, (byte) 0x74, (byte) 0x01 });
			byte[] requestStationInfo = { (byte) 0x02, (byte) 0x83, (byte) 0x02, (byte) 0x74, (byte) 0x01, (byte) (crc >> 8 & 0xff), (byte) (crc & 0xff), (byte) 0x03 };
			getPort().write(requestStationInfo);
		} else if (data.length == 10 && data[1] == -125) {
			// get station info

			/*
			 * CN1, CN0 2 bytes stations code number 1...999 0x74 1 byte data address in the system memory
			 * 
			 * CPC 1 byte protocol configuration, bit mask value xxxxxxx1b extended protocol xxxxxx1xb auto send
			 * out xxxxx1xxb handshake (only valid for card readout) xxx1xxxxb
			 * access with password only 1xxxxxxxb read out SI-card after punch
			 * (only for punch modes; depends on bit 2: auto send out or
			 * handshake) 
			 * 
			 * CRC1, CRC0 2 bytes 16 bit CRC value, computed
			 * including command byte and LEN (0x04, 0x14 for the request)
			 */

			Long stationNr = ByteUtility.getLongFromBytes(data[3], data[4]);

			byte cpc = data[6];
			boolean extendedProtocol = (cpc & (1 << 0)) != 0;
			boolean autoSend = (cpc & (1 << 1)) != 0;
			boolean handShake = (cpc & (1 << 2)) != 0;
			boolean password = (cpc & (1 << 4)) != 0;
			boolean readOutAfterPunch = (cpc & (1 << 7)) != 0;
			System.out.println("Checking SI-Station, Nr: " + stationNr + ", Extended Protocol: " + extendedProtocol + ", Password: " + password + ", Read-Out After Punch: " + readOutAfterPunch);

			if (!extendedProtocol) {
				throw new DownloadException("Extended Protocol required. Please set SI Station to extended Protocol.");
			}
			if (autoSend) {
				throw new DownloadException("AutoSend not supported. Please turn off AutoSend on SI Station.");
			}
			if (!handShake) {
				throw new DownloadException("Handshake required. Please turn on Handshake on SI Station.");
			}

			synchronized (lock) {
				// set ready flag to true (so isInitialized() returns true)
				isInitialized = true;
				lock.notifyAll();
			}

			System.out.println("ECardStationReady" + ": " + String.valueOf(stationNr));
		} else {
			// unknown
			throw new DownloadException("Unsupported Operation: " + ByteUtility.dumpBytes(data));
		}

	}

	public boolean isInitialized() {
		return isInitialized;
	}

}
