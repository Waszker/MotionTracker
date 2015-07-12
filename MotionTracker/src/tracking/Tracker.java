package tracking;

import image.Image;
import imageutils.ImageEffect;

import java.awt.Color;
import java.io.IOException;

public class Tracker
{
	/***************/
	/* VARIABLES */
	/***************/

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
			long startTime = System.nanoTime();
			Image background = new Image(
					"res/photos/s08-24h-glass-DMEMbrak-pbs20min_b0t" + 0
							+ "c0x0-2048y0-2048.png");

			for (int i = 1; i < 570; i++)
			{
				Image frame = new Image(
						"res/photos/s08-24h-glass-DMEMbrak-pbs20min_b0t"
								+ (i + 1) + "c0x0-2048y0-2048.png");

				Image result = ImageEffect.binarizeImage(ImageEffect
						.subtractImages(ImageEffect.getGrayscale(background),
								ImageEffect.getGrayscale(frame)), 5, 0, 255);
				background = ImageEffect.moveTowards(background, frame, 1);
				result = ImageEffect.dilatation(result, 255, 0, 13);
				result = ImageEffect.erosion(result, 255, 0, 15);
				result = ImageEffect.dilatation(result, 255, 0, 3);
				frame = ImageEffect.getGrayscale(frame);
				result = ImageEffect.makeBorders(
						ImageEffect.binarizeImage(
								ImageEffect.getGrayscale(result), 5, 0, 255),
						255, 0, 3, new Color(255, 0, 0).getRGB());
				result = ImageEffect.getObjectAreas(result,
						new Color(255, 0, 0).getRGB(),
						new Color(255, 255, 255).getRGB());
				result = ImageEffect.makeBorders(
						ImageEffect.binarizeImage(
								ImageEffect.getGrayscale(result), 5, 0, 255),
						255, 0, 3, new Color(255, 0, 0).getRGB());

				for (int w = 0; w < frame.getWidth(); w++)
					for (int h = 0; h < frame.getHeight(); h++)
						if (result.getRed(w, h) == 255)
							frame.setARGB(w, h, 255, 255, 0, 0);

				frame.show();
				Thread.sleep(2 * 1000);
				System.out.println("Image " + i + " out of 570");
			}

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

}
