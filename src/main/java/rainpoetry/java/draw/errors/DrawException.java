package rainpoetry.java.draw.errors;

/*
 * User: chenchong
 * Date: 2019/4/16
 * description:
 */

public class DrawException extends CommonException{

	public DrawException(String msg) {
		super(msg);
	}

	public DrawException(String prefix, String msg) {
		super(prefix +" :" + msg);
	}

	public DrawException(String msg, Throwable t) {
		super(msg, t);
	}
}
