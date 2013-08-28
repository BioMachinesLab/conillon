package web;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Date;
import java.util.Random;

import javax.swing.JPanel;

public class ScreenSaver extends JPanel {

	private static final double MAX_DIM = 100;
	private Random r = new Random();
	private Point p2 = new Point(100, 100);
	private Point p1 = new Point(10, 20);
	private Point dir1 = new Point(70, 20);
	private Point dir2 = new Point(5, 10);
	private int value = 0;
	private int time = 0;

	private OperatingSystemMXBean os = ManagementFactory
			.getOperatingSystemMXBean();

	public ScreenSaver() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ScreenSaver.this.repaint();
				}
			}
		}).start();
	}

	public void paint(Graphics g) {
		int width = getWidth();
		int height = getHeight();

		// if (time == 0) {
		g.setColor(Color.black);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.white);
		g.drawString("Conillon Screen Saver  -  " //s+
													// (int)(os.getSystemLoadAverage()*100)
													// + "% - "
				+ new Date(System.currentTimeMillis()), r.nextInt(width),
				r.nextInt(height));
		// }

		// // Erase old line:
		// g.setColor(getBackground());
		// g.drawLine(p1.x, p1.y, p2.x, p2.y);

		// // Move points and bounce off walls:
		// bounce(p1, dir1, width, height);
		// bounce(p2, dir2, width, height);
		//
		// Color lineColor = new Color(value, value, value);
		// value = (value + 1) % 256;
		//
		// // Draw new line:
		// g.setColor(lineColor);
		// g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	private void bounce(Point p, Point d, int width, int height) {
		if (p.x > width || p.x < 0) {
			d.x = -d.x;
		}
		if (p.y > height || p.y < 0) {
			d.y = -d.y;
		}
		p.x += d.x;
		p.y += d.y;

	}

	// @Override
	// public void paint(Graphics g) {
	// // // TODO Auto-generated method stub
	// // super.paint(g);
	// // setBackground(Color.black);
	// // setOpaque(true);
	// // g.setColor(new Color((int)(Math.random()*256),
	// (int)(Math.random()*256), (int)(Math.random()*256)));
	// // g.fillOval((int)(Math.random()*getWidth()),
	// (int)(Math.random()*getHeight()), (int)(Math.random()*MAX_DIM),
	// (int)(Math.random()*MAX_DIM));
	//
	//
	// // Clear the background to black.
	// g.setColor (Color.BLACK);
	// g.fillRect (0, 0, getWidth(), getHeight());
	//
	// // Setup the transformation values of the object.
	// double scale = random (60, 400);
	// double radius = scale / 2.0;
	// double centerX = random (radius, getWidth() - radius);
	// double centerY = random (radius, getHeight() - radius);
	//
	// // Choose a random color for the object.
	// float red = (float)Math.random();
	// float green = (float)Math.random();
	// float blue = (float)Math.random();
	// g.setColor (new Color (red, green, blue));
	//
	// // Draw an object.
	// if (Math.random() < 0.5) {
	// drawButterfly (g, scale, centerX, centerY);
	// } else {
	// drawRose (g, scale, centerX, centerY);
	// }
	//
	//
	//
	// }

	// Return a random number within the specified inclusive range.
	public static double random(double minimum, double maximum) {
		final double RANGE = maximum - minimum;
		return minimum + Math.random() * RANGE;
	}

	// Draw Temple H. Fay's butterfly curve.
	public static void drawButterfly(Graphics g, double scale, double centerX,
			double centerY) {
		// Draw many of the points along the curve.
		for (double t = 0; t < 2 * Math.PI; t = t + 0.001) {
			// Exchange the t value for x and y points on the butterfly curve.
			double cache = (Math.exp(Math.cos(t)) - 2 * Math.cos(4 * t) - Math
					.pow(Math.sin(t / 12), 5));
			double x = Math.sin(t) * cache;
			double y = Math.cos(t) * cache;

			// At this point, the butterfly is upside down. Flip it about the
			// x-axis.
			y = y * -1;

			// Normalize the butterfly's size to a 1x1 rectangle.
			// This simplifies the code needed to randomly place the object on
			// the
			// screen, such that it doesn't go over an edge.
			x = x / 6;
			y = y / 6;

			drawPoint(g, x, y, scale, centerX, centerY);

		}
	}

	// Draw a rose.
	public static void drawRose(Graphics g, double scale, double centerX,
			double centerY) {
		// Draw many of the points along the curve.
		for (double t = 0; t < 2 * Math.PI; t = t + 0.001) {
			// Exchange the t value for x and y points on the rose.
			double cache = Math.sin(2 * t);
			double x = Math.sin(t) * cache;
			double y = Math.cos(t) * cache;

			// Normalize the rose's size to a 1x1 rectangle.
			// This simplifies the code needed to randomly place the object on
			// the
			// screen, such that it doesn't go over an edge.
			x = x / 2;
			y = y / 2;

			drawPoint(g, x, y, scale, centerX, centerY);
		}
	}

	// Draw a point of a scaled and translated object.
	public static void drawPoint(Graphics g, double x, double y, double scale,
			double centerX, double centerY) {
		// Scale the point.
		x = x * scale;
		y = y * scale;

		// Translate the point.
		x = x + centerX;
		y = y + centerY;

		// Draw the point as a circle to give the eventual line some thickness.
		final int DIAMETER = 4;
		g.fillOval((int) x, (int) y, DIAMETER, DIAMETER);
	}

}
