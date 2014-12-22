import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

public class Experiment3 extends JComponent implements Runnable{
	// https://docs.oracle.com/javase/7/docs/api/allclasses-noframe.html
	private static final long serialVersionUID = -7077789957096265275L;
	public static Experiment3 exp;
	Map map;
	Perceptron p;
	int currentx, currenty;
	static Thread thread;
	boolean started = false;
	public boolean finished = false;
	public final int cellSize = 16;
	boolean printPerceptronMode = false;
	boolean debugOnce = false;
	double[][][] dirMap;
	public Experiment3() {
		map = new Map(getClass().getResource("/map").getPath().substring(1));
		p = new Perceptron(map.powerX+map.powerY, new int[]{16, 8});
		dirMap = new double[map.h][map.w][8];
		int iterationSpeed = 2;
		int teachingIterations = 0;
		//int debugCounter = 0;
		long startTime = System.currentTimeMillis();
		double[][][] perceptronStateBest = null;
		double errorBest = 0;
		int y1 = map.endy;
		int x1 = map.endx;
//		for (int y1 = 0; y1 < map.h; y1 += multiplier) {
//			for (int x1 = 0; x1 < map.w; x1 += multiplier) {
//				if (!map.occupied[y1][x1]){
					map.fillDirections(x1, y1, dirMap);
					boolean limitReached = true;
					while (limitReached) {
						teachingIterations++;
						for (int y2 = 0; y2 < map.h; y2 += iterationSpeed) {
							for (int x2 = 0; x2 < map.w; x2 += iterationSpeed) {
								if ((x1 != x2 || y1 != y2) && !map.occupied[y2][x2]) {
									p.train(encodePoints(x2, y2),
											dirMap[y2][x2]);
								}
							}
						}
						limitReached = false;
						double quadraticError = 0;
						for (int y2 = 0; y2 < map.h; y2 += iterationSpeed) {
							for (int x2 = 0; x2 < map.w; x2 += iterationSpeed) {
								if ((x1 != x2 || y1 != y2) && !map.occupied[y2][x2]) {
									double[] r = p.result(encodePoints(x2, y2));
									double[] cD = dirMap[y2][x2];
									double subSum = 0;
									for (int z = 0; z < r.length; z++) {
										subSum += (r[z] - cD[z]) * (r[z] - cD[z]);
									}
									subSum = Math.sqrt(subSum);
									quadraticError += subSum;
								}
							}
						}
						int quadraticCount = ((int)map.w/iterationSpeed)/((int)map.h/iterationSpeed);
						if (quadraticError/quadraticCount > 0.1) limitReached = true;
						if (perceptronStateBest == null || quadraticError/quadraticCount < errorBest) {
							perceptronStateBest = p.saveState();
							errorBest = quadraticError/quadraticCount;
						}
						if(System.currentTimeMillis() - startTime > 10000) break;
					}
//				}
//			}
//		}
		//p.loadState(perceptronStateBest);
		System.out.printf("teaching iterations: %d%nerror: %f%n", teachingIterations, errorBest);
		System.out.println(p);
		currentx = map.startx;
		currenty = map.starty;
		setPreferredSize(new Dimension(cellSize*map.w, cellSize*map.h));
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (started) {
					if (!finished) {
						exp.printPerceptron();
						finished = true;
					}
				} else started = true;
			}
		});
		map.fillDirections(map.endx,map.endy,dirMap);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int i, j;
		for (i = 0; i < map.h; i++)
			for (j = 0; j < map.w; j++) {
				if (currentx == j && currenty == i) g.setColor(Color.BLUE);
				else if (map.endx == j && map.endy == i) g.setColor(Color.RED);
				else if (map.occupied[i][j]) g.setColor(Color.GRAY);
				else g.setColor(Color.WHITE);
				g.fillRect(j*cellSize, i*cellSize, cellSize, cellSize);
			}
		g.setColor(Color.BLACK);
		for (i = 0; i < map.h; i++) {
			for (j = 0; j < map.w; j++) {
				g.drawRect(j*cellSize, i*cellSize, cellSize, cellSize);
				if (printPerceptronMode) {
					double[] r = p.result(encodePoints(j,i));
					//r = dirMap[i][j];
					if (!debugOnce) {
						debugOnce = true;
						double[] en = encodePoints(j,i);
						System.out.print("encodingResult=");
						for (int z = 0; z < en.length; z++) {
							System.out.printf("%fs,", en[z]);
						}
						System.out.println();
						System.out.println("powerX="+map.powerX+", powerY="+map.powerY);
					}
					int dx = dirToX(r);
					int dy = dirToY(r);
					g.drawString((dx == -1 ? "-" : dx == 0 ? "0" : "+") + 
								 (dy == -1 ? "-" : dy == 0 ? "0" : "+"),
								 j*cellSize, i*cellSize + cellSize);
				}
			}
		}
		printPerceptronMode = false;
	}
	@Override
	public void run() {
		if (started) {
			double[] r = p.result(encodePoints(currentx, currenty));
			int dx = dirToX(r);
			int dy = dirToY(r);
//			int dx = (int)dirMap[currenty][currentx].xDir;
//			int dy = (int)dirMap[currenty][currentx].yDir;
			int newx = currentx + dx;
			int newy = currenty + dy;
			int boundaryOffset = 1;
			if (newx < boundaryOffset || newx >= map.w - boundaryOffset) {
				newx = currentx;
			}
			if (newy < boundaryOffset || newy >= map.h - boundaryOffset || map.occupied[newy][newx]) {
				newy = currenty;
			}
			if (map.occupied[newy][newx]) {
				newx = currentx;
			}
			if (map.occupied[newy][newx]) {
				newy = currenty;
			}
			if (currentx == newx && currenty == newy) finished = true;
			currentx = newx;
			currenty = newy;
			System.out.printf("(%d,%d)\n", currentx, currenty);
			if (Math.abs(currentx - map.endx) <= 1 && Math.abs(currenty - map.endy) <= 1)
				finished = true;
		}
		repaint();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!finished) run();
		else {
			printPerceptron();
		}
	}
	public static void main(String[] args) {
		JFrame frame = new JFrame("Neuron Experiment");
		exp = new Experiment3();
		frame.add(exp);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				if (!exp.finished) exp.printPerceptron();
				System.exit(0);
			}
		});
		thread = new Thread(exp);
		thread.start();
	}
	public static double[] joinArrays(double[]... args) {
		int lengthSum = 0;
		for (double[] a : args) lengthSum += a.length;
		double[] r = new double[lengthSum];
		int counter = 0;
		for (double[] a : args) for (double d : a) {
			r[counter++] = d;
		}
		return r;
	}
	public double[] encodePoints(int x1, int y1) {
		return joinArrays(Map.intToDoubleArray(x1, map.powerX),
				   Map.intToDoubleArray(y1, map.powerY));
	}
	void printPerceptron() {
		printPerceptronMode = true;
		repaint();
	}
//	public double dirToImpulse(double dir) {
//		//OBSOLETE
//		//was used when map filled dirMap with xDir, yDir values ranging from -1 to 1
//		//neuron impulse would be converted to dir between lowLimit and highLimit
//		return (dir + 1)/3 * (highLimit - lowLimit) + lowLimit;
//	}
//	public int impulseToDir(double impulse) {
//		return (int)Math.round(Math.max(-1, Math.min(1, (impulse - lowLimit)/(highLimit - lowLimit)*3 - 1)));
//	}
	double[] booleanToDouble(boolean[] ar) {
		double[] r = new double[ar.length];
		for (int i = 0; i < ar.length; i++) {
			r[i] = ar[i] ? 1 : 0;
		}
		return r;
	}
	boolean[] doubleToBoolean(double[] ar) {
		boolean[] r = new boolean[ar.length];
		for (int i = 0; i < ar.length; i++) {
			r[i] = ar[i]>0.5;
		}
		return r;
	}
	int dirToX(double[] ar) {
		int max = 0;
		for (int z = 1; z < ar.length; z++) {
			if (ar[z] > ar[max]) {
				max = z;
			}
		}
		if (max == 7 || max == 0 || max == 1) return 1;
		if (max == 3 || max == 4 || max == 5) return -1;
		return 0;
	}
	int dirToY(double[] ar) {
		int max = 0;
		for (int z = 1; z < ar.length; z++) {
			if (ar[z] > ar[max]) {
				max = z;
			}
		}
		if (max == 1 || max == 2 || max == 3) return 1;
		if (max == 5 || max == 6 || max == 7) return -1;
		return 0;
	}
}
