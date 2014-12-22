import java.util.Arrays;

public class Perceptron {
	public Neuron[][] n;//[layer][neuron in layer]
	int xLength, yLength;//xLength - number of inputs. yLength - number of outputs
	int max;//maximum number of neurons in a layer
	public Perceptron(int inputs, int[] neuronsCount){
		xLength = inputs;
		if (inputs < 1 || neuronsCount.length == 0) throw new IllegalArgumentException("Creating empty or trivial perceptron.");
		n = new Neuron[neuronsCount.length][];
		max = inputs;
		for (int i = 0; i < neuronsCount.length; i++) {
			n[i] = new Neuron[neuronsCount[i]];
			for (int j = 0; j < n[i].length; j++) {
				int l = i == 0 ? inputs : neuronsCount[i - 1];
				n[i][j] = new Neuron(l, 0.005);
			}
			max = Math.max(max, neuronsCount[i]);
		}
		yLength = neuronsCount[neuronsCount.length - 1];
	}
	public double[] result(double[] x) {
		double[] inputs;
		double[] outputs = x;
		for (int i = 0; i < n.length; i++) {
			Neuron[] p = n[i];
			inputs = outputs;
			outputs = new double[max];
			for (int j = 0; j < p.length; j++) {
				outputs[j] = p[j].result(inputs);
			}
		}
		return Arrays.copyOf(outputs, yLength);
	}
	
	/**
	 * Trains the perceptron on an individual example.
	 * @param x - input values.
	 * @param y - expected correct output values.
	 * @param low, high - specify these parameters if you don't care if the output will be outside some limits.
	 */
	public void train(double[] x, double[] y, double low, double high) {
		low += 0.01;
		high -= 0.01;
		double[] r = result(x);
		double[] newErrors = new double[max];
		for (int i = 0; i < r.length; i++) {
			if (r[i] < low && y[i] < low || r[i] > high && y[i] > high) newErrors[i] = 0;
			else {
				newErrors[i] = (y[i] - r[i]) * r[i] * (1 - r[i]);
				n[n.length - 1][i].correct(newErrors[i]);
			}
		}
		for (int i = n.length - 2; i >= 0; i--) {
			double[] errors = newErrors;
			newErrors = new double[max];
			for (int j = 0; j < n[i].length; j++) {
				double errorSum = 0;
				for (int z = 0; z < n[i + 1].length; z++) {
					errorSum += errors[z] * n[i + 1][z].w[j];
				}
				newErrors[j] = errorSum * n[i][j].lastResult * (1 - n[i][j].lastResult); 
				n[i][j].correct(newErrors[j]);
			}
		}
	}
	public void train(double[] x, double[] y) {
		train(x, y, -1, 2);
	}
	public String toString() {
		String s = "Neuron Network {\n";
		for (int i = 0; i < n.length; i++) {
			s += "\tLevel " + i + ":\n";
			for (int j = 0; j < n[i].length; j++) {
				s += "\t\t" + n[i][j] + "\n";
			}
		}
		return s + "}";
	}
	public double[][][] saveState() {
		double[][][] pst = new double[n.length][][];
		for (int i = 0; i < n.length; i++) {
			pst[i] = new double[n[i].length][];
			for (int j = 0; j < n[i].length; j++) {
				pst[i][j] = Arrays.copyOf(n[i][j].w, n[i][j].w.length);
			}
		}
		return pst;
	}
	public void loadState(double[][][] pst) {
		for (int i = 0; i < n.length; i++) {
			for (int j = 0; j < n[i].length; j++) {
				n[i][j].w = Arrays.copyOf(pst[i][j], pst[i][j].length);
			}
		}
	}
}
