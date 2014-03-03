package com.remyoukaour.spectrogram;

import java.io.IOException;
import javax.sound.sampled.*;
import javax.swing.JOptionPane;

public class Player extends Thread {
	private static final int BUFFERED_FRAMES = 1024;
	
	private final SignalWindow window;
	private final Signal signal;
	
	public Player(SignalWindow window, Signal signal) {
		this.window = window;
		this.signal = signal;
	}
	
	public void run() {
		AudioInputStream ain = null;
		AudioFormat format = null;
		SourceDataLine outputLine = null;
		try {
			ain = signal.toStream();
			format = ain.getFormat();
			outputLine = AudioSystem.getSourceDataLine(format);
			outputLine.open(format);
		}
		catch (LineUnavailableException ex) {
			JOptionPane.showMessageDialog(window, "Error: " + ex,
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		if (ain == null || outputLine == null)
			return;
		outputLine.start();
		byte[] buffer = new byte[format.getFrameSize() * BUFFERED_FRAMES];
		try {
			int n;
			while (window.isPlaying() && (n = ain.read(buffer)) != -1) {
				outputLine.write(buffer, 0, n);
				window.updateCursor(outputLine.getMicrosecondPosition());
			}
		}
		catch (IOException ex) {
			JOptionPane.showMessageDialog(window, "Error: " + ex.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		outputLine.drain();
		outputLine.close();
		window.updateCursor(SignalPanel.NO_CURSOR);
		window.stop();
	}
}
