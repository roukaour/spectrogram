package com.remyoukaour.spectrogram;

public class SampledMemoryData extends SampledData {
	private final double[] samples;
	
	public SampledMemoryData(double[] samples) {
		this.samples = samples;
	}
	
	public int size() {
		return samples.length;
	}
	
	public double get(int i) {
		return samples[i];
	}
	
	public double[] get(int start, int length) {
		double[] data = new double[length];
		if (start < 0) {
			length += start;
			start = 0;
		}
		if (start + length > samples.length) {
			length = samples.length - start;
		}
		System.arraycopy(samples, start, data, 0, length);
		return data;
	}
}
