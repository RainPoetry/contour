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

	private final BufferedImage baseImg;
	private final Graphics2D graphics2D;

	private GraphicsUtils(String base) throws IOException {
		File basePic = new File(base);
		if (!basePic.exists())
			throw new IllegalArgumentException("file not found: " + base);
		this.baseImg = ImageIO.read(basePic);
		this.graphics2D = baseImg.createGraphics();
		drawBase();
	}

	/**
	 * 图片处理器
	 */
	public static class PictureBuilder {
		public static GraphicsUtils open(String base) {
			try {
				return new GraphicsUtils(base);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
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
			graphics2D.drawImage(bis, left, top, (int) (bis.getWidth() * scaleX),
					(int) (bis.getHeight() * scaleY), null);
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

	private void flush() {
		if (graphics2D != null)
			graphics2D.dispose();
		if (baseImg != null)
			baseImg.flush();
	}

	public static void main(String[] args) {
		PictureBuilder.open("G:/118256.jpg")
				.mergePic("G:/212406.jpg", 600, 300,1.5f,1.5f)
				.putWords("Hello World", 60, 300, 50, Color.BLACK)
				.build("G:/final.jpg");
	}
}
