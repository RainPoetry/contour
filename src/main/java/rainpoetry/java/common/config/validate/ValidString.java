package rainpoetry.java.common.config.validate;




import rainpoetry.java.common.config.ConfigException;
import rainpoetry.java.common.tools.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * User: chenchong
 * Date: 2018/11/16
 * description:
 */
public class ValidString implements Validator {
	final List<String> validStrings;

	private ValidString(List<String> validStrings) {
		this.validStrings = validStrings;
	}

	public static ValidString in(String... validStrings) {
		return new ValidString(Arrays.asList(validStrings));
	}

	@Override
	public void ensureValid(String name, Object o) {
		String s = (String) o;
		if (!validStrings.contains(s)) {
			throw new ConfigException(name, o, "String must be one of: " + Utils.join(validStrings, ", "));
		}

	}

	public String toString() {
		return "[" + Utils.join(validStrings, ", ") + "]";
	}
}
