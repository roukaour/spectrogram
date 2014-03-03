package com.remyoukaour.spectrogram;

import java.awt.*;
import javax.swing.JPanel;

public abstract class SignalPanel extends JPanel {
	private static final long serialVersionUID = 7742340141168006910L;

	public static final long NO_CURSOR = -1;
	
	protected Signal signal;
	protected int zoom;
	protected int prefHeight;
	protected long cursor;
	
	public SignalPanel() {
		this.signal = null;
		this.zoom = 1;
		this.prefHeight = 0;
		this.cursor = -1;
		rescale();
	}
	
	public Signal getSignal() {
		return signal;
	}
	
	public void setSignal(Signal signal) {
		this.signal = signal;
		rescale();
	}
	
	public int getPrefHeight() {
		return prefHeight;
	}
	
	public void setPrefHeight(int prefHeight) {
		this.prefHeight = prefHeight;
		rescale();
	}
	
	public int getZoom() {
		return zoom;
	}
	
	public void zoomIn() {
		if (zoom > 1)
			zoom /= 2;
		rescale();
	}
	
	public void zoomOut() {
		zoom *= 2;
		rescale();
	}
	
	public void zoomTo(int zoom) {
		this.zoom = zoom;
		rescale();
	}
	
	protected void rescale() {
		int prefWidth = signal != null ? (int)(signal.getNumSamples() / zoom) : 0;
		setPreferredSize(new Dimension(prefWidth, prefHeight));
		revalidate();
		repaint();
	}
	
	protected void drawCursor(Graphics g) {
		if (cursor != NO_CURSOR) {
			int cx = (int)(cursor / zoom);
			g.setColor(Color.YELLOW);
			g.drawLine(cx, 0, cx, getHeight());
		}
	}
	
	public synchronized void updateCursor(long microseconds) {
		int h = (int)getHeight();
		if (microseconds == NO_CURSOR) {
			cursor = NO_CURSOR;
			repaint();
			return;
		}
		repaint((int)(cursor / zoom) - 1, 0, 3, h);
		double seconds = microseconds / 1000000.0;
		cursor = (long)(signal.getHertz() * seconds);
		repaint((int)(cursor / zoom) - 1, 0, 3, h);
	}
}
