package com.fmila.sportident.port;

import java.io.IOException;

import com.fmila.sportident.serial.FMilaSerialEventListener;
import com.fmila.sportident.serial.FMilaSerialPort;

public class FMilaSerialTestPort implements FMilaSerialPort {

	public FMilaSerialTestPort() {
	}

	@Override
	public void write(byte... data) throws IOException {
		// nop - testing only
	}

	@Override
	public void close() {
		// nop - testing only
	}

	@Override
	public void addEventListener(FMilaSerialEventListener listener) {
		// nop - testing only
	}

	@Override
	public String getStationNo() {
		return null;
	}

	@Override
	public void setStationNo(String stationNo) {
	}

}
