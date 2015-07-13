package tracking;

import image.Image;
import imageutils.ImageEffect;
import imageutils.ImageEffect.ShapeRectangle;

import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Tracker
{
	/***************/
	/* VARIABLES */
	/***************/
	private static final int BACKGROUND = 0;
	private static final int SHAPE = 255;
	private static final int TRESHOLD = 5;

	private static class Worker implements Runnable
	{
		private Image result;
		private Image frame;
		private int number;

		private Worker(Image result, Image frame, int number)
		{
			this.result = result;
			this.frame = frame;
			this.number = number;
		}

		@Override
		public void run()
		{
			result = makeOpeningsAndClosings(result);
			result = ImageEffect.makeBorders(result, 255, 0, 3, new Color(255,
					0, 0).getRGB());
			result = getRectanglesOnBlack(
					frame,
					ImageEffect.getObjectAreas(result,
							new Color(255, 0, 0).getRGB(),
							new Color(0, 0, 0).getRGB()));

			frame = ImageEffect.getGrayscale(frame);
			for (int w = 0; w < frame.getWidth(); w++)
				for (int h = 0; h < frame.getHeight(); h++)
					if (result.getRed(w, h) == 255)
						frame.setARGB(w, h, 255, 255, 0, 0);

			System.out.println("Image " + number + " out of 570");
			try
			{
				frame.save("/tmp/result" + number + ".png");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
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
			ExecutorService executor = Executors.newFixedThreadPool(9);
			long startTime = System.nanoTime();
			Image background = new Image(
					"res/photos2/s08-24h-glass-DMEMbrak-pbs20min_b0t" + 0
							+ "c0x0-2048y0-2048.png");

			for (int i = 1; i < 570; i++)
			{
				Image frame = new Image(
						"res/photos2/s08-24h-glass-DMEMbrak-pbs20min_b0t"
								+ (i + 1) + "c0x0-2048y0-2048.png");

				Image result = getBinarizedDifferences(background, frame);
				background = ImageEffect.moveTowards(background, frame, 1);
				executor.execute(new Worker(result, frame, i));
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

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

	private static Image getRectanglesOnBlack(final Image image,
			List<ShapeRectangle> rectangles)
	{
		Image result = new Image(image.getWidth(), image.getHeight());

		Graphics g = result.getGraphics();
		g.setColor(Color.RED);
		for (ShapeRectangle r : rectangles)
			if (r.sideX > 20 || r.sideY > 20)
				g.drawRect(r.centerX - r.sideX / 2, r.centerY - r.sideY / 2,
						r.sideX, r.sideY);
		g.dispose();

		return result;
	}

}
