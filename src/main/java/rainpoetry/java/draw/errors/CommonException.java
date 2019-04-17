package rainpoetry.java.draw.errors;

/*
 * User: chenchong
 * Date: 2019/4/16
 * description:
 */

public class CommonException extends RuntimeException{

	private final static long serialVersionUID = 1L;

	public CommonException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommonException(String message) {
		super(message);
	}

	public CommonException(Throwable cause) {
		super(cause);
	}

	public CommonException() {
		super();
	}
}