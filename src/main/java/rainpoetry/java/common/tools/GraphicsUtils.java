package rainpoetry.java.common.tools;

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

/**
 * User: chenchong
 * Date: 2019/2/21
 * description: 画板工具
 */
public class GraphicsUtils {

	private BufferedImage baseImg;
	private Graphics2D graphics2D;

	private GraphicsUtils(String base) throws IOException {
		File basePic = new File(base);
		if (!basePic.exists())
			throw new IllegalArgumentException("file not found: " + base);
		this.baseImg = ImageIO.read(basePic);
		this.graphics2D = baseImg.createGraphics();
		drawBase();
	}

	public static GraphicsUtils open(String base) {
		try {
			return new GraphicsUtils(base);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private GraphicsUtils drawBase() {
		return drawBase(false, 0, 0);
	}

	/**
	 * 描绘底图
	 *
	 * @param append       是否扩大底图(放大宽和高)
	 * @param appendWidth  放大的大小，如果 > 0 , 则底图做部分有留白，如果 < 0, 则底图的右部分有留白
	 * @param appendHeight
	 * @return
	 */
	private GraphicsUtils drawBase(boolean append, int appendWidth, int appendHeight) {
		AlphaComposite ac = AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER);
		graphics2D.setComposite(ac);
		if (!append) {
			graphics2D.drawImage(baseImg, 0, 0, baseImg.getWidth(), baseImg.getHeight(),
					null);
			return this;
		}
		int x = appendWidth > 0 ? appendWidth : 0;
		int y = appendHeight > 0 ? appendHeight : 0;
		graphics2D.drawImage(baseImg, x, y, baseImg.getWidth(), baseImg.getHeight(),
				null);
		return this;
	}

	public GraphicsUtils mergePic(String mergedFile, int left, int top) {
		return mergePic(mergedFile, left, top, 1, 1);
	}

	/**
	 * 叠加图片
	 *
	 * @param mergedFile 图片路径
	 * @param left       左边距
	 * @param top        上边距
	 * @return
	 */
	public GraphicsUtils mergePic(String mergedFile, int left, int top, float scaleX, float scaleY) {
		File picSampleFile = new File(mergedFile);
		if (!picSampleFile.exists())
			throw new IllegalArgumentException("mergedFile not found: " + mergedFile);
		try {
			BufferedImage bis = ImageIO.read(picSampleFile);
			int imgWidth = (int) (bis.getWidth() * scaleX);
			int imgHeight = (int) (bis.getHeight() * scaleY);
			appendGraphics(left,top,imgWidth,imgHeight);
			int appendX = left < 0 ? 0 : left;
			int appendY = top < 0 ? 0 : top;
			graphics2D.drawImage(bis, appendX, appendY,imgWidth , imgHeight, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public void appendGraphics(int left, int top, int imgWidth, int imgHeight) {
		int width = baseImg.getWidth();
		int height = baseImg.getHeight();
		if (left < 0) {
			width += left;
		} else {
			width = Math.max(width, left + imgWidth);
		}
		if (top < 0) {
			height += top;
		} else {
			height = Math.max(height, top + imgHeight);
		}
		if (width > baseImg.getWidth() || height > baseImg.getHeight()) {
			BufferedImage bufferedImage = transparencyImage(width, height, Transparency.BITMASK);
			graphics2D.dispose();
			graphics2D = bufferedImage.createGraphics();
			drawBase(true, left, top);
		}
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
	public GraphicsUtils putWords(String text, int fontSize, int left, int top, Color color) {
		createMark(graphics2D, text, "微软雅黑", fontSize, left, top, color);
		return this;
	}

	private GraphicsUtils createMark(Graphics2D g, String text, String fontType,
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

	// 设置生成图片的透明模式
	// 参数 transparency  ---  对应的值：(Transparency.BITMASK/Transparency.OPAQUE/Transparency.TRANSLUCENT)
	// 		Transparency 接口用于控制透明模式
	//			 Transparency.BITMASK 		--  完全透明或者完全不透明
	//		 	 Transparency.OPAQUE	 	--  不透明
	//			 Transparency.TRANSLUCENT	--	透明度在 0-1 之间
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

	/**
	 * 透明度处理
	 *
	 * @param imgsrc
	 * @param alpha
	 * @return
	 */
	public static BufferedImage img_alpha(BufferedImage imgsrc, int alpha) {
		// 需要过滤掉的颜色
		int filterCode = new Color(255, 255, 255).hashCode();
		try {
			// 创建一个包含透明度的图片,半透明效果必须要存储为png合适才行，存储为jpg，底色为黑色
			BufferedImage back = transparencyImage(imgsrc.getWidth(),
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

	private void flush() {
		if (graphics2D != null)
			graphics2D.dispose();
		if (baseImg != null)
			baseImg.flush();
	}

	public static void main(String[] args) {
		GraphicsUtils.open("G:/tmp/demo.png")
				.mergePic("G:/tmp/demo_r.png", -500, 0, 2.0f, 2.0f)
				.putWords("Hello World", 60, 300, 50, Color.BLACK)
				.build("G:/tmp/final.jpg");
	}
}
