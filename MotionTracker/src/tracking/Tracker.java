package tracking;

import java.io.IOException;

import image.Image;
import imageutils.ImageEffect;

public class Tracker
{
	/***************/
	/* VARIABLES */
	/***************/

	/***************/
	/* METHODS */
	/***************/
	public static void main(String[] args)
	{
		try
		{
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
				result = ImageEffect.dilatation(result, 255, 0, 13);
				result = ImageEffect.erosion(result, 255, 0, 15);
				result = ImageEffect.dilatation(result, 255, 0, 3);

				for (int w = 0; w < frame.getWidth(); w++)
					for (int h = 0; h < frame.getHeight(); h++)
						if (result.getRed(w, h) == 255)
							frame.setARGB(w, h, 255, 255, 0, 0);

				result.show();
				frame.show();
				background = ImageEffect.moveTowards(background, frame, 1);
				Thread.sleep(20 * 1000);
			}
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

}
