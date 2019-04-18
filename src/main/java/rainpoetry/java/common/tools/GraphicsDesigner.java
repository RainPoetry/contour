package rainpoetry.java.common.tools;

/*
 * User: chenchong
 * Date: 2019/4/18
 * description:
 */

import rainpoetry.java.draw.bean.Tuple2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphicsDesigner {

	private final File src;
	private final List<Pic> picList = new ArrayList<>();
	private final List<Words> textList = new ArrayList<>();

	private GraphicsDesigner(File src) {
		this.src = src;
	}

	public static GraphicsDesigner open(String src) {
		File srcFile = new File(src);
		if (!srcFile.exists())
			throw new IllegalArgumentException("file not found: " + src);
		return new GraphicsDesigner(srcFile);
	}

	public GraphicsDesigner putPic(String file, int left, int top) {
		return putPic(file, left, top, 1.0f, 1.0f, 255);
	}

	public GraphicsDesigner putPic(String file, int left, int top, float scaleX, float scaleY) {
		return putPic(file, left, top, scaleX, scaleY, 255);
	}

	public GraphicsDesigner putPic(String file, int left, int top, float scaleX, float scaleY, int alpha) {
		File picSampleFile = new File(file);
		if (!picSampleFile.exists())
			throw new IllegalArgumentException("file not found: " + file);
		try {
			BufferedImage bufferedImage = ImageIO.read(picSampleFile);
			picList.add(new Pic(file, bufferedImage.getWidth(), bufferedImage.getHeight(),
					left, top, scaleX, scaleY, alpha));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public GraphicsDesigner putWords(String text, int fontSize, int left, int top, Color color) {
		textList.add(new Words(text, fontSize, left, top, color));
		return this;
	}


	public void create(String path) {
		try {
			BufferedImage srcImg = ImageIO.read(src);
			Tuple2<Integer, Integer> start = new Tuple2<>(0, 0);
			int width = srcImg.getWidth();
			int height = srcImg.getHeight();
			int left = 0, top = 0, right = 0, bottom = 0;
			for (Pic pic : picList) {
				if (pic.left < 0) {
					left = Math.max(left, Math.abs(pic.left));
				} else {
					right = Math.max(right, pic.width + pic.left - width);
				}
				if (pic.top < 0) {
					top = Math.max(top, Math.abs(pic.top));
				} else {
					top = Math.max(top, pic.height + pic.top - height);
				}
			}
			for (Words pic : textList) {
				if (pic.left < 0) {
					left = Math.max(left, Math.abs(pic.left));
				} else {
					right = Math.max(right, pic.left - width);
				}
				if (pic.top < 0) {
					top = Math.max(top, Math.abs(pic.top));
				} else {
					top = Math.max(top, pic.top - height);
				}
			}
			int realWidth = width + left + right;
			int realHeight = height + top + bottom;
			GraphicsBuilder builder = GraphicsBuilder.of(realWidth, realHeight)
					.putPic(src.getCanonicalPath(), left, top, 1.0f, 1.0f, 255);
			for (Pic pic : picList) {
				builder.putPic(pic.path, pic.left + left, pic.top + top, pic.scaleX, pic.scaleY, pic.alpha);
			}
			for (Words words : textList) {
				builder.putWords(words.text, words.fontSIze, words.left + left, words.top + top, words.color);
			}
			builder.build(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	class Words {
		String text;
		int fontSIze;
		int left;
		int top;
		Color color;

		public Words(String text, int fontSIze, int left, int top, Color color) {
			this.text = text;
			this.fontSIze = fontSIze;
			this.left = left;
			this.top = top;
			this.color = color;
		}
	}

	class Pic {
		String path;
		int width;
		int height;
		int left;
		int top;
		float scaleX;
		float scaleY;
		int alpha;

		public Pic(String path, int width, int height, int left, int top, float scaleX, float scaleY, int alpha) {
			this.path = path;
			this.width = width;
			this.height = height;
			this.left = left;
			this.top = top;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			this.alpha = alpha;
		}
	}

	public static void main(String[] args) {

		GraphicsDesigner.open("G:/tmp/demo.png")
				.putPic("G:/tmp/demo_r.png", -500, 0, 2.0f, 2.0f, 255)
				.putWords("Hello World", 60, 300, 50, Color.BLACK)
				.create("G:/tmp/final.jpg");
	}
}
