package rainpoetry.java.draw;

/*
 * User: chenchong
 * Date: 2019/4/16
 * description:
 */

import rainpoetry.java.draw.processors.contour.java.JavaContourConfig;

import java.util.HashMap;
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

	public Drawable append(Map<String,String> maps) {
		cfg.putAll(maps);
		return this;
	}

	public Drawable path(String value) {
		cfg.put(JavaContourConfig.FILE_PATH,value);
		return this;
	}

	public abstract boolean build();
}
