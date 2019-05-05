package rainpoetry.java.draw;

/*
 * User: chenchong
 * Date: 2019/4/16
 * description:
 */

import rainpoetry.java.draw.bean.ExternData;
import rainpoetry.java.draw.bean.Tuple3;
import rainpoetry.java.draw.processors.contour.java.JavaContourConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Drawable {

	public abstract String name();

	protected Map<String, Object> cfg;

	public Drawable() {
		cfg = new HashMap<>();
	}

	public Drawable config(String key, Object o) {
		cfg.put(key, o);
		return this;
	}

	public Drawable append(Map<String, String> maps) {
		cfg.putAll(maps);
		return this;
	}

	public ExternData extern(List<Tuple3<Double, Double, String>> dataList) {
		ExternData data = new ExternData(dataList, this);
		cfg.put(JavaContourConfig.STYLE_EXTERN_DATA, data);
		return data;
	}

	public Drawable path(String value) {
		cfg.put(JavaContourConfig.FILE_PATH, value);
		return this;
	}

	public abstract boolean build();
}
