package rainpoetry.java.common.config;





import rainpoetry.java.common.config.validate.Validator;
import rainpoetry.java.common.tools.Utils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * User: chenchong
 * Date: 2018/11/16
 * description: This class is used for specifying the set of expected configurations.
 */
public class ConfigDef {

	private static final Pattern COMMA_WITH_WHITESPACE = Pattern.compile("\\s*,\\s*");

	// A unique Java object which represents the lack of a default value.
	private static final Object NO_DEFAULT_VALUE = new Object();

	// store configKeys
	private final Map<String, ConfigKey> configKeys;

	public ConfigDef() {
		configKeys = new LinkedHashMap<>();
	}

	public static enum Type {
		BOOLEAN, STRING, INT, SHORT, LONG, DOUBLE, LIST, CLASS
	}

	public ConfigDef define(String name, Type type) {
		return define(name,type,null);
	}

//	public ConfigDef define(String name, Type type, Object defaultValue) {
//		return define(name,type,defaultValue,null);
//	}

	public ConfigDef define(String name, Type type, Validator validator) {
		return define(name, type, NO_DEFAULT_VALUE, validator);
	}

	public ConfigDef define(String name, Type type, Object defaultValue, Validator validator) {
		return define(new ConfigKey(name, type, defaultValue, validator));
	}

	public ConfigDef define(ConfigKey key) {
		if (configKeys.containsKey(key.name))
			throw new ConfigException("Configuration " + key.name + " is defined twice.");
		configKeys.put(key.name, key);
		return this;
	}

	public Map<String, Object> parse(Map<?, ?> props) {
		// parse all known keys
		Map<String, Object> values = new HashMap<>();
		for (ConfigKey key : configKeys.values())
			values.put(key.name, parseValue(key, props.get(key.name), props.containsKey(key.name)));
		return values;
	}

	Object parseValue(ConfigKey key, Object value, boolean isSet) {
		Object parsedValue;
		if (isSet) {
			parsedValue = parseType(key.name, value, key.type);
			// props map doesn't contain setting, the key is required because no default value specified - its an error
		} else if (NO_DEFAULT_VALUE.equals(key.defaultValue)) {
//			throw new ConfigException("Missing required configuration \"" + key.name + "\" which has no default value.");
			parsedValue = null;
		} else {
			// otherwise assign setting its default value
			parsedValue = key.defaultValue;
		}
		if (key.validator != null) {
			key.validator.ensureValid(key.name, parsedValue);
		}
		return parsedValue;
	}

	private class ConfigKey {
		private final String name;
		private final Type type;
		private final Object defaultValue;
		private final Validator validator;

		public ConfigKey(String name, Type type, Object defaultValue, Validator validator) {
			this.name = name;
			this.type = type;
			this.defaultValue = NO_DEFAULT_VALUE.equals(defaultValue) ? NO_DEFAULT_VALUE : parseType(name, defaultValue, type);
			this.validator = validator;
			if (this.validator != null && hasDefault())
				this.validator.ensureValid(name, this.defaultValue);
		}

		public boolean hasDefault() {
			return !NO_DEFAULT_VALUE.equals(this.defaultValue);
		}
	}

	/**
	 * Parse a value according to its expected type.
	 *
	 * @param name  The config name
	 * @param value The config value
	 * @param type  The expected type
	 * @return The parsed object
	 */
	public static Object parseType(String name, Object value, Type type) {
		try {
			if (value == null) return null;

			String trimmed = null;
			if (value instanceof String)
				trimmed = ((String) value).trim();

			switch (type) {
				case BOOLEAN:
					if (value instanceof String) {
						if (trimmed.equalsIgnoreCase("true"))
							return true;
						else if (trimmed.equalsIgnoreCase("false"))
							return false;
						else
							throw new ConfigException(name, value, "Expected value to be either true or false");
					} else if (value instanceof Boolean)
						return value;
					else
						throw new ConfigException(name, value, "Expected value to be either true or false");
				case STRING:
					if (value instanceof String)
						return trimmed;
					else
						throw new ConfigException(name, value, "Expected value to be a string, but it was a " + value.getClass().getName());
				case INT:
					if (value instanceof Integer) {
						return value;
					} else if (value instanceof String) {
						return Integer.parseInt(trimmed);
					} else {
						throw new ConfigException(name, value, "Expected value to be a 32-bit integer, but it was a " + value.getClass().getName());
					}
				case SHORT:
					if (value instanceof Short) {
						return value;
					} else if (value instanceof String) {
						return Short.parseShort(trimmed);
					} else {
						throw new ConfigException(name, value, "Expected value to be a 16-bit integer (short), but it was a " + value.getClass().getName());
					}
				case LONG:
					if (value instanceof Integer)
						return ((Integer) value).longValue();
					if (value instanceof Long)
						return value;
					else if (value instanceof String)
						return Long.parseLong(trimmed);
					else
						throw new ConfigException(name, value, "Expected value to be a 64-bit integer (long), but it was a " + value.getClass().getName());
				case DOUBLE:
					if (value instanceof Number)
						return ((Number) value).doubleValue();
					else if (value instanceof String)
						return Double.parseDouble(trimmed);
					else
						throw new ConfigException(name, value, "Expected value to be a double, but it was a " + value.getClass().getName());
				case LIST:
					if (value instanceof List)
						return value;
					else if (value instanceof String)
						if (trimmed.isEmpty())
							return Collections.emptyList();
						else
							return Arrays.asList(COMMA_WITH_WHITESPACE.split(trimmed, -1));
					else
						throw new ConfigException(name, value, "Expected a comma separated list.");
				case CLASS:
					if (value instanceof Class)
						return value;
					else if (value instanceof String)
						// need a Non-parametric constructor
						return Class.forName(trimmed, true, Utils.getContextOrClassLoader());
					else
						throw new ConfigException(name, value, "Expected a Class instance or class name.");
				default:
					throw new IllegalStateException("Unknown type.");
			}
		} catch (NumberFormatException e) {
			throw new ConfigException(name, value, "Not a number of type " + type);
		} catch (ClassNotFoundException e) {
			throw new ConfigException(name, value, "Class " + value + " could not be found.");
		}
	}

}
