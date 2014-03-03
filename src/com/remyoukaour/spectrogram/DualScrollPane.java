package com.remyoukaour.spectrogram;

import java.awt.BorderLayout;
import javax.swing.*;

public class DualScrollPane extends JPanel {
	private static final long serialVersionUID = 8254516895802138993L;
	
	private final JSplitPane splitPane;
	
	public DualScrollPane(JScrollPane pane1, JScrollPane pane2, boolean shareH, boolean shareV) {
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane1, pane2);
		splitPane.setResizeWeight(0.5);
		setLayout(new BorderLayout());
		add(splitPane);
		if (shareH)
			pane1.getHorizontalScrollBar().setModel(pane2.getHorizontalScrollBar().getModel());
		if (shareV)
			pane1.getVerticalScrollBar().setModel(pane2.getVerticalScrollBar().getModel());
	}
}
