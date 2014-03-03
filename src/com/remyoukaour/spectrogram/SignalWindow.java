/*
 * Overlap iFFT blocks instead of taking half the samples
 * Optionally use phase info when encoding/decoding spectrogram
 * Support FLAC files
 * Support bit depths other than 16
 * Write a help file
 * http://arss.sourceforge.net/
 * http://devrand.org/view/imageSpectrogram
 * http://www.cs.unm.edu/~brayer/vision/fourier.html
 * http://www.44342.com/dsp-f288-t15339-p1.htm
 * http://dsp.ucsd.edu/students/present-students/mik/specanalyzer/01_intro.htm
 * http://labrosa.ee.columbia.edu/matlab/pvoc/
 * http://cobweb.ecn.purdue.edu/~malcolm/interval/1994-014/#4
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.72.6336&rep=rep1&type=pdf
 */

package com.remyoukaour.spectrogram;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import org.kc7bfi.jflac.apps.ExtensionFileFilter;

public class SignalWindow extends JFrame implements ActionListener, ItemListener {
	private static final long serialVersionUID = 5825907294405308517L;
	
	/*** Constants ***/
	
	private static final int DEFAULT_BINS = 2048;
	private static double DEFAULT_OVERLAP = 0.5;
	private static final WindowFunction DEFAULT_WINDOW = WindowFunction.HAMMING;
	private static final String[]
			AUDIO_READ_EXTENSIONS = {"wav", "au", "mp3", "ogg", "oga"/*, "flac"*/},
			AUDIO_WRITE_EXTENSIONS = {"wav"},
			IMAGE_EXTENSIONS = {"png"};
	
	/*** Members ***/
	
	private final String name;
	private final Waveform waveform = new Waveform();
	private final Spectrogram spectrogram = new Spectrogram(DEFAULT_BINS,
			DEFAULT_OVERLAP, DEFAULT_WINDOW, false, false, false);
	private boolean playing = false;
	private boolean busy = false;
	
	/*** Components ***/
	
	private final JMenuBar menuBar = new JMenuBar();
	private final JMenu fileMenu = new JMenu("File");
	private final JMenuItem open = new JMenuItem("Open audio...", KeyEvent.VK_O);
	private final JMenuItem close = new JMenuItem("Close audio", KeyEvent.VK_C);
	private final JMenuItem save = new JMenuItem("Save image...", KeyEvent.VK_S);
	private final JMenuItem convert = new JMenuItem("Image to audio...", KeyEvent.VK_A);
	private final JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
	private final JMenu viewMenu = new JMenu("View");
	private final JMenuItem zoomIn = new JMenuItem("Zoom in", KeyEvent.VK_I);
	private final JMenuItem zoomOut = new JMenuItem("Zoom out", KeyEvent.VK_O);
	private final JMenuItem zoomMax = new JMenuItem("Zoom max", KeyEvent.VK_X);
	private final JMenuItem zoomFit = new JMenuItem("Zoom fit", KeyEvent.VK_F);
	private final JMenu spectrogramMenu = new JMenu("Spectrogram");
	private final JMenu setBins = new JMenu("Frequency bins");
	private final ButtonGroup binsGroup = new ButtonGroup();
	private HashMap<JRadioButtonMenuItem, Integer> binsItems =
			new HashMap<JRadioButtonMenuItem, Integer>();
	private final JMenu setOverlap = new JMenu("Spectrum overlap");
	private final ButtonGroup overlapGroup = new ButtonGroup();
	private HashMap<JRadioButtonMenuItem, Double> overlapItems =
			new HashMap<JRadioButtonMenuItem, Double>();
	private final JMenu setWindow = new JMenu("Window function");
	private final ButtonGroup windowGroup = new ButtonGroup();
	private HashMap<JRadioButtonMenuItem, WindowFunction> windowItems =
		new HashMap<JRadioButtonMenuItem, WindowFunction>();
	private final JCheckBoxMenuItem logAxis = new JCheckBoxMenuItem("Log frequency");
	private final JCheckBoxMenuItem fullHeight = new JCheckBoxMenuItem("Full height");
	private final JCheckBoxMenuItem showPhase = new JCheckBoxMenuItem("Show phase");
	private final JMenu playbackMenu = new JMenu("Playback");
	private final JMenuItem play = new JMenuItem("Play", KeyEvent.VK_P);
	private final JMenuItem stop = new JMenuItem("Stop", KeyEvent.VK_S);
	private final JMenu memoryMenu = new JMenu("Memory");
	private final JCheckBoxMenuItem audioMemory = new JCheckBoxMenuItem("Store audio in memory");
	private final JMenuItem garbageCollect = new JMenuItem("Garbage collect", KeyEvent.VK_G);
	private final JMenu helpMenu = new JMenu("Help");
	private final JMenuItem help = new JMenuItem("Help", KeyEvent.VK_H);
	private final JMenuItem about = new JMenuItem("About", KeyEvent.VK_A);
	private final JScrollPane waveformScroll = new JScrollPane();
	private final JScrollPane spectrogramScroll = new JScrollPane();
	private final DualScrollPane dualScroll =
			new DualScrollPane(waveformScroll, spectrogramScroll, true, false);
	private final StatusBar statusBar = new StatusBar();
	private final JFileChooser fc = new JFileChooser();
	private final FileFilter audioReadFilter =
			new ExtensionFileFilter(AUDIO_READ_EXTENSIONS, "Audio files");
	private final FileFilter audioWriteFilter =
			new ExtensionFileFilter(AUDIO_WRITE_EXTENSIONS, "Audio files");
	private final FileFilter imageFilter =
			new ExtensionFileFilter(IMAGE_EXTENSIONS, "Image files");
	
	/*** Constructors ***/
	
	public SignalWindow(String title) {
		super(title);
		this.name = title;
		// configuration
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		// components
		setupMenuBar();
		setupLayout();
	}
	
	/*** Setup ***/
	
	private void setupMenuBar() {
		// mnemonics
		fileMenu.setMnemonic(KeyEvent.VK_F);
		viewMenu.setMnemonic(KeyEvent.VK_V);
		spectrogramMenu.setMnemonic(KeyEvent.VK_S);
		playbackMenu.setMnemonic(KeyEvent.VK_P);
		memoryMenu.setMnemonic(KeyEvent.VK_M);
		helpMenu.setMnemonic(KeyEvent.VK_H);
		setBins.setMnemonic(KeyEvent.VK_B);
		setOverlap.setMnemonic(KeyEvent.VK_O);
		setWindow.setMnemonic(KeyEvent.VK_W);
		logAxis.setMnemonic(KeyEvent.VK_L);
		fullHeight.setMnemonic(KeyEvent.VK_H);
		showPhase.setMnemonic(KeyEvent.VK_P);
		audioMemory.setMnemonic(KeyEvent.VK_M);
		// accelerators
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		convert.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		zoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
		zoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
		zoomMax.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, ActionEvent.CTRL_MASK));
		zoomFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_END, ActionEvent.CTRL_MASK));
		logAxis.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		fullHeight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		showPhase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		play.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		stop.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		audioMemory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		garbageCollect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		// structure
		setJMenuBar(menuBar);
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(spectrogramMenu);
		menuBar.add(playbackMenu);
		menuBar.add(memoryMenu);
		menuBar.add(helpMenu);
		fileMenu.add(open);
		fileMenu.add(close);
		fileMenu.add(save);
		fileMenu.add(convert);
		fileMenu.add(exit);
		viewMenu.add(zoomIn);
		viewMenu.add(zoomOut);
		viewMenu.add(zoomMax);
		viewMenu.add(zoomFit);
		spectrogramMenu.add(setBins);
		spectrogramMenu.add(setOverlap);
		spectrogramMenu.add(setWindow);
		spectrogramMenu.add(logAxis);
		spectrogramMenu.add(fullHeight);
		spectrogramMenu.add(showPhase);
		playbackMenu.add(play);
		playbackMenu.add(stop);
		memoryMenu.add(audioMemory);
		memoryMenu.add(garbageCollect);
		helpMenu.add(help);
		helpMenu.add(about);
		// listeners
		open.addActionListener(this);
		close.addActionListener(this);
		save.addActionListener(this);
		convert.addActionListener(this);
		exit.addActionListener(this);
		play.addActionListener(this);
		stop.addActionListener(this);
		zoomIn.addActionListener(this);
		zoomOut.addActionListener(this);
		zoomMax.addActionListener(this);
		zoomFit.addActionListener(this);
		logAxis.addItemListener(this);
		fullHeight.addItemListener(this);
		showPhase.addItemListener(this);
		garbageCollect.addActionListener(this);
		help.addActionListener(this);
		about.addActionListener(this);
		// components
		setupBinMenus();
		setupOverlapMenus();
		setupWindowMenus();
	}
	
	private void setupBinMenus() {
		int[] binsValues = new int[] {32, 64, 128, 256, 512, 1024, 2048, 4096,
				8000, 8192, 11025, 16384, 22050, 32768, 44100};
		for (int i = 0; i < binsValues.length; i++) {
			int value = binsValues[i];
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(FormatUtils.formatInt(value));
			setBins.add(item);
			item.addActionListener(this);
			binsGroup.add(item);
			binsItems.put(item, value);
			if (value == DEFAULT_BINS)
				item.setSelected(true);
		}
	}
	
	private void setupOverlapMenus() {
		double[] overlapValues = new double[] {1.0, 0.75, 0.5, 0.25, 0.125,
				0.0625, 0.03125, 0.015625};
		for (int i = 0; i < overlapValues.length; i++) {
			double value = overlapValues[i];
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(FormatUtils.formatPercent(1 - value));
			setOverlap.add(item);
			item.addActionListener(this);
			overlapGroup.add(item);
			overlapItems.put(item, value);
			if (value == DEFAULT_OVERLAP)
				item.setSelected(true);
		}
	}
	
	private void setupWindowMenus() {
		WindowFunction[] windowValues = WindowFunction.values();
		for (int i = 0; i < windowValues.length; i++) {
			WindowFunction value = windowValues[i];
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(value.getName());
			setWindow.add(item);
			item.addActionListener(this);
			windowGroup.add(item);
			windowItems.put(item, value);
			if (value == DEFAULT_WINDOW)
				item.setSelected(true);
		}
	}
	
	private void setupLayout() {
		// structure
		setLayout(new BorderLayout());
		add(dualScroll, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
		// configuration
		waveformScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		waveformScroll.setViewportView(waveform);
		spectrogramScroll.setViewportView(spectrogram);
	}
	
	/*** Methods ***/
	
	private void setSignal(Signal signal) {
		setTitle(signal == null ? name : signal.getName() + " - " + name);
		waveform.setSignal(signal);
		spectrogram.setSignal(signal);
		updateStatus();
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
	private void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	private void updateStatus() {
		String status = "";
		Signal signal = spectrogram.getSignal();
		if (signal != null) {
			int samples = signal.getNumSamples();
			int hertz = signal.getHertz();
			int zoom = spectrogram.getZoom();
			int bins = spectrogram.getBins();
			double overlap = spectrogram.getOverlap();
			int zoomFactor = (int)spectrogram.getSpectrumWidth();
			WindowFunction window = spectrogram.getWindow();
			status = String.format("%s (%s, %s Hz) | %s zoom | " +
					"%s-bin %s%s spectra (%s overlap, %s window)",
					FormatUtils.formatTime(samples / hertz),
					FormatUtils.formatCount(samples, "sample"),
					FormatUtils.formatInt(hertz),
					FormatUtils.formatFactor(zoom, zoomFactor),
					FormatUtils.formatInt(bins),
					logAxis.isSelected() ? "logarithmic" : "linear",
					showPhase.isSelected() ? " phase" : "",
					FormatUtils.formatPercent(1 - overlap),
					window.getName()
			);
		}
		statusBar.setText(status);
	}
	
	public synchronized void updateCursor(long microseconds) {
		if (waveform == null || spectrogram == null)
			return;
		waveform.updateCursor(microseconds);
		spectrogram.updateCursor(microseconds);
	}
	
	/*** Actions ***/
	
	public void actionPerformed(ActionEvent event) {
		if (busy)
			return;
		Object source = event.getSource();
		if (source == open)
			open();
		else if (source == close)
			close();
		else if (source == save)
			save();
		else if (source == convert)
			convert();
		else if (source == exit)
			exit();
		else if (source == zoomIn)
			zoomIn();
		else if (source == zoomOut)
			zoomOut();
		else if (source == zoomMax)
			zoomMax();
		else if (source == zoomFit)
			zoomFit();
		else if (binsItems.containsKey(source))
			setBins(binsItems.get(source));
		else if (overlapItems.containsKey(source))
			setOverlap(overlapItems.get(source));
		else if (windowItems.containsKey(source))
			setWindow(windowItems.get(source));
		else if (source == play)
			play();
		else if (source == stop)
			stop();
		else if (source == garbageCollect)
			garbageCollect();
		else if (source == help)
			help();
		else if (source == about)
			about();
	}
	
	public void itemStateChanged(ItemEvent event) {
		if (busy)
			return;
		Object item = event.getItem();
		if (item == logAxis)
			logAxis();
		else if (item == fullHeight)
			fullHeight();
		else if (item == showPhase)
			showPhase();
	}
	
	private void open() {
		fc.setDialogTitle("Open audio");
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileFilter(audioReadFilter);
		if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		final File file = fc.getSelectedFile();
		final String name = file.getName();
		final boolean useMemory = audioMemory.isSelected();
		SwingWorker<Signal, Void> worker = new SwingWorker<Signal, Void>() {
			public Signal doInBackground() {
				try {
					return Signal.fromFile(SignalWindow.this, file, useMemory);
				}
				catch (InterruptedIOException ex) {}
				catch (Exception ex) {
					JOptionPane.showMessageDialog(SignalWindow.this,
							"Could not open " + name + "!",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				return null;
			}
			
			public void done() {
				Signal signal = null;
				try {
					signal = get();
				}
				catch (InterruptedException ex) {}
				catch (ExecutionException ex) {
					JOptionPane.showMessageDialog(SignalWindow.this,
							"Could not open " + name + "!",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				if (signal != null)
					SignalWindow.this.setSignal(signal);
				SignalWindow.this.setBusy(false);
			}
		};
		setBusy(true);
		worker.execute();
	}
	
	private void save() {
		if (spectrogram.getSignal() == null)
			return;
		fc.setDialogTitle("Save image");
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setFileFilter(imageFilter);
		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		final File file = fc.getSelectedFile();
		final String name = file.getName();
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			public Void doInBackground() {
				try {
					spectrogram.save(SignalWindow.this, file, IMAGE_EXTENSIONS[0]);
					JOptionPane.showMessageDialog(SignalWindow.this,
							"Saved " + name + "!",
							"Success", JOptionPane.PLAIN_MESSAGE);
				}
				catch (IOException ex) {
					JOptionPane.showMessageDialog(SignalWindow.this,
							"Could not save " + name + "!",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				return null;
			}
			
			public void done() {
				try {
					get();
				}
				catch (InterruptedException ex) {}
				catch (ExecutionException ex) {
					JOptionPane.showMessageDialog(SignalWindow.this,
							"Could not save " + name + "!",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				SignalWindow.this.setBusy(false);
			}
		};
		setBusy(true);
		worker.execute();
	}
	
	private void convert() {
		// get image
		fc.setDialogTitle("Open spectrogram image");
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileFilter(imageFilter);
		if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		File imageFile = fc.getSelectedFile();
		final String imageName = imageFile.getName();
		// read image
		final BufferedImage image;
		try {
			image = ImageIO.read(imageFile);
		}
		catch (IOException ex) {
			JOptionPane.showMessageDialog(this,
					"Could not open " + imageName + "!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		// get hertz
		int hertz = 0;
		do {
			Object input = JOptionPane.showInputDialog(this, "Enter sample rate in Hz:",
					"Sample rate", JOptionPane.QUESTION_MESSAGE, null, null, "44100");
			hertz = Integer.parseInt((String)input);
			if (hertz <= 0) {
				JOptionPane.showMessageDialog(this, "Sample rate must be positive!",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} while (hertz <= 0);
		// get overlap
		double overlap = 0.5;
		Double overlapValues[] = new Double[overlapItems.size()];
		int i = 0;
		for (Double value : overlapItems.values()) {
			overlapValues[i++] = value;
		}
		Arrays.sort(overlapValues, Collections.reverseOrder());
		String overlapOptions[] = new String[overlapValues.length];
		for (i = 0; i < overlapValues.length; i++) {
			overlapOptions[i] = FormatUtils.formatPercent(1 - overlapValues[i]);
		}
		Object input = JOptionPane.showInputDialog(this, "Select spectrum overlap:",
				"Spectrum overlap", JOptionPane.QUESTION_MESSAGE, null,
				overlapOptions, overlapOptions[2]);
		for (i = 0; i < overlapOptions.length; i++) {
			if (overlapOptions[i].equals(input)) {
				overlap = overlapValues[i];
				break;
			}
		}
		// save audio
		fc.setDialogTitle("Save audio");
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setFileFilter(audioWriteFilter);
		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		final File audioFile = fc.getSelectedFile();
		final String audioName = audioFile.getName();
		final int hz = hertz;
		final double lap = overlap;
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			public Void doInBackground() {
				try {
					Spectrogram.imageToAudio(SignalWindow.this, image, audioFile,
							hz, lap);
					JOptionPane.showMessageDialog(SignalWindow.this,
							"Converted " + imageName + " to " + audioName + "!",
							"Success", JOptionPane.PLAIN_MESSAGE);
				}
				catch (IOException ex) {
					JOptionPane.showMessageDialog(SignalWindow.this,
							"Could not convert " + imageName + "!",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				return null;
			}
			
			public void done() {
				try {
					get();
				}
				catch (InterruptedException ex) {}
				catch (ExecutionException ex) {
					JOptionPane.showMessageDialog(SignalWindow.this,
							"Could not convert " + imageName + "!",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				SignalWindow.this.setBusy(false);
			}
		};
		setBusy(true);
		worker.execute();
	}
	
	private void close() {
		stop();
		setSignal(null);
	}
	
	private void exit() {
		close();
		setVisible(false);
		dispose();
	}
	
	private void zoomIn() {
		if (waveform.getSignal() == null || spectrogram.getSignal() == null)
			return;
		waveform.zoomIn();
		spectrogram.zoomIn();
		updateStatus();
	}
	
	private void zoomOut() {
		if (waveform.getSignal() == null || spectrogram.getSignal() == null)
			return;
		waveform.zoomOut();
		spectrogram.zoomOut();
		updateStatus();
	}
	
	private void zoomMax() {
		if (waveform.getSignal() == null || spectrogram.getSignal() == null)
			return;
		waveform.zoomTo(1);
		spectrogram.zoomTo(1);
		updateStatus();
	}
	
	private void zoomFit() {
		if (waveform.getSignal() == null || spectrogram.getSignal() == null)
			return;
		int zoom = (int)(spectrogram.getSpectrumWidth());
		waveform.zoomTo(zoom);
		spectrogram.zoomTo(zoom);
		updateStatus();
	}
	
	private void setBins(int bins) {
		spectrogram.setBins(bins);
		updateStatus();
	}
	
	private void setOverlap(double overlap) {
		spectrogram.setOverlap(overlap);
		updateStatus();
	}
	
	private void setWindow(WindowFunction window) {
		spectrogram.setWindow(window);
		updateStatus();
	}
	
	private void logAxis() {
		spectrogram.setLogAxis(logAxis.isSelected());
		updateStatus();
	}
	
	private void fullHeight() {
		spectrogram.setFullHeight(fullHeight.isSelected());
	}
	
	private void showPhase() {
		spectrogram.showPhase(showPhase.isSelected());
		updateStatus();
	}
	
	private synchronized void play() {
		Signal signal = spectrogram.getSignal();
		if (playing || signal == null)
			return;
		playing = true;
		try {
			new Player(this, signal).start();
		}
		catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			playing = false;
		}
	}
	
	public synchronized void stop() {
		playing = false;
	}
	
	private void garbageCollect() {
		System.gc();
	}
	
	private void help() {
		String message = "<html><body>" +
				"Help is not yet available.<br>" +
				"We apologize for the inconvenience." +
				"</body></html>";
		JOptionPane.showMessageDialog(this, message, "Help",
				JOptionPane.PLAIN_MESSAGE);
	}
	
	private void about() {
		String message = "<html><body>" +
				"<b>" + name + "</b><br><br>" +
				"Copyright &copy; 2012 Remy Oukaour<br><br>" +
				"This program uses the JTransforms, MP3SPI,<br>" +
				"VorbisSPI, and jFLAC libraries." +
				"</body></html>";
		JOptionPane.showMessageDialog(this, message, "About",
				JOptionPane.PLAIN_MESSAGE);
	}
}
