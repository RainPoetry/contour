package rainpoetry.java.common.tools;

/*
 * User: chenchong
 * Date: 2019/4/18
 * description:
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class GraphicsBuilder {

	private BufferedImage baseImg;
	private Graphics2D graphics2D;

	private GraphicsBuilder(int width, int height) {
		baseImg = transparencyImage(width, height, Transparency.BITMASK);
		graphics2D = baseImg.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		AlphaComposite ac = AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER);
		graphics2D.setComposite(ac);
	}

	public static GraphicsBuilder of(int width, int height) {
		return new GraphicsBuilder(width, height);
	}

	/**
	 * 叠加图片
	 *
	 * @param pic  图片路径
	 * @param left 左边距
	 * @param top  上边距
	 * @return
	 */
	public GraphicsBuilder putPic(String pic, int left, int top, float scaleX, float scaleY, int alpha) {
		File picSampleFile = new File(pic);
		if (!picSampleFile.exists())
			throw new IllegalArgumentException("mergedFile not found: " + pic);
		try {
			BufferedImage bis = ImageIO.read(picSampleFile);
			if (alpha != 255)
				bis = img_alpha(bis,alpha);
			int imgWidth = (int) (bis.getWidth() * scaleX);
			int imgHeight = (int) (bis.getHeight() * scaleY);
			int appendX = left < 0 ? 0 : left;
			int appendY = top < 0 ? 0 : top;
			graphics2D.drawImage(bis, appendX, appendY, imgWidth, imgHeight, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * 添加文字
	 *
	 * @param text     内容
	 * @param fontSize 字体大小
	 * @param left     左边距
	 * @param top      上边距
	 * @param color    字体颜色
	 * @return
	 */
	public GraphicsBuilder putWords(String text, int fontSize, int left, int top, Color color) {
		createMark(graphics2D, text, "微软雅黑", fontSize, left, top, color);
		return this;
	}

	private GraphicsBuilder createMark(Graphics2D g, String text, String fontType,
									 int fontSize, int left, int top, Color c) {
		AttributedString ats = new AttributedString(text);
		Font font = new Font(fontType, Font.BOLD, fontSize);
		g.setFont(font);
		// 消除java.awt.Font字体的锯齿
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		ats.addAttribute(TextAttribute.FONT, font, 0, text.length());
		AttributedCharacterIterator iter = ats.getIterator();
		g.setColor(c);
		g.drawString(iter, left, top);
		return this;
	}

	/**
	 * 最终调用，生成图片
	 *
	 * @param outputPath 图片路径
	 */
	public void build(String outputPath) {
		File destFile = new File(outputPath);
		if (!destFile.getParentFile().exists())
			destFile.getParentFile().mkdirs();
		OutputStream out = null;
		try {
			out = new FileOutputStream(new File(outputPath));
			ImageIO.write(baseImg, "png", out);
			out.close();
			flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage img_alpha(BufferedImage imgsrc, int alpha) {
		// 需要过滤掉的颜色
		int filterCode = new Color(255, 255, 255).hashCode();
		try {
			// 创建一个包含透明度的图片,半透明效果必须要存储为png合适才行，存储为jpg，底色为黑色
			BufferedImage back = new BufferedImage(imgsrc.getWidth(),
					imgsrc.getHeight(), BufferedImage.TYPE_INT_ARGB);
			int width = imgsrc.getWidth();
			int height = imgsrc.getHeight();
			for (int j = 0; j < height; j++) {
				for (int i = 0; i < width; i++) {
					int rgb = imgsrc.getRGB(i, j);
					Color color = new Color(rgb);
					// 判断图片是否透明
					if ((imgsrc.getRGB(i, j) >> 24) == 0) {
						continue;
					}
					if (color.hashCode() == filterCode) {
						Color newColor = new Color(color.getRed(),
								color.getGreen(), color.getBlue(), 0);
						back.setRGB(i, j, newColor.getRGB());
					} else {
						Color newColor = new Color(color.getRed(),
								color.getGreen(), color.getBlue(), alpha);
						back.setRGB(i, j, newColor.getRGB());
					}
				}
			}
			return back;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static BufferedImage transparencyImage(int width, int height, int transparency) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();
		// 设置画布透明模式
		bi = g2d.getDeviceConfiguration().createCompatibleImage(width, height, transparency);
		// 释放资源
		// graphics2D 使用的是系统资源，不释放的话，其他 graphics2D 的实例对象无法获取系统资源
		g2d.dispose();
		return bi;
	}

	private void flush() {
		if (graphics2D != null)
			graphics2D.dispose();
		if (baseImg != null)
			baseImg.flush();
	}

	public static void main(String[] args){

	}

}
