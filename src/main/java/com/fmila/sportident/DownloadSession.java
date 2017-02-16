package com.fmila.sportident;

import java.util.List;

import com.fmila.sportident.bean.Punch;

public interface DownloadSession {
	
	public boolean handleCardInserted(String cardNo);
	
	public boolean handleData(String stationNo, String cardNo, List<Punch> controlData);
	
	public void handleAutoSend(String cardNo, String controlNo, Punch punch);
	
	public Boolean enableSiacAirMode();

	public void handleCardRemoved();

	public void waitForCards();

	public void close();

}
