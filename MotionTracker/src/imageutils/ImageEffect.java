package imageutils;

import image.Image;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Class used to apply different filters on images.
 * </p>
 * 
 * @author Piotr Waszkiewicz
 *
 */
public class ImageEffect
{
	/***************/
	/* VARIABLES */
	/***************/
	public static final class ShapeRectangle
	{
		public int centerX, centerY;
		public int sideX, sideY;
	}

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
	 * Returns image binarized using Saurola method. Source image must be in
	 * grayscale, otherwise results are undefined.
	 * </p>
	 * 
	 * @param img
	 * @param maskDimension
	 * @param backgroundChannelValue
	 * @param shapeChannelValue
	 * @return
	 */
	public static Image binarizeSaurola(Image img, int maskDimension,
			int backgroundChannelValue, int shapeChannelValue)
	{
		if (backgroundChannelValue < 0 || backgroundChannelValue > 255
				|| shapeChannelValue < 0 || shapeChannelValue > 255)
			throw new IllegalArgumentException(
					"Both channel values must be between 0 and 255");

		if (maskDimension % 2 == 0)
			throw new IllegalArgumentException(
					"Mask size must be an odd number.");

		Image result = new Image(img.getWidth(), img.getHeight());
		int maskHalfDim = maskDimension / 2;

		for (int w = maskHalfDim; w < img.getWidth() - maskHalfDim; w++)
			for (int h = maskHalfDim; h < img.getHeight() - maskHalfDim; h++)
			{
				double sumPixelR = 0;
				double sigma = 0;

				/* Getting average pixel value */
				for (int i = -1 * maskHalfDim; i <= maskHalfDim; i++)
					for (int j = -1 * maskHalfDim; j <= maskHalfDim; j++)
						sumPixelR += img.getRed(w + i, h + j);
				sumPixelR /= Math.pow(maskDimension, 2);

				/* Getting sigma */
				for (int i = -1 * maskHalfDim; i <= maskHalfDim; i++)
					for (int j = -1 * maskHalfDim; j <= maskHalfDim; j++)
						sigma += Math.pow(
								(img.getRed(w + i, h + j) - sumPixelR), 2);
				sigma = (int) Math.sqrt(sigma / Math.pow(maskDimension, 2));

				/* Getting average treshold */
				sumPixelR = (int) (sumPixelR * (1 + (0.5) * (sigma / 128 - 1)));
				int res = 0;
				if (img.getRed(w, h) > sumPixelR) res = 255;
				result.setRGB(w, h, res, res, res);
			}

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
	 * <p>
	 * Move towards filter is used to make source image more like destination
	 * one. The update equation is defined in the next way: res = src + Min(
	 * Abs( ovr - src ), step ) * Sign( ovr - src ).
	 * </p>
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

	/**
	 * <p>
	 * Draws border around objects on binarized image.
	 * </p>
	 * 
	 * @param img
	 * @param shapeChannelValue
	 * @param backgroundChannelValue
	 * @param borderWidth
	 * @param color
	 * @return
	 */
	public static Image makeBorders(Image img, int shapeChannelValue,
			int backgroundChannelValue, int borderWidth, int color)
	{
		Image result = dilatation(img, shapeChannelValue,
				backgroundChannelValue, borderWidth);
		result = subtractImages(result, img);

		for (int w = 0; w < result.getWidth(); w++)
			for (int h = 0; h < result.getHeight(); h++)
				if (result.getRed(w, h) == shapeChannelValue)
					result.setRGB(w, h, color);

		return result;

	}

	/**
	 * <p>
	 * Returns list of rectangles within which detected shape are placed.
	 * </p>
	 * 
	 * @param img
	 * @return
	 */
	public static List<ShapeRectangle> getObjectAreas(Image img,
			int boundColor, int backgroundColor)
	{
		Image copyImg = img.getCopy();
		List<ShapeRectangle> rectangles = new ArrayList<>();

		for (int w = 0; w < img.getWidth(); w++)
			for (int h = 0; h < img.getHeight(); h++)
				if (copyImg.getRGB(w, h) == boundColor)
					rectangles.add(getRectangleForShape(copyImg, w, h,
							backgroundColor));

		return rectangles;
	}

	private ImageEffect()
	{
		// This class should serve as "final abstract" class
		// And so that's why its only constructor is private
	}

	private static ShapeRectangle getRectangleForShape(Image img, int x, int y,
			int backgroundColor)
	{
		int shapeColor = img.getRGB(x, y);
		List<Integer> pointsX = new ArrayList<>();
		List<Integer> pointsY = new ArrayList<>();

		getShapePoints(img, x, y, shapeColor, backgroundColor, pointsX, pointsY);

		Integer[][] points = new Integer[2][];
		points[0] = pointsX.toArray(new Integer[pointsX.size()]);
		points[1] = pointsY.toArray(new Integer[pointsY.size()]);

		return createRectangleForPoints(points);
	}

	private static void getShapePoints(Image img, int x, int y, int shapeColor,
			int backgroundColor, List<Integer> pointsX, List<Integer> pointsY)
	{
		pointsX.add(x);
		pointsY.add(y);
		img.setRGB(x, y, backgroundColor);

		// Left
		if (x > 0 && img.getRGB(x - 1, y) == shapeColor)
			getShapePoints(img, x - 1, y, shapeColor, backgroundColor, pointsX,
					pointsY);
		// Left top
		if (x > 0 && y > 0 && img.getRGB(x - 1, y - 1) == shapeColor)
			getShapePoints(img, x - 1, y - 1, shapeColor, backgroundColor,
					pointsX, pointsY);
		// Top
		if (y > 0 && img.getRGB(x, y - 1) == shapeColor)
			getShapePoints(img, x, y - 1, shapeColor, backgroundColor, pointsX,
					pointsY);
		// Right top
		if (x < img.getWidth() - 1 && y > 0
				&& img.getRGB(x + 1, y - 1) == shapeColor)
			getShapePoints(img, x + 1, y - 1, shapeColor, backgroundColor,
					pointsX, pointsY);
		// Right
		if (x < img.getWidth() - 1 && img.getRGB(x + 1, y) == shapeColor)
			getShapePoints(img, x + 1, y, shapeColor, backgroundColor, pointsX,
					pointsY);
		// Right down
		if (x < img.getWidth() - 1 && y < img.getWidth() - 1
				&& img.getRGB(x + 1, y + 1) == shapeColor)
			getShapePoints(img, x + 1, y + 1, shapeColor, backgroundColor,
					pointsX, pointsY);
		// Down
		if (y < img.getWidth() - 1 && img.getRGB(x, y + 1) == shapeColor)
			getShapePoints(img, x, y + 1, shapeColor, backgroundColor, pointsX,
					pointsY);
		// Left down
		if (x > 0 && y < img.getWidth() - 1
				&& img.getRGB(x - 1, y + 1) == shapeColor)
			getShapePoints(img, x - 1, y + 1, shapeColor, backgroundColor,
					pointsX, pointsY);
	}

	private static ShapeRectangle createRectangleForPoints(Integer[][] points)
	{
		ShapeRectangle rectangle = new ShapeRectangle();
		int xSum = 0, ySum = 0;

		for (Integer i : points[0])
			xSum += i;
		for (Integer i : points[1])
			ySum += i;
		rectangle.centerX = xSum / points[0].length;
		rectangle.centerY = ySum / points[1].length;

		Arrays.sort(points[0]);
		Arrays.sort(points[1]);
		rectangle.sideX = points[0][points[0].length - 1] - points[0][0];
		rectangle.sideY = points[1][points[1].length - 1] - points[1][0];

		return rectangle;
	}
}
