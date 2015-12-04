package com.fmila.sportident;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fmila.sportident.util.WindowsRegistryUtility;

public final class DownloadPorts {

	public static Map<String, String> getPorts() {
		
		// find friendly names
		HashMap<String, String> friendlyNames = new HashMap<String, String>();
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			String[] registryList = WindowsRegistryUtility.listRegistryEntries("HKLM\\System\\CurrentControlSet\\Enum")
					.split("\n");
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
		return friendlyNames;
		
	}
	
}
