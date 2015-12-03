package com.fmila.sportident.util;

public class StringUtility {

	public static String lpad(String s, String fill, int len) {
		if (s == null || fill == null || s.length() >= len || fill.length() == 0) {
			return s;
		}
		StringBuilder buf = new StringBuilder(s);
		while (buf.length() < len) {
			buf.insert(0, fill);
		}
		return buf.substring(buf.length() - len, buf.length());
	}

	public static String substring(String s, int start) {
		if (s == null || start >= s.length()) {
			return "";
		}
		return s.substring(start);
	}

	public static String substring(String s, int start, int len) {
		if (s == null || start >= s.length()) {
			return "";
		}
		len = Math.min(s.length() - start, len);
		return s.substring(start, start + len);
	}

}
