package com.fmila.sportident.util;

import com.fmila.sportident.util.FMilaUtility.OperatingSystem;

public class TestUtility {

	public static String getSerialTestingPort() {
		if (OperatingSystem.WINDOWS.equals(FMilaUtility.getPlatform())) {
			return "COM3"; // Windows
		} else if (OperatingSystem.MACOSX.equals(FMilaUtility.getPlatform())) {
			return "tty.Bluetooth-Incoming-Port"; // Mac OS X 10.9
		}
		return "UNKNOWN_OS";
	}

}
