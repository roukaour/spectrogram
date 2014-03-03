package com.remyoukaour.spectrogram;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

public class SampledDiskData extends SampledData {
	private static final int CAPACITY = 24;
	private static final int BLOCK_SIZE = 2048;
	
	private final RandomAccessFile raf;
	private final int size;
	private final Map<Integer, double[]> cache;
	
	public SampledDiskData(RandomAccessFile raf, int size) {
		this.raf = raf;
		this.size = size;
		this.cache = new LinkedHashMap<Integer, double[]>(CAPACITY + 1, 1.1f, true) {
			private static final long serialVersionUID = 865438240L;

			@SuppressWarnings("unused")
			protected boolean removeEldestEntry(Integer eldest) {
				return size() > CAPACITY;
			}
		};
	}
	
	public int size() {
		return size;
	}
	
	public double get(int i) {
		try {
			raf.seek(i);
			return raf.readDouble();
		}
		catch (IOException ex) {
			return 0.0;
		}
	}
	
	public double[] get(int start, int length) {
		double[] data = new double[length];
		if (start < 0) {
			length += start;
			start = 0;
		}
		if (start + length > size) {
			length = size - start;
		}
		int blockIndex = start / BLOCK_SIZE;
		int indexIntoBlock = start % BLOCK_SIZE;
		double[] block = getBlock(blockIndex);
		int lengthInBlock = BLOCK_SIZE - indexIntoBlock;
		if (lengthInBlock < length && size > length) {
			System.arraycopy(block, indexIntoBlock, data, 0, lengthInBlock);
			int restLength = length - lengthInBlock;
			double[] rest = get(start + lengthInBlock, restLength);
			System.arraycopy(rest, 0, data, lengthInBlock, restLength);
		}
		else {
			System.arraycopy(block, indexIntoBlock, data, 0, length);
		}
		return data;
	}
	
	private double[] getBlock(int blockIndex) {
		double[] block = cache.get(blockIndex);
		if (block != null)
			return block;
		block = new double[BLOCK_SIZE];
		try {
			int start = blockIndex * BLOCK_SIZE;
			int n = BLOCK_SIZE;
			if (start + n >= size) {
				n = size - start;
			}
			int width = 8;
			n *= width;
			byte[] bytes = new byte[n];
			raf.seek(start * width);
			raf.readFully(bytes);
			for (int i = 0, j = 0; j < n; i++, j += width) {
				block[i] = ByteUtils.bytesToDouble(bytes, j);
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		cache.put(blockIndex, block);
		return block;
	}
}
