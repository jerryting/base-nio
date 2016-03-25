package com.polarlight.commons.basenio.utils;

import java.io.UnsupportedEncodingException;

public class ByteConverter {
	private final static String CHAR_SET = "UTF-8";
	public enum ByteOrder {
		Little_Endian, Big_Endian
	}

	public static short decodeShort(final byte[] source, int nIndex,
			ByteOrder byteOrder) {
		short result = 0;

		if (byteOrder == ByteOrder.Little_Endian) {
			result = (short) (((source[nIndex + 1] & 0xff) << 8) | (source[nIndex] & 0xff));
		} else // ByteOrder.Big_Endian
		{
			result = (short) (((source[nIndex] & 0xff) << 8) | (source[nIndex + 1] & 0xff));
		}
		return result;
	}

	public static int decodeInt(final byte[] source, int nIndex, ByteOrder byteOrder) {
		int result = 0;

		if (byteOrder == ByteOrder.Little_Endian) {
			result = (((source[nIndex + 3] & 0xff) << 24)
					| ((source[nIndex + 2] & 0xff) << 16)
					| ((source[nIndex + 1] & 0xff) << 8) | (source[nIndex] & 0xff));
		} else // ByteOrder.Big_Endian
		{
			result = (int) (((source[nIndex] & 0xff) << 24)
					| ((source[nIndex + 1] & 0xff) << 16)
					| ((source[nIndex + 2] & 0xff) << 8) | (source[nIndex + 3] & 0xff));
		}
		return result;
	}

	public static long decodeLong(final byte[] source, int nIndex, ByteOrder byteOrder) {
		long result = 0;

		if (byteOrder == ByteOrder.Little_Endian) {
			result = (long) (((source[nIndex + 7] & 0xffl) << 56)
					| ((source[nIndex + 6] & 0xffl) << 48)
					| ((source[nIndex + 5] & 0xffl) << 40)
					| ((source[nIndex + 4] & 0xffl) << 32)
					| ((source[nIndex + 3] & 0xffl) << 24)
					| ((source[nIndex + 2] & 0xffl) << 16)
					| ((source[nIndex + 1] & 0xffl) << 8) | (source[nIndex] & 0xffl));
		} else // ByteOrder.Big_Endian
		{
			result = (long) (((source[nIndex] & 0xffl) << 56)
					| ((source[nIndex + 1] & 0xffl) << 48)
					| ((source[nIndex + 2] & 0xffl) << 40)
					| ((source[nIndex + 3] & 0xffl) << 32)
					| ((source[nIndex + 4] & 0xffl) << 24)
					| ((source[nIndex + 5] & 0xffl) << 16)
					| ((source[nIndex + 6] & 0xffl) << 8) | (source[nIndex + 7] & 0xffl));
		}
		return result;
	}

	public static String decodeString(final byte[] source, int nIndex, int len) {
		String res;
		int nMaxLen = (source.length < (nIndex + len)) ? source.length
				: (nIndex + len);
		for (int i = nIndex; i < nMaxLen; i++) {
			if (source[i] == 0) {
				len = i - nIndex;
				break;
			}
		}
		try {
			res = new String(source, nIndex, len, CHAR_SET);
		} catch (UnsupportedEncodingException ex) {
			res = new String(source);
		}
		if (res != null)
			res = res.trim();
		return res;
	}

	public static int encodeShort(byte[] goal, int nIndex, ByteOrder byteOrder,
			short datavalue) {
		if (byteOrder == ByteOrder.Little_Endian) {
			goal[nIndex] = (byte) (datavalue & 0xff);
			goal[nIndex + 1] = (byte) (datavalue >> 8 & 0xff);
		} else // ByteOrder.Big_Endian
		{
			goal[nIndex + 1] = (byte) (datavalue & 0xff);
			goal[nIndex] = (byte) (datavalue >> 8 & 0xff);
		}
		return 2;
	}

	public static int encodeInt(byte[] goal, int nIndex, ByteOrder byteOrder,
			int datavalue) {
		if (byteOrder == ByteOrder.Little_Endian) {
			goal[nIndex] = (byte) (datavalue & 0xff);
			goal[nIndex + 1] = (byte) (datavalue >> 8 & 0xff);
			goal[nIndex + 2] = (byte) (datavalue >> 16 & 0xff);
			goal[nIndex + 3] = (byte) (datavalue >> 24 & 0xff);
		} else // ByteOrder.Big_Endian
		{
			goal[nIndex + 3] = (byte) (datavalue & 0xff);
			goal[nIndex + 2] = (byte) (datavalue >> 8 & 0xff);
			goal[nIndex + 1] = (byte) (datavalue >> 16 & 0xff);
			goal[nIndex] = (byte) (datavalue >> 24 & 0xff);
		}
		return 4;
	}

	public static int encodeLong(byte[] goal, int nIndex, ByteOrder byteOrder,
			long datavalue) {
		if (byteOrder == ByteOrder.Little_Endian) {
			goal[nIndex] = (byte) (datavalue & 0xff);
			goal[nIndex + 1] = (byte) (datavalue >> 8 & 0xff);
			goal[nIndex + 2] = (byte) (datavalue >> 16 & 0xff);
			goal[nIndex + 3] = (byte) (datavalue >> 24 & 0xff);
			goal[nIndex + 4] = (byte) (datavalue >> 32 & 0xff);
			goal[nIndex + 5] = (byte) (datavalue >> 40 & 0xff);
			goal[nIndex + 6] = (byte) (datavalue >> 48 & 0xff);
			goal[nIndex + 7] = (byte) (datavalue >> 56 & 0xff);
		} else // ByteOrder.Big_Endian
		{
			goal[nIndex + 7] = (byte) (datavalue & 0xff);
			goal[nIndex + 6] = (byte) (datavalue >> 8 & 0xff);
			goal[nIndex + 5] = (byte) (datavalue >> 16 & 0xff);
			goal[nIndex + 4] = (byte) (datavalue >> 24 & 0xff);
			goal[nIndex + 3] = (byte) (datavalue >> 32 & 0xff);
			goal[nIndex + 2] = (byte) (datavalue >> 40 & 0xff);
			goal[nIndex + 1] = (byte) (datavalue >> 48 & 0xff);
			goal[nIndex] = (byte) (datavalue >> 56 & 0xff);
		}
		return 8;
	}

	public static int encodeString(byte[] goal, int nIndex, int len,
			String datavalue) {
		byte[] temp;
		try {
			temp = datavalue.getBytes(CHAR_SET);
		} catch (UnsupportedEncodingException ex) {
			temp = datavalue.getBytes();
		}
		int i;
		for (i = 0; i < temp.length; i++) {
			goal[nIndex + i] = temp[i];
		}
		while (i < len) {
			goal[nIndex + i] = 0;
			i++;
		}
		return len;
	}
	/**
	 * 效率 不及 System.arraycopy 后续考虑替换
	 * @param dst
	 * @param src
	 * @param dstPos
	 * @param len
	 * @return
	 */
	public static int byteCopy(byte[] dst, byte[] src , int dstPos, int len){
		int i;
		for(i=0; i<len; i++){
			dst[dstPos+i] = src[i];
		}
		return i;
	}
}