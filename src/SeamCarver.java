import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.princeton.cs.algs4.Picture;

public class SeamCarver {

	public static final double BORDER_ENERGY = 1000.0;
	private Picture picture;
	private double[][] energyArray;

	// create a seam carver object based on the given picture
	public SeamCarver(Picture picture) {
		this.picture = new Picture(picture);
		this.energyArray = new double[this.picture.width()][this.picture.height()];
		for (int i = 0; i < picture.width(); i++)
			for (int j = 0; j < picture.height(); j++)
				this.energyArray[i][j] = energy(i, j);
	}

	// current picture
	public Picture picture() {
		return this.picture;
	}

	// width of current picture
	public int width() {
		return this.picture.width();
	}

	// height of current picture
	public int height() {
		return this.picture.height();
	}

	// energy of pixel at column x and row y
	public double energy(int x, int y) {
		if (x < 0 || x >= this.picture.width() || y < 0 || y >= this.picture.height())
			throw new IndexOutOfBoundsException();
		if (x == 0 || x == this.picture.width() - 1 || y == 0 || y == this.picture.height() - 1)
			return BORDER_ENERGY;
		return Math.hypot(energyX(x, y), energyY(x, y));
	}

	private double energyX(int x, int y) {
		Color leftColor = picture.get(x - 1, y);
		Color rightColor = picture.get(x + 1, y);
		return dRGB(leftColor.getRed() - rightColor.getRed(), leftColor.getGreen() - rightColor.getGreen(),
				leftColor.getBlue() - rightColor.getBlue());
	}

	private double energyY(int x, int y) {
		Color upColor = picture.get(x, y - 1);
		Color downColor = picture.get(x, y + 1);
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

	public void initQueue(List<Integer> queue, Direction direction, Route[][] route) {
		if (direction.isVertic()) {
			for (int i = 0; i < this.picture.width(); i++) {
				int ansP = this.getAns(i, 0, direction);
				int e = getPoint(i, 0);
				route[i][0] = new Route(this.picture.width(), this.picture.height(), i, 0, ansP, energyArray[i][0]);
				queue.add(e);
			}
		} else {
			for (int i = 0; i < this.picture.height(); i++) {
				int ansP = this.getAns(0, i, direction);
				int e = getPoint(0, i);
				route[0][i] = new Route(this.picture.width(), this.picture.height(), 0, i, ansP, energyArray[0][i]);
				queue.add(e);
			}
		}
	}

	private int[] findSeam(Direction direction) {
		Route[][] route = new Route[this.picture.width()][this.picture.height()];
		List<Integer> queue = new ArrayList<Integer>();
		Route minRoute = new Route();
		initQueue(queue, direction, route);
		while (!queue.isEmpty()) {
			int now = queue.get(0);
			int nowX = this.getX(now);
			int nowY = this.getY(now);
			Route nowRoute = route[nowX][nowY];
			if (((nowX == this.picture.width() - 1) && !direction.isVertic())
					|| ((nowY == this.picture.height() - 1) && direction.isVertic())) {
				if (nowRoute.energy < minRoute.energy) {
					minRoute = nowRoute;
				}
			}
			for (int i = 0; i < 3; i++) {
				int nextX = nowX + direction.x(i);
				int nextY = nowY + direction.y(i);
				if (nextX < 0 || nextX >= this.picture.width() || nextY < 0 || nextY >= this.picture.height()) {
					continue;
				}
				Route nextRoute = route[nextX][nextY];
				int e = getPoint(nextX, nextY);
				int ansP = this.getAns(nextX, nextY, direction);
				if (nextRoute == null) {
					route[nextX][nextY] = new Route(nowRoute, nowRoute.energy + energyArray[nextX][nextY], ansP);
					nextRoute = route[nextX][nextY];
					queue.add(e);
				} else if (nextRoute.energy > nowRoute.energy + energyArray[nextX][nextY]) {
					route[nextX][nextY].changeNew(nowRoute, nowRoute.energy + energyArray[nextX][nextY]);
				}
			}
			queue.remove(0);

		}
		int ans[] = new int[minRoute.route.size()];
		ans = minRoute.route.stream().mapToInt(i -> i).toArray();
		return ans;
	}

	public int getX(int p) {
		return p % this.picture.width();
	}

	public int getY(int p) {
		return p / this.picture.width();
	}

	public int getPoint(int x, int y) {
		return x + y * this.picture.width();
	}

	public int getAns(int x, int y, Direction direction) {
		if (direction.isVertic())
			return x;
		return y;
	}

	// remove horizontal seam from current picture
	public void removeHorizontalSeam(int[] seam) {
		

	}
	// remove vertical seam from current picture

	public void removeVerticalSeam(int[] seam) {
	}

	private class Route {
		private int width;
		private int height;
		private List<Integer> route;
		// public int nowP;
		public double energy;

		public Route() {
			this.energy = Double.MAX_VALUE;
		}

		public Route(Route old, double energy, int ansP) {
			this.route = new ArrayList<Integer>(old.route);
			this.route.add(ansP);
			this.energy = energy;
		}

		public Route(int width, int height, int startX, int startY, int ansP, double energy) {
			this.width = width;
			this.height = height;
			this.route = new ArrayList<>();
			this.route.add(ansP);
			// this.nowP = startY * width + startX;
			this.energy = energy;
		}

		public void changeNew(Route nowRoute, double energy) {
			int ansP = this.route.get(this.route.size() - 1);
			this.route = new ArrayList<Integer>(nowRoute.route);
			this.route.add(ansP);
			this.energy = energy;
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
				this.x[0] = -1;
				this.x[1] = 0;
				this.x[2] = 1;
				this.y[0] = 1;
				this.y[1] = 1;
				this.y[2] = 1;
			} else {
				this.x[0] = 1;
				this.x[1] = 1;
				this.x[2] = 1;
				this.y[0] = -1;
				this.y[1] = 0;
				this.y[2] = 1;
			}
		}

		public int x(int i) {
			return this.x[i];
		}

		public int y(int i) {
			return this.y[i];
		}

		public boolean isVertic() {
			return this.isVertic;
		}
	}

}
