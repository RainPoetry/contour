package rainpoetry.java.draw;

/*
 * User: chenchong
 * Date: 2019/4/16
 * description:
 */

import rainpoetry.java.draw.errors.DrawException;

import java.util.ServiceLoader;

public class DrawBuilder {

	public static Drawable of(String name) {
		ServiceLoader<Drawable> services = ServiceLoader.load(Drawable.class);
		for(Drawable d : services) {
			if (d.name().equals(name))
				return d;
		}
		throw new DrawException("无效的 Drawable 类别 ：" + name);
	}
}
