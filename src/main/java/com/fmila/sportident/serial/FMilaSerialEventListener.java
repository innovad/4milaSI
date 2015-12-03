package com.fmila.sportident.serial;

import java.io.IOException;

/**
 * 
 */
public interface FMilaSerialEventListener {

	public void serialEvent(byte[] data) throws IOException;

}
