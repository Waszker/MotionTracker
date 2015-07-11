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
			Image im1 = new Image(
					"res/aa.png");
			Image im2 = new Image(
					"res/bb.png");
			Image result = ImageEffect.binarizeImage(
					ImageEffect.subtractImages(im1, im2), 5, 0, 255);
			result = ImageEffect.erosion(result, 255, 0, 3);
			result = ImageEffect.dilatation(result, 255, 0, 9);

			result.show();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
