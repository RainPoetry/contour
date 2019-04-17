package rainpoetry.java.common.config.validate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: chenchong
 * Date: 2018/11/16
 * description: combined validation
 */
public class CompositeValidator implements Validator {

	private final List<Validator> validators;

	public CompositeValidator(List<Validator> validators) {
		this.validators = Collections.unmodifiableList(validators);
	}

	public static CompositeValidator of(Validator... validators) {
		return new CompositeValidator(Arrays.asList(validators));
	}

	@Override
	public void ensureValid(String name, Object value) {
		for (Validator validator: validators) {
			validator.ensureValid(name, value);
		}
	}

	@Override
	public String toString() {
		if (validators == null) return "";
		StringBuilder desc = new StringBuilder();
		for (Validator v: validators) {
			if (desc.length() > 0) {
				desc.append(',').append(' ');
			}
			desc.append(String.valueOf(v));
		}
		return desc.toString();
	}
}
