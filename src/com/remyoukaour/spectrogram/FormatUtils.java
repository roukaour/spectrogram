package com.remyoukaour.spectrogram;

import java.text.DecimalFormat;

public class FormatUtils {
	public static String formatInt(int n) {
		return String.format("%,d", n);
	}
	
	public static String formatFactor(int value, int base) {
		int v = value > base ? value / base : base / value;
		String p = value > base ? "1/" : "";
		return String.format("%s%sx", p, formatInt(v));
	}
	
	public static String formatCount(int n, String word) {
		String s = n != 1 ? "s" : "";
		return String.format("%s %s%s", formatInt(n), word, s);
	}
	
	public static String formatPercent(double v) {
		return new DecimalFormat("#.####%").format(v);
	}
	
	public static String formatTime(float seconds) {
		int m = (int)seconds / 60;
		int s = (int)seconds % 60;
		int u = (int)((seconds % 1) * 100);
		return String.format("%d:%02d.%02d", m, s, u);
	}
}
