package com.remyoukaour.spectrogram;

import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;

public class IndeterminateProgressMonitor extends ProgressMonitor {
	public IndeterminateProgressMonitor(Component parent, Object message,
			String note) {
		super(parent, message, note, 0, 100);
		setMillisToDecideToPopup(0);
		setMillisToPopup(0);
		setProgress(50);
		makeIndeterminate();
	}
	
	public IndeterminateProgressMonitor(Component parent, Object message,
			String note, int min, int max) {
		super(parent, message, note, min, max);
	}
	
	public void makeIndeterminate() {
		try {
			JProgressBar bar = (JProgressBar)getAccessibleContext().getAccessibleChild(1);
			bar.setIndeterminate(true);
		}
		catch (Exception ex) {}
	}
}
