import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Map {
	public boolean[][] occupied;
	public int startx, starty, endx, endy;
	public int w, h, powerX, powerY;
	public int[][] cost;
	static int encodingBase = 3;
	public Map(String path) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
			occupied = new boolean[lines.size()][lines.get(0).length()];
			h = lines.size();
			w = lines.get(0).length();
			powerX = (int)Math.ceil(ln(encodingBase, w));
			powerY = (int)Math.ceil(ln(encodingBase, h));
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {
					char c = lines.get(i).charAt(j);
					occupied[i][j] = c == '1';
					if (c == 'x') {
						startx = j;
						starty = i;
					}
					if (c == 'y') {
						endx = j;
						endy = i;
					}
				}
			}
			cost = new int[h][w];
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	/**
	 * Fills map with values ([-1,+1],[-1,+1]) describing direction in which to go
	 * in order to reach the target point (x0, y0). The map covers all space
	 * @param x0, y0 - coordinates of the starting point, to which to go from everywhere.
	 * @param dirMap - array of size [h][w] filled with boolean[8] arrays.
	 */
	public void fillDirections(int x0, int y0, double[/*h*/][/*w*/][/*8*/] dirMap) {
		List<Point> queue = Arrays.asList(new Point(x0, y0));
		List<Point> newQueue;
		for (int i = 0; i < h; i++) 
			for (int j = 0; j < w; j++) {
				cost[i][j] = Integer.MAX_VALUE;
				for (int z = 0; z < 8; z++)
					dirMap[i][j][z] = 0;
			}
		cost[y0][x0] = 0;
		while (queue.size() != 0) {
			newQueue = new ArrayList<Point>();
			for (Point p : queue) {
				fillCell(p.x - 1, p.y - 1, p.x, p.y, newQueue, dirMap);
				fillCell(p.x + 0, p.y - 1, p.x, p.y, newQueue, dirMap);
				fillCell(p.x + 1, p.y - 1, p.x, p.y, newQueue, dirMap);
				fillCell(p.x - 1, p.y + 0, p.x, p.y, newQueue, dirMap);
			  //fillCell(p.x + 0, p.y + 0, p.x, p.y, newQueue, dirMap);
				fillCell(p.x + 1, p.y + 0, p.x, p.y, newQueue, dirMap);
				fillCell(p.x - 1, p.y + 1, p.x, p.y, newQueue, dirMap);
				fillCell(p.x + 0, p.y + 1, p.x, p.y, newQueue, dirMap);
				fillCell(p.x + 1, p.y + 1, p.x, p.y, newQueue, dirMap);
			}
			queue = newQueue;
		}
	}
	private void fillCell(int x, int y, int sx, int sy, List<Point> queueList, double[][][] result) {
		if (x < 0 || x >= w || y < 0 || y >= h || cost[sy][sx] + 1 > cost[y][x] || occupied[y][x]) return;
		if (cost[sy][sx] + 1 <= cost[y][x]) {
			double val = 0.5;
			if (cost[sy][sx] + 1 < cost[y][x]) {
				val = 1;
				cost[y][x] = cost[sy][sx] + 1;
				queueList.add(new Point(x,y));
			}
			if (x - sx == +1 && y - sy == +0) result[y][x][4] = val; else
			if (x - sx == +1 && y - sy == +1) result[y][x][5] = val; else
			if (x - sx == +0 && y - sy == +1) result[y][x][6] = val; else
			if (x - sx == -1 && y - sy == +1) result[y][x][7] = val; else
			if (x - sx == -1 && y - sy == +0) result[y][x][0] = val; else
			if (x - sx == -1 && y - sy == -1) result[y][x][1] = val; else
			if (x - sx == +0 && y - sy == -1) result[y][x][2] = val; else
			if (x - sx == +1 && y - sy == -1) result[y][x][3] = val;
		}
	}
	public static class Point {
		public int x, y;
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	public static double[] intToDoubleArray(int a, int length) {
		int l = (int)Math.ceil(ln(encodingBase,a));
		length = Math.max(length, l);
		double[] r = new double[length];
		int i = r.length - 1;
		while (a != 0) {
			r[i--] = a % encodingBase;
			a = a/encodingBase;
		}
		for (i = r.length; i < length; i++) r[i] = 0;
		return r;
	}
	public static double[] intToDoubleArray(int a) {
		return intToDoubleArray(a, 0);
	}
	public static double ln(double a, double b) {
		return Math.log(b)/Math.log(a);
	}
}
