package rainpoetry.java.common.config.validate;


import rainpoetry.java.common.config.ConfigException;

/**
 * User: chenchong
 * Date: 2018/11/16
 * description: Validation logic for numeric ranges
 */
public class Range implements Validator{
	private final Number min;
	private final Number max;

	/**
	 *  A numeric range with inclusive upper bound and inclusive lower bound
	 * @param min  the lower bound
	 * @param max  the upper bound
	 */
	private Range(Number min, Number max) {
		this.min = min;
		this.max = max;
	}

	/**
	 * A numeric range that checks only the lower bound
	 *
	 * @param min The minimum acceptable value
	 */
	public static Range atLeast(Number min) {
		return new Range(min, null);
	}

	/**
	 * A numeric range that checks both the upper (inclusive) and lower bound
	 */
	public static Range between(Number min, Number max) {
		return new Range(min, max);
	}

	public void ensureValid(String name, Object o) {
		if (o == null)
			throw new ConfigException(name, null, "Value must be non-null");
		Number n = (Number) o;
		if (min != null && n.doubleValue() < min.doubleValue())
			throw new ConfigException(name, o, "Value must be at least " + min);
		if (max != null && n.doubleValue() > max.doubleValue())
			throw new ConfigException(name, o, "Value must be no more than " + max);
	}

	public String toString() {
		if (min == null && max == null)
			return "[...]";
		else if (min == null)
			return "[...," + max + "]";
		else if (max == null)
			return "[" + min + ",...]";
		else
			return "[" + min + ",...," + max + "]";
	}
}
