package rainpoetry.java.draw.processors.contour.java;

/*
 * User: chenchong
 * Date: 2019/4/16
 * description:
 */

import rainpoetry.java.draw.bean.Tuple3;
import rainpoetry.java.draw.bean.Tuple5;
import rainpoetry.java.draw.errors.DrawException;
import wContour.Global.PointD;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BaseDrawContourHandler extends BaseDrawContour {


	@Override
	public boolean execute() {
		prepare();
		return draw();
	}

	private void prepare() {
		dataDeal();
		borderDeal();
		colorDeal();
	}

	// 等值线数据处理
	private void dataDeal() {
		if (datas.size() == 0) {
			throw new DrawException("处理数据为空!!!!");
		}
		double[][] discreteData = new double[3][datas.size()];
		double maxV = Double.MIN_VALUE, minV = Double.MAX_VALUE;
		for (int i = 0; i < datas.size(); i++) {
			Tuple3<Double, Double, Double> data = datas.get(i);
			discreteData[0][i] = data._1;
			discreteData[1][i] = data._2;
			discreteData[2][i] = data._3;
			maxV = Math.max(data._3, maxV);
			minV = Math.min(data._3, minV);
		}
		discreteData(discreteData);
//		dc.setMaxValue(maxV);
//		dc.setMinValue(minV);
//		minValue = minV;
//		maxValue = maxV;
	}

	// 处理色标数据
	private void colorDeal() {
		Color[] colorArray = new Color[colors.size()];
		double[] valueArray = new double[colors.size()];
		int count = 0;
		for (Tuple5<Double, Double, Integer, Integer, Integer> color : colors) {
			double value_min = color._1;
			double value_max = color._2;
			valueArray[count] = value_max;
			colorArray[count] = new Color(color._3, color._4, color._5);
			count++;
		}
		colorArray(colorArray);
		colorValue(valueArray);
	}

	// 处理边界数据
	private void borderDeal() {
		double left = contourConf.getOrElse(JavaContourConfig.LONGITUDE_LEFT, Double.MAX_VALUE);
		double right = contourConf.getOrElse(JavaContourConfig.LONGITUDE_RIGHT, Double.MIN_VALUE);
		double top = contourConf.getOrElse(JavaContourConfig.LATITUDE_RIGHT, Double.MIN_VALUE);
		double bottom = contourConf.getOrElse(JavaContourConfig.LATITUDE_LEFT, Double.MAX_VALUE);
		List<List<PointD>> clipLines = new ArrayList<List<PointD>>();
		for (Tuple5<Double, Double, Double, Double, String> border : borders) {
			double llon = border._1;
			double llat = border._2;
			double rlon = border._3;
			double rlat = border._4;
			String region = border._5;
			// 边界数据可能由多根线组成，不同的线采用  ";" 隔开
			String[] borderLines = region.split(";");
			for (String line : borderLines) {
				// 每一条边界线由多个坐标点构成，不同的点以 "," 隔开
				String[] spots = line.split(",");
				List<PointD> spotPoints = new ArrayList<>();
				for (String s : spots) {
					PointD aPoint = new PointD();
					String horizontal = s.split("\\s+")[0];
					String vertical = s.split("\\s+")[1];
					aPoint.X = Double.valueOf(horizontal);
					aPoint.Y = Double.valueOf(vertical);
					spotPoints.add(aPoint);
				}
				clipLines.add(spotPoints);
			}
			left = Math.min(llon, left);
			right = Math.max(rlon, right);
			top = Math.max(rlat, top);
			bottom = Math.min(llat, bottom);
		}
		int width, height;
		if (contourConf.contains(JavaContourConfig.WIDTH, JavaContourConfig.HEIGHT)) {
			width = contourConf.getInt(JavaContourConfig.WIDTH);
			height = contourConf.getInt(JavaContourConfig.HEIGHT);
		} else {
			width = (int) ((right - left) * 1000);
			height = (int) ((top - bottom) * 1000);
			if (width > 3000) {
				double ratio = (double) (width + 0.1) / 3000;
				// System.out.println("ratio:" + ratio);
				width = (int) (width / ratio);
				height = (int) (height / ratio);
			}
			if (height > 3000) {
				double ratio = (double) (height + 0.1) / 3000;
				width = (int) (width / ratio);
				height = (int) (height / ratio);
			}
		}
		outLine(clipLines);
		model(width, height, left, right, bottom, top);
	}


}
