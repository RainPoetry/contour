package rainpoetry.java.draw.processors.contour.java;

/*
 * User: chenchong
 * Date: 2019/4/16
 * description:
 */

import rainpoetry.java.draw.Drawable;
import rainpoetry.java.draw.bean.Tuple3;
import rainpoetry.java.draw.bean.Tuple5;
import rainpoetry.java.draw.errors.DrawException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseContourIdentify extends Drawable {

	public static final String NAME = "contour";

	protected JavaContourConfig contourConf;
	protected Map<String,Object> styles;

	//  sample: llon，llat,rlon,rlat,region
	protected List<Tuple5<Double, Double, Double, Double, String>> borders;
	//	sample: lon,lat,value
	protected List<Tuple3<Double, Double, Double>> datas;
	// sample: value_min,value_max,r,g,b
	protected List<Tuple5<Double, Double, Integer, Integer, Integer>> colors;

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public boolean build() {
		valid();
		style();
		try {
			borders = (List<Tuple5<Double, Double, Double, Double, String>>) cfg.get(JavaContourConfig.BORDERS);
			datas = (List<Tuple3<Double, Double, Double>>) cfg.get(JavaContourConfig.DATA);
			colors = (List<Tuple5<Double, Double, Integer, Integer, Integer>>) cfg.get(JavaContourConfig.COLORS);
		} catch (Exception e) {
			new DrawException("数据强转异常", e);
		}
		this.contourConf = new JavaContourConfig(cfg);
		return execute();
	}

	public abstract boolean execute();

	public void valid() {
		if (!cfg.containsKey(JavaContourConfig.FILE_PATH)) {
			throw new DrawException("文件路径没有匹配", JavaContourConfig.FILE_PATH + "is unknown");
		}
		if (!cfg.containsKey(JavaContourConfig.DATA)) {
			throw new DrawException("没有处理数据", JavaContourConfig.DATA + "is unknown");
		}
		if (!cfg.containsKey(JavaContourConfig.BORDERS)) {
			throw new DrawException("没有边界数据", JavaContourConfig.BORDERS + "is unknown");
		}
		if (!cfg.containsKey(JavaContourConfig.COLORS)) {
			throw new DrawException("没有色标数据", JavaContourConfig.COLORS + "is unknown");
		}
	}

	public void style() {
		styles = new HashMap<>();
		for(Map.Entry<String,Object> entry : cfg.entrySet()) {
			if (entry.getKey().startsWith("style."))
				styles.put(entry.getKey(),entry.getValue());
		}
	}

}
