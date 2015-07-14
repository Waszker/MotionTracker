package tracking;

import image.Image;
import imageutils.ImageEffect;
import imageutils.ImageEffect.ShapeRectangle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Tracker
{
	/***************/
	/* VARIABLES */
	/***************/
	private static final int NUMBER_OF_PHOTOS = 571;
	private static final int BACKGROUND = 0;
	private static final int SHAPE = 255;
	private static final int TRESHOLD = 5;
	private static final int SMALL_BACTERIA = 20;

	private static List<List<ShapeRectangle>> rectanglesInSequence;
	private static List<Image> imagesInSequence;
	private static Color[] colors = { Color.BLACK, Color.BLUE, Color.CYAN,
			Color.DARK_GRAY, Color.GRAY, Color.GREEN, Color.LIGHT_GRAY,
			Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE,
			Color.YELLOW };

	/**
	 * <p>
	 * Class responsible for parallel computations.
	 * </p>
	 * 
	 * @author Piotr Waszkiewicz
	 *
	 */
	private static class Worker implements Runnable
	{
		private Image result;
		private int number;

		private Worker(Image result, int number)
		{
			this.result = result;
			this.number = number;
		}

		@Override
		public void run()
		{
			result = makeOpeningsAndClosings(result);
			result = ImageEffect.makeBorders(result, 255, 0, 3, new Color(255,
					0, 0).getRGB());

			rectanglesInSequence.get(number - 1).addAll(
					ImageEffect.getObjectAreas(result,
							new Color(255, 0, 0).getRGB(),
							new Color(0, 0, 0).getRGB()));

			System.out.println("Image " + number + " out of 570");
		}
	}

	private static class BacteriaSpy implements Runnable
	{
		private ShapeRectangle lastRectangle;
		private List<Point> pointsInPath;
		private int timesToDie = 5;
		private Color myColor;

		public BacteriaSpy(ShapeRectangle bacteria, Color color)
		{
			lastRectangle = bacteria;
			myColor = color;
			pointsInPath = new ArrayList<>();
		}

		@Override
		public void run()
		{
			/* For every frame */
			for (int i = 1; i < rectanglesInSequence.size(); i++)
			{
				/* Save last position */
				pointsInPath.add(new Point(lastRectangle.centerX,
						lastRectangle.centerY));

				/* Check all rectangles in new image */
				double smallestDist = Double.MAX_VALUE;
				int j = -1, smallestIndex = 0;
				for (ShapeRectangle r : rectanglesInSequence.get(i))
				{
					j++;
					if (r.sideX < SMALL_BACTERIA || r.sideY < SMALL_BACTERIA)
						continue;

					double distance = distanceTo(r.centerX, r.centerY,
							lastRectangle.centerX, lastRectangle.centerY);
					if (distance < smallestDist)
					{
						smallestIndex = j;
						smallestDist = distance;
					}
				}

				/* Check if smallest rectangle is indeed yours */
				ShapeRectangle probableNextPosition = rectanglesInSequence.get(
						i).get(smallestIndex);
				double distance = distanceTo(probableNextPosition.centerX,
						probableNextPosition.centerY, lastRectangle.centerX,
						lastRectangle.centerY);
				if ((int) distance < 3 * lastRectangle.sideX
						&& (int) distance < 3 * lastRectangle.sideY)
				{
					timesToDie = 5;
					lastRectangle = probableNextPosition;
				}
				else if ((--timesToDie) <= 0) break;

				/* Paint rectangle */
				drawMyRectangle(imagesInSequence.get(i));
			}

			drawMyPath();
		}

		private void drawMyRectangle(Image image)
		{
			Graphics g = image.getGraphics();
			g.setColor(myColor);
			g.drawRect(lastRectangle.centerX - lastRectangle.sideX / 2,
					lastRectangle.centerY - lastRectangle.sideY / 2,
					lastRectangle.sideX, lastRectangle.sideY);
			g.dispose();
		}
		
		private void drawMyPath()
		{
			Graphics g = imagesInSequence.get(imagesInSequence.size() - 1)
					.getGraphics();
			g.setColor(myColor);
			Point p1 = pointsInPath.get(0);
			for (int i = 1; i < pointsInPath.size(); i++)
			{
				Point p2 = pointsInPath.get(i);
				g.drawLine(p1.x, p1.y, p2.x, p2.y);
				p1 = p2;
			}
		}

		private double distanceTo(int x1, int y1, int x2, int y2)
		{
			return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		}
	}

	/***************/
	/* METHODS */
	/***************/
	/**
	 * <p>
	 * </p>
	 * 
	 * @see http
	 *      ://www.codeproject.com/Articles/10248/Motion-Detection-Algorithms
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			rectanglesInSequence = new ArrayList<>(NUMBER_OF_PHOTOS - 1);
			imagesInSequence = new ArrayList<>(NUMBER_OF_PHOTOS - 1);
			ExecutorService executor = Executors.newFixedThreadPool(9);
			long startTime = System.nanoTime();
			Image background = new Image(
					"res/photos2/s08-24h-glass-DMEMbrak-pbs20min_b0t" + 0
							+ "c0x0-2048y0-2048.png");

			for (int i = 1; i < NUMBER_OF_PHOTOS; i++)
			{
				Image frame = new Image(
						"res/photos2/s08-24h-glass-DMEMbrak-pbs20min_b0t" + i
								+ "c0x0-2048y0-2048.png");
				imagesInSequence.add(frame.getCopy());

				Image result = getBinarizedDifferences(background, frame);
				background = ImageEffect.moveTowards(background, frame, 1);
				rectanglesInSequence.add(new ArrayList<>());
				executor.execute(new Worker(result, i));
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			runRectangleColorizations();

			long endTime = System.nanoTime();
			long duration = (endTime - startTime) / 1000000;
			System.out.println("It took me " + duration / 1000 + " seconds");
			Thread.sleep(20 * 1000);
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private static void runRectangleColorizations()
	{
		int num = 0;
		Thread[] threads = new Thread[rectanglesInSequence.get(0).size()];

		for (ShapeRectangle r : rectanglesInSequence.get(0))
		{
			threads[num] = new Thread(new BacteriaSpy(r, colors[num
					% colors.length]));
			threads[num].start();
			num++;
		}

		for (Thread t : threads)
			try
			{
				t.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

		for (int i = 1; i < imagesInSequence.size(); i++)
			try
			{
				imagesInSequence.get(i).save("/tmp/result" + i + ".png");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
	}

	private static Image getBinarizedDifferences(Image background, Image frame)
	{
		return ImageEffect.binarizeImage(ImageEffect.subtractImages(
				ImageEffect.getGrayscale(background),
				ImageEffect.getGrayscale(frame)), TRESHOLD, BACKGROUND, SHAPE);
	}

	private static Image makeOpeningsAndClosings(Image image)
	{
		image = ImageEffect.dilatation(image, 255, 0, 5);
		image = ImageEffect.erosion(image, 255, 0, 7);
		return ImageEffect.dilatation(image, 255, 0, 3);
	}
}
