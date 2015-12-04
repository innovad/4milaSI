package com.fmila.sportident;

import java.util.List;

import com.fmila.sportident.bean.Punch;

public interface DownloadSession {
	
	public boolean handleCardInserted(String cardNo);
	
	public boolean handleData(String cardNo, List<Punch> controlData);

	public void handleCardRemoved();

}
