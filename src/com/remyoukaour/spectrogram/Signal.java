package com.remyoukaour.spectrogram;

import java.awt.Component;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.*;

public class Signal {
	private static final int BUFFERED_DOUBLES = 2048;

	private final String name;
	private final SampledData samples;
	private final int hertz;

	public static Signal fromFile(Component parent, File f, boolean useMemory)
			throws UnsupportedAudioFileException, IOException {
		String name = f.getName();
		SignalIterator iter = new SignalIterator(f);
		int length = iter.getRemaining();
		int hertz = iter.getHertz();
		boolean decoded = iter.isDecoded();
		ProgressMonitor monitor = null;
		if (decoded) {
			monitor = new IndeterminateProgressMonitor(parent,
					"Loading " + name + "...", null);
		}
		else {
			ProgressMonitorInputStream pin = (ProgressMonitorInputStream)
					iter.getStream(parent, "Loading " + name + "...");
			monitor = pin.getProgressMonitor();
			monitor.setMillisToDecideToPopup(250);
			monitor.setMillisToPopup(250);
		}
		SampledData data = null;
		if (useMemory) {
			double[] samples = new double[length];
			for (int i = 0; i < length; i++) {
				Double v = iter.next();
				if (v == null || monitor.isCanceled())
					throw new IOException("failed to read sample");
				samples[i] = v;
			}
			iter.close();
			data = new SampledMemoryData(samples);
		}
		else {
			File temp = File.createTempFile("signal_" + name, null);
			temp.deleteOnExit();
			int width = 8;
			BufferedRandomAccessFile braf = new BufferedRandomAccessFile(temp,
					"rw", width * BUFFERED_DOUBLES);
			byte[] bytes = new byte[width];
			for (int i = 0; i < length; i++) {
				Double v = iter.next();
				if (v == null || monitor.isCanceled()) {
					braf.close();
					throw new IOException("failed to read sample");
				}
				ByteUtils.doubleToBytes(v, bytes, 0);
				braf.writeBuffered(bytes);
			}
			braf.flushBuffer();
			braf.close();
			iter.close();
			RandomAccessFile raf = new RandomAccessFile(temp, "r");
			data = new SampledDiskData(raf, length);
		}
		monitor.close();
		return new Signal(name, data, hertz);
	}

	protected Signal(String name, SampledData samples, int hertz) {
		this.name = name;
		this.samples = samples;
		this.hertz = hertz;
	}

	public String getName() {
		return name;
	}

	public int getHertz() {
		return hertz;
	}

	public int getNumSamples() {
		return samples.size();
	}

	public double[] getSamples(int start, int length) {
		return samples.get(start, length);
	}

	public Spectrum getSpectrum(int i, int length) {
		return getSpectrum(i, length, null, false);
	}

	public Spectrum getSpectrum(int i, int length, WindowFunction window,
			boolean showPhase) {
		double[] samples = getSamples(i - length, length * 2);
		return new Spectrum(samples, i, window, showPhase);
	}

	public AudioInputStream toStream() {
		return samples.toStream(hertz);
	}
}
