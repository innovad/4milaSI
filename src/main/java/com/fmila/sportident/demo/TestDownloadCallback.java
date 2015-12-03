package com.fmila.sportident.demo;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import com.fmila.sportident.DownloadCallback;
import com.fmila.sportident.bean.Punch;

public class TestDownloadCallback implements DownloadCallback {

	private String url;

	public TestDownloadCallback(String url) {
		super();
		this.url = url;
	}

	@Override
	public boolean handleCardInserted(String eCardNo) {
		System.out.println("ReadECard" + ": " + eCardNo);
		return true;
	}

	@Override
	public boolean handleData(String cardNo, List<Punch> controlData) {
		StringBuilder punchList = new StringBuilder();
		for (Punch p : controlData) {
			punchList.append(p.getControlNo());
			punchList.append(";");
		}
		System.out.println("Read Controls: " + punchList.toString());

		// send URL to localhost server
		try {
			String request = url + "?card=" + cardNo + "&punches=" + punchList.toString();
			System.out.println("Send request GET " + request);
			URL u = new URL(request);
			Scanner scanner = new Scanner(u.openStream());
			String r = scanner.useDelimiter("\\Z").next();
			scanner.close();
			System.out.println("http result: " + r);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}
	
	@Override
	public void handleCardRemoved() {
	    System.out.println("===> Waiting for cards ([q] for quit/exit): ");
	}

}
