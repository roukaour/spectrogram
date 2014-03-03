package com.remyoukaour.spectrogram;

import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public abstract class SampledData {
	private static final int BUFFERED_SAMPLES = 2048;
	
	public abstract int size();
	public abstract double get(int i);
	public abstract double[] get(int start, int length);
	
	public AudioInputStream toStream(int hertz) {
		AudioFormat format = new AudioFormat(hertz, 16, 1, true, true);
		int n = size();
		byte[] data = new byte[n * 2];
		int off = 0;
		for (int i = 0; i < n; i += BUFFERED_SAMPLES) {
			double[] buffer = get(i, BUFFERED_SAMPLES);
			for (int j = 0; j < BUFFERED_SAMPLES && i + j < n; j++, off += 2) {
				double v = buffer[j] * 32768.0;
				short s = v > Short.MAX_VALUE ? Short.MAX_VALUE :
						v < Short.MIN_VALUE ? Short.MIN_VALUE : (short)v;
				ByteUtils.shortToBytes(s, true, data, off);
			}
		}
		return new AudioInputStream(new ByteArrayInputStream(data), format, n);
	}
}
