package com.fmila.sportident;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

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
		StringBuilder punchList = new StringBuilder();
		for (Punch p : controlData) {
			System.out.println(p.getControlNo());
			punchList.append(p.getControlNo());
			punchList.append(";");
		}
		System.out.println("************");

		/***************/
		
		try {
		    URL u = new URL( "http://localhost/kletterhalle/upload.php?card=" + cardNo + "&punches=" + punchList.toString());
		    Scanner scanner = new Scanner( u.openStream() );
			String r = scanner.useDelimiter( "\\Z" ).next();
			scanner.close();
		    System.out.println( r );
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/***************/
		
		return true;
	}

}
