package com.remyoukaour.spectrogram;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class Spectrogram extends SignalPanel {
	private static final long serialVersionUID = -5442088111430822270L;
	
	private static final double TAU = Math.PI * 2;
	private static final int MAX_COLOR = 0xFF;
	
	private boolean logAxis, showPhase;
	private int bins, lbins[];
	private double overlap, maxPower, step, cf;
	private WindowFunction window;
	
	private static double getPower(int rgb, double maxPower) {
		double step = Math.log1p(maxPower) / 4;
		double cf = MAX_COLOR / step;
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		double k = 0.0;
		if (r > 0 && g > 0 && b > 0)
			k = g / cf + step * 3;
		else if (r > 0)
			k = r / cf + step * 2;
		else if (g > 0)
			k = g / cf + step;
		else
			k = b / cf;
		return Math.expm1(k);
	}
	
	public static void imageToAudio(Component parent, BufferedImage image,
			File f, int hertz, double overlap) throws IOException {
		String name = f.getName();
		int cols = image.getWidth();
		int bins = image.getHeight();
		IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(parent,
				"Converting image to " + name + "...", null, 0, cols);
		monitor.setMillisToDecideToPopup(250);
		monitor.setMillisToPopup(250);
		double maxPower = bins * bins / 4.0;
		//double freqF = (double)hertz / bins / 2; // unused
		int sw = (int)(bins * 2 * overlap);
		double[] samples = new double[sw * cols];
		double[] col = new double[bins * 2];
		DoubleFFT_1D fft = new DoubleFFT_1D(col.length);
		int index = 0;
		for (int c = 0; c < cols; c++) {
			monitor.setProgress(c);
			for (int r = 0; r < bins; r++) {
				if (monitor.isCanceled())
					throw new IOException("failed to convert sample");
				int rgb = image.getRGB(c, bins - r - 1);
				double power = getPower(rgb, maxPower);
				double amplitude = Math.sqrt(power);
				//double frequency = r * freqF; // unused
				double phase = Math.random() * TAU - Math.PI;
				col[2*r] = amplitude * Math.cos(phase);
				col[2*r+1] = amplitude * Math.sin(phase);
			}
			fft.realInverse(col, true);
			int start = 0; // keep beginning samples
			int end = start + sw;
			for (int s = start; s < end; s++) {
				samples[index++] = col[s] * 2;
			}
		}
		monitor.makeIndeterminate();
		SampledMemoryData data = new SampledMemoryData(samples);
		AudioInputStream stream = data.toStream(hertz);		
		AudioSystem.write(stream, AudioFileFormat.Type.WAVE, f);
		monitor.close();
	}
	
	public Spectrogram(int bins, double overlap, WindowFunction window,
			boolean logAxis, boolean fullHeight, boolean showPhase) {
		super();
		this.bins = bins;
		this.overlap = overlap;
		this.window = window;
		this.logAxis = logAxis;
		this.showPhase = showPhase;
		setFullHeight(fullHeight);
		recalculateBinsCache();
	}
	
	private void recalculateBinsCache() {
		this.maxPower = bins * bins / 4.0;
		this.step = Math.log1p(maxPower) / 4;
		this.cf = MAX_COLOR / step;
		this.lbins = new int[bins];
		double bf = (bins - 1) / Math.log(bins);
		for (int i = 0; i < bins; i++) {
			lbins[i] = (int)(Math.log1p(i) * bf);
		}
	}
	
	public int getBins() {
		return bins;
	}
	
	public void setBins(int bins) {
		this.bins = bins;
		recalculateBinsCache();
		repaint();
	}
	
	public double getOverlap() {
		return overlap;
	}
	
	public void setOverlap(double overlap) {
		this.overlap = overlap;
		repaint();
	}
	
	public WindowFunction getWindow() {
		return window;
	}
	
	public void setWindow(WindowFunction window) {
		this.window = window;
		repaint();
	}
	
	public void setLogAxis(boolean log) {
		this.logAxis = log;
		repaint();
	}
	
	public void setFullHeight(boolean fullHeight) {
		setPrefHeight(fullHeight ? bins : 0);
	}
	
	public void showPhase(boolean showPhase) {
		this.showPhase = showPhase;
		repaint();
	}
	
	public double getSpectrumWidth() {
		return bins * 2 * overlap;
	}
	
	private Color powerColor(double power) {
		double k = Math.log1p(power);
		int r = 0, g = 0, b = 0;
		if (k < step) {
			// black to blue
			b = (int)(k * cf);
		}
		else if (k < step * 2) {
			// blue to green
			k -= step;
			g = (int)(k * cf);
			b = MAX_COLOR - g;
		}
		else if (k < step * 3) {
			// green to red
			k -= step * 2;
			r = (int)(k * cf);
			g = MAX_COLOR - r;
		}
		else {
			// red to white
			k -= step * 3;
			r = MAX_COLOR;
			g = (int)(k * cf);
			b = g;
		}
		if (r > MAX_COLOR) r = MAX_COLOR;
		if (g > MAX_COLOR) g = MAX_COLOR;
		if (b > MAX_COLOR) b = MAX_COLOR;
		return new Color(r, g, b);
	}
	
	private Color phaseColor(double phase) {
		int c = (int)(phase * MAX_COLOR);
		if (c < 0) c = 0;
		if (c > MAX_COLOR) c = MAX_COLOR;
		return new Color(c, c, c);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (signal == null)
			return;
		Color oldColor = g.getColor();
		double xf = getSpectrumWidth();
		double yf = (double)getHeight() / (bins - 1);
		int sw = (int)(xf / zoom) + 1;
		int sh = (int)Math.ceil(yf);
		int inc = (int)xf;
		Rectangle bounds = g.getClipBounds();
		int minX = (int)bounds.getX() - sw;
		int maxX = (int)(bounds.getX() + bounds.getWidth());
		int n = signal.getNumSamples();
		int limX = n / zoom - sw;
		n += inc; // overshoot
		for (int i = 0; i < n; i += inc) {
			int x = (int)((i - xf / 2) / zoom);
			if (x < minX || x > maxX)
				continue;
			Spectrum spectrum = signal.getSpectrum(i, bins, window, showPhase);
			if (x > limX) {
				sw -= x - limX;
				i = n;
			}
			for (int j = 0; j < bins; j++) {
				int y = (int)(j * yf);
				int s = bins - (logAxis ? lbins[j] : j) - 1;
				double v = spectrum.get(s);
				g.setColor(showPhase ? phaseColor(v) : powerColor(v));
				g.fillRect(x, y, sw, sh);
			}
		}
		drawCursor(g);
		g.setColor(oldColor);
	}
	
	public void save(Component parent, File f, String ext) throws IOException {
		if (signal == null)
			throw new IOException("no signal to save");
		String name = f.getName();
		int n = signal.getNumSamples();
		int sw = (int)getSpectrumWidth();
		int width = n / sw + 1;
		IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(parent,
				"Saving " + name + "...", null, 0, width);
		monitor.setMillisToDecideToPopup(250);
		monitor.setMillisToPopup(250);
		BufferedImage image = new BufferedImage(width, bins,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			monitor.setProgress(x);
			Spectrum spectrum = signal.getSpectrum(x * sw, bins, window, showPhase);
			for (int y = 0; y < bins; y++) {
				if (monitor.isCanceled())
					throw new IOException("failed to get pixel");
				int s = bins - (logAxis ? lbins[y] : y) - 1;
				double v = spectrum.get(s);
				Color c = showPhase ? phaseColor(v) : powerColor(v);
				image.setRGB(x, y, c.getRGB());
			}
		}
		monitor.makeIndeterminate();
		ImageIO.write(image, ext, f);
		monitor.close();
	}
}
