package com.remyoukaour.spectrogram;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class Spectrum {
	private static int cachedLength = -1;
	private static DoubleFFT_1D fft = null;
	
	private final double[] spectrum;
	private final int time;
	
	public Spectrum(double[] samples, int time) {
		this(samples, time, null, false);
	}
	
	public Spectrum(double[] samples, int time, WindowFunction window,
			boolean showPhase) {
		if (window != null)
			window.window(samples);
		int n = samples.length;
		if (n != cachedLength) {
			cachedLength = n;
			fft = new DoubleFFT_1D(n);
		}
		fft.realForward(samples);
		n /= 2;
		spectrum = new double[n];
		for (int i = 0; i < n; i++) {
			double re = samples[2*i], im = samples[2*i+1];
			spectrum[i] = showPhase ? (Math.atan2(im, re) + Math.PI) / 2 :
				re * re + im * im;
		}
		this.time = time;
	}
	
	public double get(int i) {
		return spectrum[i];
	}
	
	public int getTime() {
		return time;
	}
}
