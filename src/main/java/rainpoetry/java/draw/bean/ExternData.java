package rainpoetry.java.draw.bean;

/*
 * User: chenchong
 * Date: 2019/4/29
 * description:
 */

import rainpoetry.java.draw.Drawable;

import java.awt.*;
import java.util.List;

public class ExternData {
	private static final DrawStyle default_fill_oval = new DrawStyle(false, 10, Color.RED);
	private static final DrawStyle default_text_style = new DrawStyle(false, 10, Color.BLACK);
	private static final int default_left = 10;
	private static final int default_top = 10;

	public DrawStyle fill_oval;
	public DrawStyle text_style;
	public int left = 10;
	public int top = 10;
	public Font font;

	public final List<Tuple3<Double, Double, String>> dataList;
	private Drawable d;

	public ExternData(List<Tuple3<Double, Double, String>> dataList, Drawable d) {
		this.dataList = dataList;
		fill_oval = default_fill_oval;
		text_style = default_text_style;
		left = default_left;
		top = default_top;
		this.d = d;
	}

	public ExternData ovalStyle(int size, Color c) {
		fill_oval = new DrawStyle(true, size, c);
		return this;
	}


	public ExternData textStyle(Font font, Color c, int left, int top) {
		this.left = left;
		this.top = top;
		text_style = new DrawStyle(true, font.getSize(), c);
		this.font = font;
		return this;
	}

	public ExternData showOval() {
		fill_oval.show = true;
		return this;
	}

	public ExternData showText() {
		text_style.show = true;
		return this;
	}

	public boolean build() {
		return d.build();
	}

}
