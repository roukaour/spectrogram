package com.remyoukaour.spectrogram;

import javax.swing.UIManager;

public class SpectrogramMain {
	private static final String PROGRAM_TITLE = "Spectrogram";
	private static final int PROGRAM_WIDTH = 720;
	private static final int PROGRAM_HEIGHT = 405;
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ex) {}
		SignalWindow gui = new SignalWindow(PROGRAM_TITLE);
		gui.setSize(PROGRAM_WIDTH, PROGRAM_HEIGHT);
		gui.setVisible(true);
	}
}
