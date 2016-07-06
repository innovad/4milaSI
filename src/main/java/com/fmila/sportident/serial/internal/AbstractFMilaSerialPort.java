package com.fmila.sportident.serial.internal;

import com.fmila.sportident.serial.FMilaSerialPort;

public abstract class AbstractFMilaSerialPort implements FMilaSerialPort {
	
	private String stationNo;

	@Override
	public String getStationNo() {
		return this.stationNo;
	}
	
	public void setStationNo(String stationNr) {
		this.stationNo = stationNr;
	}

}
