package com.fmila.sportident.util;

import com.fmila.sportident.DownloadException;
import com.fmila.sportident.ECardType;

public final class ECardUtility {

	private ECardUtility() {
	}

	public static ECardType getType(String eCardNo) throws DownloadException {

		// SI-Card5 SI-Card6 SI-Card6* SI-Card8 SI-Card9 pCard tCard
		// (upgrade to SI-6* possible)
		// card number range 1 500'000 16'711'680 2'000'000 1'000'000 4'000'000
		// 6'000'000
		// 499'999 999'999 16'777'215 2'999'999 1'999'999 4'999'999 6'999'999

		Long eCardId = null;
		try {
			eCardId = Long.parseLong(eCardNo);
		} catch (NumberFormatException e) {
			throw new DownloadException("InvalidSICardNumber");
		}

		if (eCardId >= 0 && eCardId <= 499999) {
			return ECardType.SICARD5;
		} else if (eCardId >= 500000 && eCardId <= 999999) {
			return ECardType.SICARD6;
		}
		// special case: SI Cards for OL WM 2003
		else if (eCardId >= 2003000 && eCardId <= 2003799) {
			return ECardType.SICARD6;
		} else if (eCardId >= 16711680 && eCardId <= 16777215) {
			return ECardType.SICARD6Star;
		} else if (eCardId >= 2000000 && eCardId <= 2999999) {
			return ECardType.SICARD8;
		} else if (eCardId >= 1000000 && eCardId <= 1999999) {
			return ECardType.SICARD9;
		} else if (eCardId >= 4000000 && eCardId <= 4999999) {
			return ECardType.pCARD;
		} else if (eCardId >= 6000000 && eCardId <= 6999999) {
			return ECardType.tCARD;
		} else if (eCardId >= 7000001 && eCardId <= 7999999) {
			return ECardType.SICARD10;
		} else if (eCardId >= 8000001 && eCardId <= 8999999) {
			return ECardType.SIAC1;
		} else if (eCardId >= 9000001 && eCardId <= 9999999) {
			return ECardType.SICARD11;
		} else {
			throw new DownloadException("UnknownSICardType" + ": " + eCardNo);
		}
	}

}
