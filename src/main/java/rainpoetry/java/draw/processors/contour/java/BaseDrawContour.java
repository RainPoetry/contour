package rainpoetry.java.draw.processors.contour.java;

/*
 * User: chenchong
 * Date: 2019/4/16
 * description:
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rainpoetry.java.draw.bean.DrawStyle;
import rainpoetry.java.draw.bean.ExternData;
import rainpoetry.java.draw.bean.Tuple3;
import wContour.Contour;
import wContour.Global.Border;
import wContour.Global.PointD;
import wContour.Global.PolyLine;
import wContour.Global.Polygon;
import wContour.Interpolate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static rainpoetry.java.draw.processors.contour.java.JavaContourConfig.*;

public abstract class BaseDrawContour extends BaseContourIdentify {

	private Logger logger = LoggerFactory.getLogger(BaseDrawContour.class);

	private static final int DEFAULT_ALGORITHM_ROWS = 200;
	private static final int DEFAULT_ALGORITHM_COLS = 200;
	private static final double DEFAULT_ALGORITHM_UNDEFINE = -9999.0;
	private static final String TMP_FILE_SUFFIX = "_tmp";

	// 经纬度以 “点 ” 的形式展示控制
	private DrawStyle station_style = new DrawStyle(false, 10, Color.RED);
	// 表格点显示控制
	private boolean grid_show = false;
	//  等值线值 显示控制、大小控制
	private DrawStyle line_value_style = new DrawStyle(true, 40, Color.BLACK);
	// 等值线是否绘制 、样式控制
	private DrawStyle line_style = new DrawStyle(true, 5, Color.ORANGE);
	// 是否填充等值线
	private boolean fillContour = true;
	// 图片轮廓显示控制
	private DrawStyle outline_style = new DrawStyle(false, 10, Color.RED);
	// 区域线控制
	private DrawStyle area_line_style = new DrawStyle(true, 1, Color.gray);
	// 展示其他的经纬度数据
	private ExternData externData;

	private List<Polygon> contourPolygons;

	private double[][] discreteData;                         // 统计数据
	private double[] colorValue;                             // 色标值
	private Color[] colorArray;                              // 色标
	private double left, right, top, bottom, scaleX = 1.0, scaleY = 1.0;
	private int width, height;

	List<List<PointD>> outLine;
	LinkedHashMap<Double, Color> colorMap;

	public boolean draw() {
		try {
			before();
			// 等值线图风格设置
			styleChange();
			logger.info("calculating .....");
			// 算法计算
			algorithm();
			String filePath = contourConf.getString(JavaContourConfig.FILE_PATH);
			String tmpPath = filePath + TMP_FILE_SUFFIX;
			logger.info("paint basic picture .....");
			// 绘制底图
			drawBasic(tmpPath);
			logger.info("paint contour picture .....");
			// 使用底图来裁剪等值线图
			drawContour(filePath, tmpPath);
			logger.info("paint legend picture .....");
			LegendBuilder.newInstance().colors(colorArray)
					.values(colorValue)
					.unit(contourConf.getString(JavaContourConfig.UNIT))
					.path(filePath + "_r")
					.legend(contourConf.getBoolean(JavaContourConfig.LEGEND))
					.create();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// 绘制底图
	public void drawBasic(String basicFile) throws IOException {
		BufferedImage base = transparencyImage(Transparency.BITMASK);
		Graphics2D g_base = base.createGraphics();
		// 填充边界线
		if (outLine != null && outLine.size() > 0)
			borderPolygon(g_base, outLine, Color.WHITE);
		OutputStream tmpStream = new FileOutputStream(new File(basicFile + ".png"));
		ImageIO.write(base, "png", tmpStream);
		tmpStream.close();
		g_base.dispose();
		base.flush();
	}

	// 绘制等值线图
	public void drawContour(String realPath, String tmpPath) throws IOException {
		BufferedImage image = transparencyImage(Transparency.TRANSLUCENT);
		Graphics2D g2 = image.createGraphics();
		// 抗锯齿处理
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		AlphaComposite ac = AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER);
		g2.setComposite(ac);

		// 绘制等值面以及等值线
		if ((fillContour || line_style.show) && contourPolygons.size() > 0)
			drawPolygon(g2, contourPolygons);
		// 绘制轮廓线
		if (outline_style.show)
			drawBorder(g2, outLine, outline_style.color, outline_style.size);
		// 绘制经纬度数据
		if (station_style.show)
			drawStation(g2);
		// 绘制扩充数据
		if (externData != null)
			drawExternData(g2);
		// 绘制边界线
		if (area_line_style.show)
			drawBorder(g2, outLine, area_line_style.color, area_line_style.size);

		// 重新打开等值面区域图像
		File file = new File(tmpPath + ".png");
		// 图片装入内存
		BufferedImage src = ImageIO.read(file);
		ac = AlphaComposite.getInstance(AlphaComposite.DST_IN);
		g2.setComposite(ac);
		g2.drawImage(src, 0, 0, width, height, null);
		src.flush();
		src = null;
		// 删除临时文件
		file.delete();
		ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
		g2.setComposite(ac);
		// 释放对象
		g2.dispose();
		// 保存文件
		OutputStream out = new FileOutputStream(new File(realPath
				+ ".png"));
		ImageIO.write(image, "png", out);
		out.close();
		image.flush();
		logger.info("图片路径： " + realPath + ".png");
	}

	public void drawExternData(Graphics2D g) {
		if (externData.dataList.size() > 0) {
			for (Tuple3<Double, Double, String> t : externData.dataList) {
				int[] sxy = ToScreen(t._1, t._2);
				if (externData.fill_oval.show) {
					g.setColor(externData.fill_oval.color);
					g.fillOval(sxy[0], sxy[1], externData.fill_oval.size, externData.fill_oval.size);
				}
				if (externData.text_style.show) {
					g.setColor(externData.text_style.color);
					g.setFont(externData.font);
					g.drawString(t._3, externData.left + sxy[0], externData.top + sxy[1]);
				}
			}
		}
	}

	public void drawStation(Graphics2D g) {
		if (discreteData != null) {
			for (int i = 0; i < discreteData[0].length; i++) {
				int[] sxy = ToScreen(discreteData[0][i], discreteData[1][i]);
				g.setColor(station_style.color);
				g.fillOval(sxy[0], sxy[1], station_style.size, station_style.size);
			}
		}
	}

	public void drawPolygon(Graphics2D g, List<Polygon> polygons) {
		Color lineColor = line_style.show ? line_style.color : null;
		int lineSize = line_style.size;
		System.out.println(colorMap);
		for (Polygon polygon : polygons) {
			Color fillColor = fillContour ? colorMap.get(polygon.LowValue) : null;
			if (!polygon.IsHighCenter) {
				Color tmp = colorArray[0];
				for (Color c : colorArray) {
					if (c == fillColor) {
						fillColor = tmp;
						break;
					} else {
						tmp = c;
					}
				}
			}
			PolyLine line = polygon.OutLine;
			String msg = line_value_style.show ? line.Value + "" : null;
			polygonLine(g, line.PointList, fillColor, lineColor, lineSize, line_value_style.size, msg);
		}
	}

	// 绘制等直线
	private void drawBorder(Graphics2D g, List<List<PointD>> outLine,
							Color lineColor, int lineSize) {
		for (List<PointD> polyLine : outLine)
			polygonLine(g, polyLine, null, lineColor, lineSize, 0, null);
	}

	// 填充等值面 & 绘制等值线
	private void borderPolygon(Graphics2D g, List<List<PointD>> outLine,
							   Color fillColor) {
		for (List<PointD> polyLine : outLine)
			polygonLine(g, polyLine, fillColor, Color.BLACK, 5, 0, null);
	}

	// 填充等值面 & 绘制等值线 方法的接口
	private void polygonLine(Graphics2D g, List<PointD> outLine,
							 Color fillColor, Color lineColor, int lineSize,
							 int fontSize, String msg) {
		PointD point;
		int len = outLine.size();
		int[] xPoints = new int[len];
		int[] yPoints = new int[len];
		for (int j = 0; j < len; j++) {
			point = outLine.get(j);
			int[] sxy = ToScreen(point.X, point.Y);
			xPoints[j] = sxy[0];
			yPoints[j] = sxy[1];
		}
		java.awt.Polygon polygon = new java.awt.Polygon(xPoints, yPoints, len);
		// 填充等值面
		if (fillColor != null) {
			g.setColor(fillColor);
			g.fillPolygon(polygon);
		}
		// 绘制等值线
		if (lineColor != null) {
			BasicStroke bs = new BasicStroke(lineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			g.setStroke(bs);
			g.setColor(lineColor);
			g.drawPolygon(polygon);
			if (msg != null) {
				g.setColor(Color.black);
				g.setFont(new Font("微软雅黑", Font.BOLD, fontSize));
				g.drawString(msg, xPoints[0], yPoints[0]);
				g.drawString(msg, xPoints[len / 2], yPoints[len / 2]);
			}
		}
	}

	private int[] ToScreen(double pX, double pY) {
		int sX = (int) ((pX - left) * scaleX);
		int sY = (int) ((top - pY) * scaleY);
		int[] sxy = {sX, sY};
		return sxy;
	}

	// 设置生成图片的透明模式
	// 参数 transparency  ---  对应的值：(Transparency.BITMASK/Transparency.OPAQUE/Transparency.TRANSLUCENT)
	// 		Transparency 接口用于控制透明模式
	//			 Transparency.BITMASK 		--  完全透明或者完全不透明
	//		 	 Transparency.OPAQUE	 	--  不透明
	//			 Transparency.TRANSLUCENT	--	透明度在 0-1 之间
	private BufferedImage transparencyImage(int transparency) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();
		// 设置画布透明模式
		bi = g2d.getDeviceConfiguration().createCompatibleImage(width, height, transparency);
		// 释放资源
		// graphics2D 使用的是系统资源，不释放的话，其他 graphics2D 的实例对象无法获取系统资源
		g2d.dispose();
		return bi;
	}

	// 算法计算
	private void algorithm() {
		double[] x = new double[DEFAULT_ALGORITHM_ROWS];
		double[] y = new double[DEFAULT_ALGORITHM_COLS];
		int neighborNumber = colorValue.length - 1;

		// 填充数据
		Interpolate.CreateGridXY_Num(left, bottom, right, top, x, y);
		double[][] gridData = Interpolate.Interpolation_IDW_Neighbor(
				discreteData, x, y, neighborNumber, DEFAULT_ALGORITHM_UNDEFINE);

		int nc = colorValue.length;
		int[][] S1 = new int[gridData.length][gridData[0].length];

		// 训练等值线
		List<Border> borders = Contour.tracingBorders(gridData, x, y, S1, DEFAULT_ALGORITHM_UNDEFINE);
		List<PolyLine> contourLines = Contour.tracingContourLines(gridData, x, y, nc,
				colorValue, DEFAULT_ALGORITHM_UNDEFINE, borders, S1);

		// 平滑处理
		contourLines = Contour.smoothLines(contourLines);
		contourLines = Contour.smoothLines(contourLines);

		// 训练等值面
		contourPolygons = Contour.tracingPolygons(gridData, contourLines,
				borders, colorValue);
		Collections.sort(contourPolygons, new Comparator<Polygon>() {
			@Override
			public int compare(Polygon o1, Polygon o2) {
				return Double.compare(o1.LowValue, o2.LowValue);
			}
		});
	}

	private void styleChange() {
		for (Map.Entry<String, Object> entry : styles.entrySet()) {
			styleDeal(entry.getKey(), entry.getValue());
		}
	}

	private void styleDeal(String key, Object value) {
		switch (key) {
			case STYLE_STATION:
				station_style = (DrawStyle) value;
				break;
			case STYLE_GRID_SHOW:
				grid_show = (boolean) value;
				break;
			case STYLE_LINE_VALUE:
				line_value_style = (DrawStyle) value;
				break;
			case STYLE_LINE:
				line_style = (DrawStyle) value;
				break;
			case STYLE_CONTOUR_FILL:
				fillContour = (boolean) value;
				break;
			case STYLE_OUTLINE:
				outline_style = (DrawStyle) value;
				break;
			case STYLE_AREA_LINE:
				area_line_style = (DrawStyle) value;
				break;
			case STYLE_EXTERN_DATA:
				externData = (ExternData) value;
				break;
		}
	}

	public void before() {
		colorMap = new LinkedHashMap();
		for (int i = 0; i < colorValue.length-1; i++) {
			colorMap.put(colorValue[i], colorArray[i]);
		}
	}

	public void discreteData(double[][] discreteData) {
		this.discreteData = discreteData;
	}

	public void outLine(List<List<PointD>> outLine) {
		this.outLine = outLine;
	}

	public void colorArray(Color[] colorArray) {
		this.colorArray = colorArray;
	}

	public void colorValue(double[] colorValue) {
		this.colorValue = colorValue;
	}

	public void model(int width, int height, double minX, double maxX, double minY, double maxY) {
		this.width = width;
		this.height = height;
		left = minX;
		right = maxX;
		bottom = minY;
		top = maxY;
		scaleX = width / (right - left);
		scaleY = height / (top - bottom);
	}
}
