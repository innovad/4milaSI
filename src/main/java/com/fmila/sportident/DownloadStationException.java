package com.fmila.sportident;

public class DownloadStationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DownloadStationException(String string) {
		super(string);
	}

	public DownloadStationException() {
		super();
	}

	public DownloadStationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DownloadStationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DownloadStationException(Throwable cause) {
		super(cause);
	}
	
	

}
