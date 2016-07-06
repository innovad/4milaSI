package com.fmila.sportident.serial;

import java.io.IOException;

/**
 * 
 */
public interface FMilaSerialPort {

	public void write(byte... data) throws IOException;

	public void close();

	public void addEventListener(final FMilaSerialEventListener listener);
	
	public String getStationNo();
	
	public void setStationNo(String stationNo);

}
