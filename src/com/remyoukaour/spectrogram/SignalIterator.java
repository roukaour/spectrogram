package com.remyoukaour.spectrogram;

import java.awt.Component;
import java.io.*;
import java.util.Iterator;
import javax.sound.sampled.*;
import javax.swing.ProgressMonitorInputStream;
import org.kc7bfi.jflac.sound.spi.*;
import org.tritonus.share.sampled.file.TAudioFileFormat;

public class SignalIterator implements Iterator<Double> {
	private static final int BUFFERED_FRAMES = 2048;
	
	private InputStream in;
	private final boolean decoded;
	private final int hertz;
	private final int channels;
	private final int bytesPerSample;
	private final boolean bigEndian;
	private final double maxSample;
	private final byte[] buffer;
	private int remaining;
	
	public SignalIterator(File f) throws UnsupportedAudioFileException, IOException {		
		AudioInputStream ain;
		AudioFileFormat fileFormat;
		AudioFormat format;
		try {
			ain = AudioSystem.getAudioInputStream(f);
			fileFormat = AudioSystem.getAudioFileFormat(f);
			format = ain.getFormat();
		}
		catch (UnsupportedAudioFileException ex) {
			FlacAudioFileReader r = new FlacAudioFileReader();
			ain = r.getAudioInputStream(f);
			fileFormat = r.getAudioFileFormat(ain);
			format = fileFormat.getFormat();
		}
		long length = -1;
		if (fileFormat instanceof TAudioFileFormat) {
			AudioFormat newFormat = new AudioFormat(format.getSampleRate(), 16,
					format.getChannels(), true, false);
			ain = AudioSystem.getAudioInputStream(newFormat, ain);
			format = ain.getFormat();
			long duration = (Long)fileFormat.properties().get("duration");
			length = (long)(duration / 1000000 * format.getSampleRate());
			this.decoded = true;
		}
		else if (fileFormat.getType() == FlacFileFormatType.FLAC) {
			/*
			AudioFormat newFormat = new AudioFormat(format.getSampleRate(), 16,
					format.getChannels(), true, false);
			// Flac2PcmAudioInputStream(ain, newFormat, ?)
			// how to get the number of samples?
			*/
			throw new UnsupportedAudioFileException("FLAC");
		}
		else {
			length = ain.getFrameLength();
			this.decoded = false;
		}
		if (length <= 0)
			throw new IllegalArgumentException("Too short");
		if (length > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Too long");
		AudioFormat.Encoding encoding = format.getEncoding();
		if (encoding != AudioFormat.Encoding.PCM_SIGNED)
			throw new IllegalArgumentException("Unsupported encoding: " + encoding);
		this.hertz = (int)format.getSampleRate();
		this.channels = format.getChannels();
		int bytesPerFrame = format.getFrameSize();
		this.bytesPerSample = bytesPerFrame / channels;
		if (bytesPerSample != 2)
			throw new IllegalArgumentException("16-bit samples required");
		this.bigEndian = format.isBigEndian();
		this.in = new BufferedInputStream(ain, bytesPerFrame * BUFFERED_FRAMES);
		this.maxSample = Math.pow(2, bytesPerSample * 8 - 1);
		this.buffer = new byte[bytesPerFrame];
		this.remaining = (int)length;
	}
	
	public boolean isDecoded() {
		return decoded;
	}
	
	public InputStream getStream() {
		return in;
	}
	
	public InputStream getStream(Component parent, String message) {
		if (!(in instanceof ProgressMonitorInputStream))
			in = new ProgressMonitorInputStream(parent, message, in);
		return in;
	}
	
	public int getHertz() {
		return hertz;
	}
	
	public int getRemaining() {
		return remaining;
	}
	
	public void close() throws IOException {
		in.close();
	}
	
	@Override
	public boolean hasNext() {
		return remaining > 0;
	}

	@Override
	public Double next() {
		try {
			in.read(buffer);
		}
		catch (IOException ex) {
			return null;
		}
		double v = 0.0;
		for (int j = 0; j < channels; j++) {
			short s = ByteUtils.bytesToShort(buffer, j * bytesPerSample, bigEndian);
			v += s / maxSample;
		}
		v /= channels;
		remaining--;
		return v;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
