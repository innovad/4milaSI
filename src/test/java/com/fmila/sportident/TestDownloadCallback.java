package com.fmila.sportident;

import java.util.List;

import com.fmila.sportident.bean.Punch;

public class TestDownloadCallback implements DownloadCallback {

	@Override
	public boolean handleCardInserted(String eCardNo) {
		System.out.println("ReadECard" + ": " + eCardNo);
		return true;
	}
	
	@Override
	public boolean handleData(String cardNo, List<Punch> controlData) {
		System.out.println("**Controls**" + "(" + cardNo + ")");
		for (Punch p : controlData) {
			System.out.println(p.getControlNo());
		}
		System.out.println("************");
		return true;
	}

}
