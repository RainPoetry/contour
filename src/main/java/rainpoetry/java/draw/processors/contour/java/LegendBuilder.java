package rainpoetry.java.draw.processors.contour.java;

/*
 * User: chenchong
 * Date: 2019/4/17
 * description:
 */

import rainpoetry.java.draw.bean.Tuple2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class LegendBuilder {

	private Color[] colors;
	private double[] values;
	private boolean legendV = true;
	private String path;
	private String unit = "";

	public static LegendBuilder newInstance() {
		return new LegendBuilder();
	}

	public LegendBuilder colors(Color[] colors) {
		this.colors = colors;
		return this;
	}

	public LegendBuilder path(String path) {
		this.path = path;
		return this;
	}

	public LegendBuilder unit(String unit) {
		this.unit = unit;
		return this;
	}

	public LegendBuilder values(double[] values) {
		this.values = values;
		return this;
	}

	public LegendBuilder legend(boolean legendV) {
		this.legendV = legendV;
		return this;
	}

	public void create() {
		try {
			Cache cache = createLegend();
			BufferedImage bi = new BufferedImage(cache.rwidth, cache.rheight,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bi.createGraphics();
			bi = g2d.getDeviceConfiguration().createCompatibleImage(cache.rwidth, cache.rheight,
					Transparency.TRANSLUCENT);
			Graphics2D g2 = bi.createGraphics();
			if (legendV) {
				this.drawLegendTable_v(g2, cache);
			} else {
				this.drawLegendTable_h(g2, cache);
			}
			g2.dispose();
			// 保存文件
			OutputStream out = new FileOutputStream(new File(path
					+ ".png"));
			ImageIO.write(bi, "png", out);
			out.close();
			bi.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Cache createLegend() {
		Cache cache = new Cache();
		int legendWidth, legendHeight, rheight, rwidth;
		// 图例方向
		if (legendV) {
			// 图例纵向显示
			legendWidth = 50;
			legendHeight = 30;
			cache.start = new Tuple2<>(5, 20);
			rheight = (int) (legendHeight * colors.length + 50);
			rwidth = legendWidth * 3;
		} else {
			// 图例横向显示
			if (this.colors.length >= 7) {
				legendWidth = 35;
			} else
				legendWidth = 50;
			legendHeight = 15;
			cache.start = new Tuple2<>(15, 20);
			rheight = legendHeight * 3;
			rwidth = legendWidth * colors.length + 100;
		}
		cache.legendHeight = legendHeight;
		cache.legendWidth = legendWidth;
		cache.rheight = rheight;
		cache.rwidth = rwidth;
		return cache;
	}

	private void drawLegendTable_v(Graphics2D g, Cache cache) {
		// 纵向颜色参照对比表
		int startY = cache.start._2;
		int startX = cache.start._1;
		System.out.println(this.colors.length);
		for (int i = 0; i < this.colors.length; i++) {
			g.setColor(colors[i]);
			g.fillRect(startX, startY + i * cache.legendHeight, cache.legendWidth,
					cache.legendHeight);
			g.setColor(Color.black);
			g.drawRect(startX, startY + i * cache.legendHeight, cache.legendWidth,
					cache.legendHeight);
			g.setFont(new Font("微软雅黑", Font.BOLD, 20));
			if (Math.abs(values[i] - (int) values[i]) > 0.01) {
				g.drawString(String.valueOf(new java.text.DecimalFormat("0.0")
								.format(values[i])) + unit, startX + cache.legendWidth + 5,
						startY + i * cache.legendHeight + 4);
			} else {
				g.drawString(String.valueOf((int) values[i]) + unit, startX
						+ cache.legendWidth + 5, startY + i * cache.legendHeight + 4);
			}
		}
	}

	private void drawLegendTable_h(Graphics2D g, Cache cache) {
		// 横向颜色参照对比表
		int startY = cache.start._2;
		int startX = cache.start._1;

		for (int i = 0; i < this.colors.length; i++) {
			g.setColor(colors[i]);
			g.fillRect(startX + i * cache.legendHeight, startY, cache.legendWidth,
					cache.legendHeight);
			g.setColor(Color.black);
			g.drawRect(startX + i * cache.legendHeight, startY, cache.legendWidth,
					cache.legendHeight);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setFont(new Font("微软雅黑", Font.BOLD, 20));
			if (Math.abs(values[i] - (int) values[i]) > 0.01) {
				g.drawString(String.valueOf(new java.text.DecimalFormat("0.0")
								.format(values[i])) + unit, startX + i * cache.legendWidth - 10,
						startY - 6);
			} else {
				g.drawString(String.valueOf((int) values[i]) + unit, startX + i
						* cache.legendWidth - 10, startY - 6);
			}
		}
	}


	class Cache {
		int legendWidth;
		int legendHeight;
		int rheight;
		int rwidth;
		Tuple2<Integer, Integer> start;
	}

}
