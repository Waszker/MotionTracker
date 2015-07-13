package image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * <p>
 * Class representing image and providing methods to easily operate on it.
 * </p>
 * 
 * @author Piotr Waszkiewicz
 *
 */
public class Image
{
	/***************/
	/* VARIABLES */
	/***************/
	private BufferedImage image;
	private int redChannelMask = 0x00FF0000;
	private int greenChannelMask = 0x0000FF00;
	private int blueChannelMask = 0x000000FF;

	/***************/
	/* METHODS */
	/***************/
	/**
	 * <p>
	 * Constructs image from given path.
	 * </p>
	 * 
	 * @param pathname
	 * @throws IOException
	 */
	public Image(String pathname) throws IOException
	{
		image = ImageIO.read(new File(pathname));
	}

	/**
	 * <p>
	 * Constructs empty image with given resolution.
	 * </p>
	 * 
	 * @param width
	 * @param height
	 */
	public Image(int width, int height)
	{
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException("Dimensions must be positive!");

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * <p>
	 * Constructs image with given resolution and background color.
	 * </p>
	 * 
	 * @param width
	 * @param height
	 */
	public Image(int width, int height, int color)
	{
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException("Dimensions must be positive!");

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int w = 0; w < image.getWidth(); w++)
			for (int h = 0; h < image.getHeight(); h++)
				image.setRGB(w, h, color);
	}

	/**
	 * <p>
	 * Constructs Image object from provided image.
	 * </p>
	 * 
	 * @param image
	 */
	public Image(BufferedImage image)
	{
		this.image = new BufferedImage(image.getWidth(), image.getHeight(),
				image.getType());
		Graphics g = this.image.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
	}

	/**
	 * <p>
	 * Returns value of red channel in given position in image.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getRed(int x, int y)
	{
		int rgb = image.getRGB(x, y);
		return (rgb & redChannelMask) >> 16;
	}

	/**
	 * <p>
	 * Returns value of green channel in given position in image.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getGreen(int x, int y)
	{
		int rgb = image.getRGB(x, y);
		return (rgb & greenChannelMask) >> 8;
	}

	/**
	 * <p>
	 * Returns value of blue channel in given position in image.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getBlue(int x, int y)
	{
		int rgb = image.getRGB(x, y);
		return (rgb & blueChannelMask);
	}

	/**
	 * <p>
	 * Sets pixel RGB value at given position.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param rgb
	 */
	public void setRGB(int x, int y, int rgb)
	{
		image.setRGB(x, y, rgb);
	}

	/**
	 * <p>
	 * Returns pixel RGB value.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getRGB(int x, int y)
	{
		return image.getRGB(x, y);
	}

	/**
	 * <p>
	 * Sets RGB value for given pixel position.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void setRGB(int x, int y, int red, int green, int blue)
	{
		if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0
				|| blue > 255)
			throw new IllegalArgumentException(
					"Channel values must be between 0 and 255");

		image.setRGB(x, y, (new Color(red, green, blue)).getRGB());
	}

	/**
	 * <p>
	 * Sets ARGB value for given pixel.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param alpha
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void setARGB(int x, int y, int alpha, int red, int green, int blue)
	{
		if (alpha < 0 || alpha > 255 || red < 0 || red > 255 || green < 0
				|| green > 255 || blue < 0 || blue > 255)
			throw new IllegalArgumentException(
					"Channel values must be between 0 and 255");

		image.setRGB(x, y, (new Color(red, green, blue, alpha)).getRGB());
	}

	/**
	 * <p>
	 * Returns width (in pixels) of image.
	 * </p>
	 * 
	 * @return
	 */
	public int getWidth()
	{
		return image.getWidth();
	}

	/**
	 * <p>
	 * Returns height (in pixels) of image.
	 * </p>
	 * 
	 * @return
	 */
	public int getHeight()
	{
		return image.getHeight();
	}

	/**
	 * <p>
	 * Shows image in new window.
	 * </p>
	 */
	public void show()
	{
		JFrame window = new JFrame("Image");
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(image));
		window.setSize(image.getWidth(), image.getHeight());
		window.add(label);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	/**
	 * <p>
	 * Returns the deep copy of this object.
	 * </p>
	 * 
	 * @return
	 */
	public Image getCopy()
	{
		return new Image(image);
	}

	/**
	 * <p>
	 * Returns graphic object associated with this image.
	 * </p>
	 * 
	 * @return
	 */
	public Graphics getGraphics()
	{
		return image.getGraphics();
	}

	/**
	 * <p>
	 * Saves image to file.
	 * </p>
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void save(String filename) throws IOException
	{
		ImageIO.write(image, "png", new File(filename));
	}
}
