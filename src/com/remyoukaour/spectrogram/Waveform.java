package com.remyoukaour.spectrogram;

import java.awt.*;

public class Waveform extends SignalPanel {
	public static final long serialVersionUID = 1220411153L;
	
	private static final int BUFFERED_SAMPLES = 1024;
	
	public Waveform() {
		super();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (signal == null)
			return;
		Color oldColor = g.getColor();
		g.setColor(Color.BLUE);
		int hh = getHeight() / 2;
		Rectangle bounds = g.getClipBounds();
		int minX = (int)bounds.getX() - 1;
		int maxX = (int)(bounds.getX() + bounds.getWidth());
		int pp = hh;
		int n = signal.getNumSamples();
		drawing:
		for (int i = 0; i < n; i += BUFFERED_SAMPLES) {
			double[] data = signal.getSamples(i, BUFFERED_SAMPLES);
			for (int j = 0; j < BUFFERED_SAMPLES; j += zoom) {
				int x = (i + j) / zoom;
				if (x < minX || x > maxX)
					continue;
				for (int k = 0; k < zoom; k++) {
					if (i + j + k >= n)
						break drawing;
					if (j + k >= BUFFERED_SAMPLES)
						break;
					double v = data[j + k];
					int p = (int)(v * hh);
					g.drawLine(x, hh + pp, x, hh + p);
					pp = p;
				}
			}
		}
		drawCursor(g);
		g.setColor(oldColor);
	}
}
