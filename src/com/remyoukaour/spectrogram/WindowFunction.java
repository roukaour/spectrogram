package com.remyoukaour.spectrogram;

public enum WindowFunction {
	RECTANGULAR("Rectangular (Dirichlet)") {
		public void window(double[] data) {
			// do nothing
		}
	},
	
	TRIANGULAR("Triangular (Bartlett)") {
		public void window(double[] data) {
			int n = data.length;
			double a = (n - 1) / 2.0;
			double b = 2.0 / (n - 1);
			for (int i = 0; i < n; i++) {
				data[i] *= b * (a - Math.abs(i - a));
			}
		}
	},
	
	COSINE("Cosine (sine)") {
		public void window(double[] data) {
			int n = data.length;
			double a = Math.PI / (n - 1);
			for (int i = 0; i < n; i++) {
				data[i] *= Math.sin(a * i);
			}
		}
	},
	
	GAUSSIAN("Gaussian") {
		public void window(double[] data) {
			int n = data.length;
			double a = (n - 1) / 2.0;
			double b = a * 0.4;
			for (int i = 0; i < n; i++) {
				data[i] *= Math.exp(-0.5 * Math.pow((i - a) / b, 2));
			}
		}
	},
	
	LANCZOS("Lanczos (sinc)") {
		public void window(double[] data) {
			int n = data.length;
			double a = TAU / (n - 1);
			for (int i = 1; i < n; i++) {
				double b = i * a;
				data[i] *= Math.sin(b) / b;
			}
		}
	},
	
	WELCH("Welch") {
		public void window(double[] data) {
			int n = data.length;
			double a = n / 2.0;
			for (int i = 0; i < n; i++) {
				data[i] *= 1 - Math.pow((i - a) / a, 2);
			}
		}
	},
	
	HANN("Hann") {
		public void window(double[] data) {
			int n = data.length;
			double a = TAU / (n - 1);
			for (int i = 0; i < n; i++) {
				data[i] *= 0.5 - 0.5 * Math.cos(i * a);
			}
		}
	},
	
	HAMMING("Hamming") {
		public void window(double[] data) {
			// http://cnx.org/content/m0505/latest/
			int n = data.length;
			double a = TAU / (n - 1);
			for (int i = 0; i < n; i++) {
				data[i] *= 0.54 - 0.46 * Math.cos(i * a);
			}
		}
	},
	
	BARTLETT_HANN("Bartlett-Hann") {
		public void window(double[] data) {
			int n = data.length;
			double a = n - 1.0;
			double b = TAU / (n - 1);
			for (int i = 0; i < n; i++) {
				data[i] *= 0.62 - 0.48 * Math.abs(i / a - 0.5) -
						0.38 * Math.cos(i * b);
			}
		}
	},
	
	BLACKMAN("Blackman") {
		public void window(double[] data) {
			int n = data.length;
			double a = TAU / (n - 1);
			double b = 2 * a;
			for (int i = 0; i < n; i++) {
				data[i] *= 0.42 - 0.5 * Math.cos(i * a) + 0.08 * Math.cos(i * b);
			}
		}
	},
	
	NUTTALL("Nuttall") {
		public void window(double[] data) {
			int n = data.length;
			double a = TAU / (n - 1);
			double b = 2 * a;
			double c = 3 * a;
			for (int i = 0; i < n; i++) {
				data[i] *= 0.355768 - 0.487396 * Math.cos(i * a) +
						0.144232 * Math.cos(i * b) - 0.012604 * Math.cos(i * c);
			}
		}
	},
	
	BLACKMAN_HARRIS("Blackman-Harris") {
		public void window(double[] data) {
			int n = data.length;
			double a = TAU / (n - 1);
			double b = 2 * a;
			double c = 3 * a;
			for (int i = 0; i < n; i++) {
				data[i] *= 0.35875 - 0.48829 * Math.cos(i * a) +
						0.14128 * Math.cos(i * b) - 0.01168 * Math.cos(i * c);
			}
		}
	},
	
	BLACKMAN_NUTTALL("Blackman-Nuttall") {
		public void window(double[] data) {
			int n = data.length;
			double a = TAU / (n - 1);
			double b = 2 * a;
			double c = 3 * a;
			for (int i = 0; i < n; i++) {
				data[i] *= 0.3635819 - 0.4891775 * Math.cos(i * a) +
						0.1365995 * Math.cos(i * b) - 0.0106411 * Math.cos(i * c);
			}
		}
	},
	
	FLAT_TOP("Flat top") {
		public void window(double[] data) {
			int n = data.length;
			double a = TAU / (n - 1);
			double b = 2 * a;
			double c = 3 * a;
			double d = 4 * a;
			for (int i = 0; i < n; i++) {
				data[i] *= 0.21557895 - 0.41663158 * Math.cos(i * a) +
						0.277263158 * Math.cos(i * b) -
						0.083578947 * Math.cos(i * c) +
						0.006947368 * Math.cos(i * d);
			}
		}
	};
	
	private static final double TAU = Math.PI * 2;
	
	private final String name;
	
	private WindowFunction(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract void window(double[] data);
}
