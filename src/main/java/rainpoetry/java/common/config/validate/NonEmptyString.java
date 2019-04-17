package rainpoetry.java.common.config.validate;


import rainpoetry.java.common.config.ConfigException;

/**
 * User: chenchong
 * Date: 2018/11/16
 * description:
 */
public class NonEmptyString implements Validator {
	@Override
	public void ensureValid(String name, Object o) {
		String s = (String) o;
		if (s != null && s.isEmpty()) {
			throw new ConfigException(name, o, "String must be non-empty");
		}
	}

	@Override
	public String toString() {
		return "non-empty string";
	}
}
