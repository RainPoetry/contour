package rainpoetry.java.common.config.validate;



/**
 * User: chenchong
 * Date: 2018/11/16
 * description:Validation logic the user may provide to perform single configuration validation.
 */
public interface Validator{
	/**
	 * Perform single configuration validation.
	 * @param name The name of the configuration
	 * @param value The value of the configuration
	 * @throws
	 */
	void ensureValid(String name, Object value);
}