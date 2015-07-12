package imageutils;

import image.Image;

import java.awt.Color;

/**
 * <p>
 * Class used to apply different filters on images.
 * </p>
 * 
 * @author Piotr Waszkiewicz
 *
 */
public final class ImageEffect
{
	/***************/
	/* VARIABLES */
	/***************/

	/***************/
	/* METHODS */
	/***************/
	/**
	 * <p>
	 * Puts image under erosion process that somewhat reduces noise in the
	 * picture. Image must be in binarized form.
	 * </p>
	 * 
	 * @param img
	 * @param shapeChannelValue
	 * @param backgroundChannelValue
	 * @param maskSize
	 * @return
	 */
	public static Image erosion(Image img, int shapeChannelValue,
			int backgroundChannelValue, int maskSize)
	{
		if (maskSize < 1)
			throw new IllegalArgumentException("Mask size must be above 1");
		if (maskSize % 2 == 0)
			throw new IllegalArgumentException(
					"Mask size must be an odd number");

		Image result = new Image(img.getWidth(), img.getHeight(), (new Color(
				backgroundChannelValue, backgroundChannelValue,
				backgroundChannelValue).getRGB()));

		for (int w = (maskSize - 1) / 2; w < img.getWidth() - (maskSize - 1)
				/ 2; w++)
			for (int h = (maskSize - 1) / 2; h < img.getHeight()
					- (maskSize - 1) / 2; h++)
			{
				boolean isInsideShape = true;

				LOOP: for (int i = -1 * (maskSize - 1) / 2; i <= (maskSize - 1) / 2; i++)
					for (int j = -1 * (maskSize - 1) / 2; j <= (maskSize - 1) / 2; j++)
						if (img.getRed(w + i, h + j) == backgroundChannelValue)
						{
							isInsideShape = false;
							break LOOP;
						}

				if (isInsideShape)
					result.setRGB(w, h, shapeChannelValue, shapeChannelValue,
							shapeChannelValue);
				else
					result.setRGB(w, h, backgroundChannelValue,
							backgroundChannelValue, backgroundChannelValue);
			}

		return result;
	}

	/**
	 * <p>
	 * Puts image under dilatation process that somewhat increases shapes'
	 * fatness in the picture. Image must be in binarized form.
	 * </p>
	 * 
	 * @param img
	 * @param shapeChannelValue
	 * @param backgroundChannelValue
	 * @param maskSize
	 * @return
	 */
	public static Image dilatation(Image img, int shapeChannelValue,
			int backgroundChannelValue, int maskSize)
	{
		if (maskSize < 1)
			throw new IllegalArgumentException("Mask size must be above 1");
		if (maskSize % 2 == 0)
			throw new IllegalArgumentException(
					"Mask size must be an odd number");

		Image result = new Image(img.getWidth(), img.getHeight(), (new Color(
				backgroundChannelValue, backgroundChannelValue,
				backgroundChannelValue).getRGB()));

		for (int w = (maskSize - 1) / 2; w < img.getWidth() - (maskSize - 1)
				/ 2; w++)
			for (int h = (maskSize - 1) / 2; h < img.getHeight()
					- (maskSize - 1) / 2; h++)
				if (img.getRed(w, h) == shapeChannelValue)
				{
					for (int i = -1 * (maskSize - 1) / 2; i <= (maskSize - 1) / 2; i++)
						for (int j = -1 * (maskSize - 1) / 2; j <= (maskSize - 1) / 2; j++)
							result.setRGB(w + i, h + j, shapeChannelValue,
									shapeChannelValue, shapeChannelValue);
				}

		return result;
	}

	/**
	 * <p>
	 * Binarizes picture - converts it to two color only.
	 * </p>
	 * 
	 * @param img
	 * @param treshold
	 * @param backgroundChannelValue
	 * @param shapeChannelValue
	 * @return
	 */
	public static Image binarizeImage(Image img, int treshold,
			int backgroundChannelValue, int shapeChannelValue)
	{
		if (backgroundChannelValue < 0 || backgroundChannelValue > 255
				|| shapeChannelValue < 0 || shapeChannelValue > 255)
			throw new IllegalArgumentException(
					"Both channel values must be between 0 and 255");

		Image result = new Image(img.getWidth(), img.getHeight());

		for (int w = 0; w < img.getWidth(); w++)
			for (int h = 0; h < img.getHeight(); h++)
				if (img.getRed(w, h) > treshold)
					result.setRGB(w, h, shapeChannelValue, shapeChannelValue,
							shapeChannelValue);
				else
					result.setRGB(w, h, backgroundChannelValue,
							backgroundChannelValue, backgroundChannelValue);

		return result;
	}

	/**
	 * <p>
	 * Subtracts two images and returns the result.
	 * </p>
	 * 
	 * @param img1
	 * @param img2
	 * @return
	 */
	public static Image subtractImages(Image img1, Image img2)
	{
		if (img1.getWidth() != img2.getWidth()
				|| img1.getHeight() != img2.getHeight())
			throw new IllegalArgumentException(
					"Both images should have the same resolution");

		Image result = new Image(img1.getWidth(), img1.getHeight());

		for (int w = 0; w < img1.getWidth(); w++)
			for (int h = 0; h < img1.getHeight(); h++)
				result.setRGB(w, h,
						Math.abs(img1.getRed(w, h) - img2.getRed(w, h)),
						Math.abs(img1.getGreen(w, h) - img2.getGreen(w, h)),
						Math.abs(img1.getBlue(w, h) - img2.getBlue(w, h)));

		return result;
	}

	/**
	 * <p>
	 * Returns grayscaled image of provided one.
	 * </p>
	 * 
	 * @param img
	 * @return
	 */
	public static Image getGrayscale(Image img)
	{
		Image result = new Image(img.getWidth(), img.getHeight());

		for (int w = 0; w < img.getWidth(); w++)
			for (int h = 0; h < img.getHeight(); h++)
			{
				int gray = (img.getRed(w, h) + img.getGreen(w, h) + img
						.getBlue(w, h)) / 3;
				result.setRGB(w, h, gray, gray, gray);
			}

		return result;
	}

	/**
	 * Move towards filter is used to make source image more like destination
	 * one. The update equation is defined in the next way: res = src + Min(
	 * Abs( ovr - src ), step ) * Sign( ovr - src ).
	 * 
	 * @param source
	 * @param destination
	 * @return
	 */
	public static Image moveTowards(Image source, Image destination, int step)
	{
		if (source.getWidth() != destination.getWidth()
				|| source.getHeight() != destination.getHeight())
			throw new IllegalArgumentException(
					"Both images should have the same resolution");
		Image result = new Image(source.getWidth(), source.getHeight());

		for (int w = 0; w < source.getWidth(); w++)
			for (int h = 0; h < source.getHeight(); h++)
			{
				int red = (source.getRed(w, h) + Math.min(Math.abs(destination
						.getRed(w, h) - source.getRed(w, h)), step)
						* (int) Math.signum(destination.getRed(w, h)
								- source.getRed(w, h)));
				int green = (source.getGreen(w, h) + Math.min(
						Math.abs(destination.getRed(w, h)
								- source.getGreen(w, h)), step)
						* (int) Math.signum(destination.getGreen(w, h)
								- source.getGreen(w, h)));
				int blue = (source.getBlue(w, h) + Math.min(
						Math.abs(destination.getBlue(w, h)
								- source.getBlue(w, h)), step)
						* (int) Math.signum(destination.getRed(w, h)
								- source.getBlue(w, h)));

				result.setRGB(w, h, red, green, blue);
			}

		return result;
	}

	private ImageEffect()
	{
		// This class should serve as "final abstract" class
		// And so that's why its only constructor is private
	}
}
