package com.fmila.sportident;

import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.fmila.sportident.bean.DownloadStation;
import com.fmila.sportident.serial.FMilaSerialPort;
import com.fmila.sportident.serial.SerialUtility;
import com.fmila.sportident.util.CRCCalculator;
import com.fmila.sportident.util.WindowsRegistryUtility;

public class DownloadTest {

	@Test
	public void testName() throws Exception {
	}
	
	public static void main (String[] args) throws Exception {

		FMilaSerialPort serialPort = null;
		
	    Scanner scanner = new Scanner(System.in);

		try {

			// find friendly names
			HashMap<String, String> friendlyNames = new HashMap<String, String>();
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				String[] registryList = WindowsRegistryUtility.listRegistryEntries("HKLM\\System\\CurrentControlSet\\Enum").split("\n");
				Pattern comPattern = Pattern.compile("FriendlyName.+REG_SZ(.+)\\((COM\\d+)\\)");

				for (String string : registryList) {
					Matcher match = comPattern.matcher(string);
					if (match.find()) {
						String com = match.group(2);
						String fname = com + ": " + match.group(1).trim();
						friendlyNames.put(com, fname);
					}
				}
			}

			// serial ports by RXTX
			try {
				for (String port : SerialUtility.getPorts()) {
					System.out.println(port + " (" + friendlyNames.get(port) + ")");
				}
			} catch (java.lang.Error e) {
				e.printStackTrace();
			}

		    //  prompt for the user's name
		    System.out.print("Select a port (1-x) or q for exit: ");

		    // get their input as a String
		    String selectedPort = scanner.next();

			if (selectedPort.toLowerCase().equals("q")) {
				System.out.println("Finished.");
				System.exit(0);
			}

			int speed = 38400;

			// open
			String port = selectedPort;
			serialPort = SerialUtility.getPort(speed, port);

			// station init
			Object lock = new Object();
			TestDownloadCallback callback = new TestDownloadCallback();
			SIStationSerialPortHandler stationHandler = new SIStationSerialPortHandler(callback, lock, serialPort);
			SISerialPortListener serialPortListener = new SISerialPortListener(callback);
			serialPortListener.installHandler(stationHandler);

			// add single serial port listener (options are done by handlers)
			serialPort.addEventListener(serialPortListener);

			// request direct communication with readout station
			int crc = CRCCalculator.crc(new byte[] { (byte) 0xF0, (byte) 0x01, (byte) 0x4D });
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
			DownloadStation station = new DownloadStation();
			SICardSerialPortHandler cardHandler = new SICardSerialPortHandler(station, new Date(), 1L, callback, serialPort);
			serialPortListener.installHandler(cardHandler);
			
			// ***
		    System.out.print("Waiting for cards (q for Exit): ");
			scanner.next();
			
			
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
			scanner.close();
			System.out.println("Finished.");
		}

	}

}
