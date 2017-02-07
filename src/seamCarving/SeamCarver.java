package seamCarving;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.Stack;

import java.util.ArrayList;

public class SeamCarver {

	private static final double BORDER_ENERGY = 1000;
	private Picture picture;
	private int[][] color;
	private double[][] enerArr;
	private int width;
	private int height;

	// create a seam carver object based on the given picture
	public SeamCarver(Picture picture) {
		this.picture = new Picture(picture);
		width = picture.width();
		height = picture.height();
		color = new int[width][height];
		enerArr = new double[width][height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++) {
				color[i][j] = this.picture.get(i, j).getRGB();
			}
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++) {
				enerArr[i][j] = energy(i, j);
			}

	}

	// current picture
	public Picture picture() {
		Picture newPicture = new Picture(width, height);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				newPicture.set(i, j, new Color(color[i][j]));
			}
		}
		picture = newPicture;
		return newPicture;
	}

	// width of current picture
	public int width() {
		return width;
	}

	// height of current picture
	public int height() {
		return height;
	}

	// energy of pixel at column x and row y
	public double energy(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			throw new IndexOutOfBoundsException();
		if (x == 0 || x == width - 1 || y == 0 || y == height - 1)
			return BORDER_ENERGY;

		return Math.hypot(energyX(x, y), energyY(x, y));
	}

	private double energyX(int x, int y) {
		Color leftColor = new Color(color[x - 1][y]);
		Color rightColor = new Color(color[x + 1][y]);
		return dRGB(leftColor.getRed() - rightColor.getRed(), leftColor.getGreen() - rightColor.getGreen(),
				leftColor.getBlue() - rightColor.getBlue());
	}

	private double energyY(int x, int y) {
		Color upColor = new Color(color[x][y - 1]);
		Color downColor = new Color(color[x][y + 1]);
		return dRGB(upColor.getRed() - downColor.getRed(), upColor.getGreen() - downColor.getGreen(),
				upColor.getBlue() - downColor.getBlue());

	}

	private double dRGB(int dRed, int dGreen, int dBlue) {
		return Math.sqrt(dRed * dRed + dGreen * dGreen + dBlue * dBlue);
	}
	// sequence of indices for horizontal seam

	public int[] findHorizontalSeam() {
		Direction direction = new Direction(false);
		return findSeam(direction);
	}

	// sequence of indices for vertical seam
	public int[] findVerticalSeam() {
		Direction direction = new Direction(true);
		return findSeam(direction);
	}

	private void initQueue(List<Integer> queue, Direction direction, Route[][] route, int[] prePoint) {
		if (direction.isVertic()) {
			for (int i = 0; i < width; i++) {
				int ansP = this.getAns(i, 0, direction);
				int e = getPoint(i, 0);
				route[i][0] = new Route(width, height, i, 0, ansP, enerArr[i][0]);
				prePoint[e] = e;
				queue.add(e);
			}
		} else {
			for (int i = 0; i < height; i++) {
				int ansP = this.getAns(0, i, direction);
				int e = getPoint(0, i);
				route[0][i] = new Route(width, height, 0, i, ansP, enerArr[0][i]);
				prePoint[e] = e;
				queue.add(e);
			}
		}
	}

	private int[] findSeam(Direction direction) {
		Route[][] route = new Route[width][height];
		List<Integer> queue = new LinkedList<Integer>();
		Route minRoute = new Route();
		int[] prePoint = new int[(width + 1) * (height + 1) + 1];
		initQueue(queue, direction, route, prePoint);
		while (!queue.isEmpty()) {

			int now = queue.get(0);

			int nowX = getX(now);
			int nowY = getY(now);
			Route nowRoute = route[nowX][nowY];
			if (((nowX == width - 1) && !direction.isVertic()) || ((nowY == height - 1) && direction.isVertic())) {
				if (nowRoute.energy < minRoute.energy) {
					minRoute = nowRoute;
					prePoint[(width + 1) * (height + 1)] = now;
				}
			}
			for (int i = 0; i < 3; i++) {
				int nextX = nowX + direction.x(i);
				int nextY = nowY + direction.y(i);
				if (nextX < 0 || nextX >= width || nextY < 0 || nextY >= height) {
					continue;
				}
				Route nextRoute = route[nextX][nextY];
				int e = getPoint(nextX, nextY);
				int ansP = this.getAns(nextX, nextY, direction);
				if (nextRoute == null) {
					route[nextX][nextY] = new Route(nowRoute, nowRoute.energy + enerArr[nextX][nextY], ansP);
					nextRoute = route[nextX][nextY];
					prePoint[e] = now;
					queue.add(e);
				} else if (nextRoute.energy > nowRoute.energy + energy(nextX, nextY)) {
					route[nextX][nextY].changeNew(nowRoute, nowRoute.energy + enerArr[nextX][nextY]);
					prePoint[e] = now;
				}
			}
			route[nowX][nowY] = null;
			queue.remove(0);

		}
		// int ans[] = new int[minRoute.route.size()];
		// ans = minRoute.route.stream().mapToInt(i -> i).toArray();
		return getRoute(prePoint, direction);
	}

	private int[] getRoute(int[] prePoint, Direction direction) {
		Stack<Integer> stack = new Stack<Integer>();
		int p = (width + 1) * (height + 1);
		while (p != prePoint[p]) {
			p = prePoint[p];
			stack.push(p);
			// System.out.println(p);
		}
		// stack.push(p);
		// System.out.println(stack.size());
		int[] ans = new int[stack.size()];
		int i = 0;
		while (!stack.isEmpty()) {
			if (direction.isVertic()) {
				ans[i++] = getX(stack.pop());
			} else {
				ans[i++] = getY(stack.pop());
			}
		}
		// System.out.println(ans.length);
		return ans;

	}

	private int getX(int p) {
		return p % width;
	}

	private int getY(int p) {
		return p / width;
	}

	private int getPoint(int x, int y) {
		return x + y * width;
	}

	private int getAns(int x, int y, Direction direction) {
		if (direction.isVertic())
			return x;
		return y;
	}

	private void checkValidSeam(int[] seam, boolean isVertic) {

		for (int i = 0; i < seam.length; i++) {
			if ((i != 0) && Math.abs(seam[i] - seam[i - 1]) > 1)
				throw new IllegalArgumentException("distance between pixels is 2");
			if (isVertic) {
				if (seam[i] >= width || seam[i] < 0)
					throw new IllegalArgumentException(" entry is not between ");
				if (seam.length != height)
					throw new IllegalArgumentException();
			} else {
				if (seam[i] >= height || seam[i] < 0)
					throw new IllegalArgumentException(" entry is not between ");
				if (seam.length != width)
					throw new IllegalArgumentException();
			}
		}
	}

	// remove horizontal seam from current picture
	public void removeHorizontalSeam(int[] seam) {
		checkValidSeam(seam, false);
		for (int i = 0; i < seam.length; i++) {
			for (int j = seam[i]; j < height - 1; j++) {
				color[i][j] = color[i][j + 1];

			}
		}

		for (int i = 0; i < seam.length; i++) {
			// int min = seam[i];
			// if ( i!=0 ) min = Math.min(min,seam[i-1]);
			// if ( i!= seam.length-1) min=Math.min(min, seam[i+1]);
			for (int j = 0; j < height - 1; j++) {
				enerArr[i][j] = energy(i, j);

			}
		}
		height--;

	}
	// remove vertical seam from current picture

	public void removeVerticalSeam(int[] seam) {
		checkValidSeam(seam, true);
		for (int j = 0; j < seam.length; j++) {

			for (int i = seam[j]; i < width - 1; i++) {
				color[i][j] = color[i + 1][j];
			}
		}
		for (int j = 0; j < seam.length; j++) {
			// int min = seam[j];
			// if ( j!=0 ) min = Math.min(min,seam[j-1]);
			// if ( j!= seam.length-1) min=Math.min(min, seam[j+1]);
			for (int i = 0; i < width - 1; i++) {
				enerArr[i][j] = energy(i, j);
			}
		}
		width--;
	}

	private class Route {
		private int width;
		private int height;
		// private List<Integer> route;
		public int nowPoint;
		public double energy;
		public int startPoint;
		public int prePoint;

		public Route() {
			energy = Double.MAX_VALUE;
		}

		public Route(Route old, double energy, int ansP) {
			// route = new ArrayList<Integer>(old.route);
			// route = old.route;
			// route.add(ansP);
			this.energy = energy;
			this.prePoint = old.nowPoint;
		}

		public Route(int width, int height, int startX, int startY, int ansP, double energy) {
			this.width = width;
			this.height = height;
			// route = new ArrayList<>();
			// route.add(ansP);
			this.nowPoint = ansP;
			this.energy = energy;
		}

		public void changeNew(Route nowRoute, double energy) {
			// int ansP = route.get(route.size() - 1);
			// route = new ArrayList<Integer>(nowRoute.route);
			// route.add(ansP);
			this.energy = energy;
			this.prePoint = nowRoute.nowPoint;
		}

	}

	private class Direction {
		private int[] x;
		private int[] y;
		private boolean isVertic;

		public Direction(boolean isVertic) {
			this.isVertic = isVertic;
			x = new int[3];
			y = new int[3];
			if (isVertic) {
				x[0] = -1;
				x[1] = 0;
				x[2] = 1;
				y[0] = 1;
				y[1] = 1;
				y[2] = 1;
			} else {
				x[0] = 1;
				x[1] = 1;
				x[2] = 1;
				y[0] = -1;
				y[1] = 0;
				y[2] = 1;
			}
		}

		public int x(int i) {
			return x[i];
		}

		public int y(int i) {
			return y[i];
		}

		public boolean isVertic() {
			return isVertic;
		}
	}

}
