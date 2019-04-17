package rainpoetry.java.common.config;





import rainpoetry.java.common.tools.Utils;

import java.util.Collections;
import java.util.Map;

/**
 * User: chenchong
 * Date: 2018/11/16
 * description:
 */
public class AbstractConfig {

	/* the original values passed in by the user */
	private final Map<String, ?> originals;

	/* the parsed values */
	private final Map<String, Object> values;

	private final ConfigDef definition;

	public AbstractConfig(ConfigDef definition, Map<?, ?> originals) {
		/* check that all the keys are really strings */
		for (Map.Entry<?, ?> entry : originals.entrySet())
			if (!(entry.getKey() instanceof String))
				throw new ConfigException(entry.getKey().toString(), entry.getValue(), "Key must be a string.");
		this.originals = (Map<String, ?>) originals;
		this.values = definition.parse(this.originals);
		this.definition = definition;
	}

	public Object get(String key) {
		if (!values.containsKey(key))
			throw new ConfigException(String.format("Unknown configuration '%s'", key));
		return values.get(key);
	}

	public boolean contains(String... keys) {
		boolean flag = true;
		for(String key : keys)
			flag &= values.containsKey(key) && values.get(key)!=null;
		return flag;
	}


	public<T> T getOrElse(String key, T t) {
		T val;
		return (val = (T) values.get(key)) == null ? t : val;
	}

	public Boolean getBoolean(String key) {
		return (Boolean) get(key);
	}
	public String getString(String key) {
		return (String) get(key);
	}
	public Double getDouble(String key) {
		return (Double) get(key);
	}
	public Integer getInt(String key) {
		return (Integer) get(key);
	}
	public Class<?> getClass(String key) {
		return (Class<?>) get(key);
	}

	public <T> T getConfiguredInstance(String key, Class<T> t) {
		Class<?> c = getClass(key);
		if (c == null)
			return null;
		Object o = Utils.newInstance(c);
		if (!t.isInstance(o))
			throw new ConfigException(c.getName() + " is not an instance of " + t.getName());
		return t.cast(o);
	}

	public Map<String,Object> toMap() {
		return Collections.unmodifiableMap(values);
	}
}


