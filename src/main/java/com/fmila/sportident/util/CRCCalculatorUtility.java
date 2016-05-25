package com.fmila.sportident.util;

public final class CRCCalculatorUtility {
	
	private static final int POLY = 0x8005;
	private static final int BITF = 0x8000;

	private CRCCalculatorUtility() {
	}

	public static int crc(byte[] buffer) {
		int count = buffer.length;
		int i;
		int j;
		int tmp; // 16 Bit
		int val; // 16 Bit
		int ptr = 0;

		tmp = (short) (buffer[ptr++] << 8 | (buffer[ptr++] & 0xFF));

		if (count > 2) {
			for (i = count / 2; i > 0; i--) { // only even counts !!! and more
												// than 4
				if (i > 1) {
					val = (buffer[ptr++] << 8 | (buffer[ptr++] & 0xFF));
				} else {
					if (count % 2 == 1) {
						val = buffer[count - 1] << 8;
					} else {
						val = 0; // last value with 0 // last 16 bit value
					}
				}

				for (j = 0; j < 16; j++) {
					if ((tmp & BITF) != 0) {
						tmp <<= 1;

						if ((val & BITF) != 0) {
							tmp++; // rotate carry
						}
						tmp ^= POLY;
					} else {
						tmp <<= 1;

						if ((val & BITF) != 0) {
							tmp++; // rotate carry
						}
					}
					val <<= 1;
				}
			}
		}
		return (tmp & 0xFFFF);
	}

}
