package com.remyoukaour.spectrogram;

public class ByteUtils {	
	public static short bytesToShort(byte[] bytes, int off, boolean bigEndian) {
		byte b1 = bytes[off], b2 = bytes[off+1];
		if (!bigEndian) {
			byte tmp = b1;
			b1 = b2;
			b2 = tmp;
		}
		return (short)(((b1 & 0xFF) << 8) | (b2 & 0xFF));
	}
	
	public static void shortToBytes(short s, boolean bigEndian, byte[] bytes,
			int off) {
		byte b1 = (byte)((s >> 8) & 0xFF);
		byte b2 = (byte)(s & 0xFF);
		if (!bigEndian) {
			byte tmp = b1;
			b1 = b2;
			b2 = tmp;
		}
		bytes[off] = b1;
		bytes[off+1] = b2;
	}
	
	public static double bytesToDouble(byte[] bytes, int off) {
		long el = 0L;
		int shift = 64;
		int lim = off + 8;
		for (int i = off; i < lim; i++) {
			shift -= 8;
			el |= (long)(bytes[i] & 0xFF) << shift;
		}
		return Double.longBitsToDouble(el);
	}
	
	public static void doubleToBytes(double v, byte[] bytes, int off) {
		long el = Double.doubleToRawLongBits(v);
		int shift = 64;
		int lim = off + 8;
		for (int i = off; i < lim; i++) {
			shift -= 8;
			bytes[i] = (byte)((el >> shift) & 0xFF);
		}
	}
}
