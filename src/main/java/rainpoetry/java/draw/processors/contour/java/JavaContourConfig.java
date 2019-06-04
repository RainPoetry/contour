package rainpoetry.java.draw.processors.contour.java;

/*
 * User: chenchong
 * Date: 2019/4/16
 * description:
 */

import rainpoetry.java.common.config.AbstractConfig;
import rainpoetry.java.common.config.ConfigDef;
import rainpoetry.java.common.config.validate.NonEmptyString;

import java.util.Map;

import static rainpoetry.java.common.config.ConfigDef.Type.*;

public class JavaContourConfig extends AbstractConfig {

	private static final ConfigDef definition;

	public static final String DATA = "data";
	public static final String BORDERS = "borders";
	public static final String COLORS = "colors";

	// 不会放在 ConfigDef 中管理
	public static final String STYLE_STATION = "style.station";
	public static final String STYLE_GRID_SHOW = "style.grid.show";
	public static final String STYLE_LINE_VALUE = "style.line.value";
	public static final String STYLE_LINE = "style.line";
	public static final String STYLE_CONTOUR_FILL = "style.contour.fill";
	public static final String STYLE_OUTLINE = "style.outline";
	public static final String STYLE_AREA_LINE = "style.area.line";
	public static final String STYLE_EXTERN_DATA = "style.extern.data";


	// 下列参数可以通过 ConfigDef 来获取
	public static final String FILE_PATH = "file.path";
	public static final String LONGITUDE_LEFT = "longitude.left";
	public static final String LONGITUDE_RIGHT = "longitude.right";
	public static final String LATITUDE_LEFT = "latitude.left";
	public static final String LATITUDE_RIGHT = "latitude.right";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String UNIT = "unit";
	public static final String LEGEND = "legend";

	static {
		definition = new ConfigDef()
				.define(FILE_PATH, STRING, new NonEmptyString())
				.define(LONGITUDE_LEFT, DOUBLE)
				.define(LONGITUDE_RIGHT, DOUBLE)
				.define(LATITUDE_LEFT, DOUBLE)
				.define(LATITUDE_RIGHT, DOUBLE)
				.define(WIDTH, INT)
				.define(HEIGHT, INT)
				.define(UNIT, STRING, "", null)
				.define(LEGEND, BOOLEAN, true, null);
	}

	public JavaContourConfig(Map<?, ?> originals) {
		super(definition, originals);
	}
}
