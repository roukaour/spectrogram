package com.remyoukaour.spectrogram;

import java.io.*;

public class BufferedRandomAccessFile extends RandomAccessFile {
	private static final int DEFAULT_BUFFER = 2048;
	
	private final int size;
	private final byte[] buffer;
	private int at;
	
	public BufferedRandomAccessFile(String name, String mode)
			throws FileNotFoundException {
		this(new File(name), mode, DEFAULT_BUFFER);
	}
	
	public BufferedRandomAccessFile(String name, String mode, int size)
			throws FileNotFoundException {
		this(new File(name), mode, size);
	}
	
	public BufferedRandomAccessFile(File file, String mode)
			throws FileNotFoundException {
		this(file, mode, DEFAULT_BUFFER);
	}
	
	public BufferedRandomAccessFile(File file, String mode, int size)
			throws FileNotFoundException {
		super(file, mode);
		this.size = size;
		this.buffer = new byte[size];
		this.at = 0;
	}
	
	public void writeBuffered(byte b) throws IOException {
		buffer[at++] = b;
		if (at >= size) {
			flushBuffer();
		}
	}
	
	public void writeBuffered(byte[] b) throws IOException {
		writeBuffered(b, 0, b.length);
	}
	
	public void writeBuffered(byte[] b, int off, int len) throws IOException {
		int s = off + len;
		for (int i = off; i < s; i++) {
			writeBuffered(b[i]);
		}
	}
	
	public void flushBuffer() throws IOException {
		write(buffer, 0, at);
		at = 0;
	}
}
