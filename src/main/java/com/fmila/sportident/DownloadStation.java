package com.fmila.sportident;

import java.io.IOException;
import java.util.Date;

import com.fmila.sportident.port.SICardSerialPortHandler;
import com.fmila.sportident.port.SISerialPortListener;
import com.fmila.sportident.port.SIStationSerialPortHandler;
import com.fmila.sportident.serial.FMilaSerialPort;
import com.fmila.sportident.serial.SerialUtility;
import com.fmila.sportident.util.CRCCalculatorUtility;


public class DownloadStation {

	private String port;
	private int speed;
	private Date evtZero;
	private DownloadSession downloadSession;
	
	public DownloadStation(String port, int speed, Date evtZero, DownloadSession downloadSession) {
		super();
		this.port = port;
		this.speed = speed;
		this.evtZero = evtZero;
		this.downloadSession = downloadSession;
	}

	public void open() throws IOException {

		FMilaSerialPort serialPort = SerialUtility.getPort(speed, port);
		
		try {
			// station init
			Object lock = new Object();
			SIStationSerialPortHandler stationHandler = new SIStationSerialPortHandler(downloadSession, lock, serialPort);
			SISerialPortListener serialPortListener = new SISerialPortListener(downloadSession);
			serialPortListener.installHandler(stationHandler);

			// add single serial port listener (options are done by handlers)
			serialPort.addEventListener(serialPortListener);

			// request direct communication with readout station
			int crc = CRCCalculatorUtility.crc(new byte[] { (byte) 0xF0, (byte) 0x01, (byte) 0x4D });
			byte[] message = { (byte) 0x02, (byte) 0xF0, (byte) 0x01, (byte) 0x4D, (byte) (crc >> 8 & 0xff), (byte) (crc & 0xff), (byte) 0x03 };
			serialPort.write(message);

			// wait 4 * 0.5sec for initialize
			synchronized (lock) {
				int counter = 0;
				while (!stationHandler.isInitialized() && counter <= 4) {
					lock.wait(500);
					counter++;
				}
			}
			if (!stationHandler.isInitialized()) {
				throw new DownloadException("Timeout occured, failed initializing station");
			}

			// station is initialized, now listen to cards
			SICardSerialPortHandler cardHandler = new SICardSerialPortHandler(evtZero, downloadSession, serialPort);
			serialPortListener.installHandler(cardHandler);
			
			downloadSession.waitForCards();
			
		} catch (Exception e) {
			if (serialPort != null) {
				serialPort.close();
			}
			e.printStackTrace(); // temp
			System.out.println(e.getMessage()); // temp
		} finally {
			if (serialPort != null) {
				serialPort.close();
			}
			downloadSession.close();
		}
		
		
		
	}

}
