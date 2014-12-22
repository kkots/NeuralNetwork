public class Neuron {
	public double[] w;//weights
	double[] x;//last given input
	public double lastResult;
	double w0;
	double speed;
	public static boolean sameSignCheck = false;
	public static int prevSign = 0;
	public static boolean notNullErrorCheck = false;
	public static boolean functionRan = false;
	public Neuron(int length, double speed) {
		if (length < 1 || speed < 0) throw new IllegalArgumentException("Creating a neuron with invalid arguments.");
		w = new double[length];
		for (int i = 0; i < length; i++) {
			w[i] = setRandomWeight();
		}
		w0 = -Math.abs(setRandomWeight());
		this.speed = speed;
	}
	public double result(double[] x) {
		this.x = x;
		double sum = w0;
		for (int i = 0; i < w.length; i++) {
			sum += x[i]*w[i];
		}
		lastResult = 1/(1+Math.pow(Math.E, -sum));
		return lastResult;
	}
	public void correct(double[] x, double error) {
		this.x = x;
		correct(error);
	}
	public void correct(double error) {
		if (x == null) throw new NullPointerException("Must call result before calling correct or specify input x.");

		if (!functionRan) {
			functionRan = true;
			System.out.println("Function ran check passed.");
		}
		double oldw;
		int newSign;
		for (int i = 0; i < w.length; i++) {
			oldw = w[i];
			w[i] += error*speed*x[i];
			newSign = oldw < w[i] ? 1 : -1;
			if (!notNullErrorCheck) {
				notNullErrorCheck = true;
				System.out.printf("error = %f, speed = %f, x[i] = %f%n", error, speed, x[i]);
			}
			if (oldw != w[i]) {
				if (!notNullErrorCheck) {
					notNullErrorCheck = true;
					System.out.println("Not null error check passed.");
				}
			}
			if (prevSign == 0) prevSign = newSign;
			else if (prevSign != newSign) {
				if (!sameSignCheck) {
					sameSignCheck = true;
					System.out.println("Same sign check failed.");
				}
			}
		}
		w0 -= error*speed;
	}
	public void setSpeed(double s) {
		if (s < 0) throw new IllegalArgumentException("Invalid speed argument for neuron.");
		speed = s;
	}
	private static double setRandomWeight() {
		return Math.random();
	}
	public String toString() {
		String s = "{"+w0+",";
		for (int i = 0; i < w.length; i++) {
			s += w[i] + ",";
		}
		return s.substring(0, s.length() - 1) + "}";
	}
}
