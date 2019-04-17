package rainpoetry.java.common.config;


import rainpoetry.java.draw.errors.CommonException;

/**
 * User: chenchong
 * Date: 2018/11/16
 * description:
 */
public class ConfigException extends CommonException {
	private static final long serialVersionUID = 1L;

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(String name, Object value) {
		this(name, value, null);
	}

	public ConfigException(String name, Object value, String message) {
		super("Invalid value " + value + " for configuration " + name + (message == null ? "" : ": " + message));
	}
}
