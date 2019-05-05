package rainpoetry.test.draw;

/*
 * User: chenchong
 * Date: 2019/4/17
 * description:
 */

import org.junit.Before;
import org.junit.Test;
import rainpoetry.java.draw.DrawBuilder;
import rainpoetry.java.draw.bean.DrawStyle;
import rainpoetry.java.draw.bean.Tuple3;
import rainpoetry.java.draw.bean.Tuple5;
import rainpoetry.java.draw.processors.contour.java.JavaContourConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContourTest {

	//  sample: llon，llat,rlon,rlat,region
	protected List<Tuple5<Double, Double, Double, Double, String>> borders;
	//	sample: lon,lat,value
	protected List<Tuple3<Double, Double, Double>> datas;
	// sample: value_min,value_max,r,g,b
	protected List<Tuple5<Double, Double, Integer, Integer, Integer>> colors;
	//	sample: lon,lat,text
	protected List<Tuple3<Double, Double, String>> externs;

	@Before
	public void before() {
		String dataPath = this.getClass().getClassLoader().getResource("contour/data.csv").getPath();
		String colorPath = this.getClass().getClassLoader().getResource("contour/color.csv").getPath();
		String borderPath = this.getClass().getClassLoader().getResource("contour/border.csv").getPath();
		List<Map<String, String>> dataList = CsvParser.parse(dataPath);
		List<Map<String, String>> colorList = CsvParser.parse(colorPath);
		List<Map<String, String>> borderList = CsvParser.parse(borderPath);
		borders = borderToTuple(borderList);
		datas = dataToTuple(dataList);
		colors = colorToTuple(colorList);

		externs = new ArrayList<>();
		for (Tuple3<Double, Double, Double> t : datas) {
			externs.add(new Tuple3<>(t._1, t._2, t._3.toString()));
		}

	}

	@Test
	public void deal() {
		DrawBuilder.of("contour")
				.path("G:/tmp/demo")
				.config("data", datas)
				.config("borders", borders)
				.config("colors", colors)
				.config("style.line.value", new DrawStyle(true, 40, Color.BLACK))
				.extern(externs)
				.ovalStyle(30, Color.RED)
				.textStyle(new Font("微软雅黑",Font.PLAIN, 40), Color.BLACK, 10, 10)
				.build();
	}

	private List<Tuple3<Double, Double, Double>> dataToTuple(List<Map<String, String>> listMaps) {
		List<Tuple3<Double, Double, Double>> retList = new ArrayList<>();
		for (Map<String, String> map : listMaps) {
			Double lon = Double.parseDouble(map.get("LON").trim());
			Double lat = Double.parseDouble(map.get("LAT").trim());
			Double value = Double.parseDouble(map.get("VALUE").trim());
			retList.add(new Tuple3<>(lon, lat, value));
		}
		return retList;
	}

	private List<Tuple5<Double, Double, Double, Double, String>> borderToTuple(List<Map<String, String>> listMaps) {
		List<Tuple5<Double, Double, Double, Double, String>> retList = new ArrayList<>();
		for (Map<String, String> map : listMaps) {
			Double llon = Double.parseDouble(map.get("LLON").trim());
			Double llat = Double.parseDouble(map.get("LLAT").trim());
			Double rlon = Double.parseDouble(map.get("RLON").trim());
			Double rlat = Double.parseDouble(map.get("RLAT").trim());
			String region = map.get("REGION").trim();
			retList.add(new Tuple5(llon, llat, rlon, rlat, region));
		}
		return retList;
	}

	private List<Tuple5<Double, Double, Integer, Integer, Integer>> colorToTuple(List<Map<String, String>> listMaps) {
		List<Tuple5<Double, Double, Integer, Integer, Integer>> retList = new ArrayList<>();
		for (Map<String, String> map : listMaps) {
			Double value_min = Double.parseDouble(map.get("VALUE_MIN").trim());
			Double value_max = Double.parseDouble(map.get("VALUE_MAX").trim());
			int r = Integer.parseInt(map.get("R").trim());
			int g = Integer.parseInt(map.get("G").trim());
			int b = Integer.parseInt(map.get("B").trim());
			retList.add(new Tuple5(value_min, value_max, r, g, b));
		}
		return retList;
	}
}
