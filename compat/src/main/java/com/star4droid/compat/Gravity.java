package com.star4droid.compat;

/** Android-like gravity flags. */
public final class Gravity {
	public static final int NONE = 0;
	public static final int LEFT = 1, RIGHT = 2, CENTER_HORIZONTAL = 4;
	public static final int TOP = 8, BOTTOM = 16, CENTER_VERTICAL = 32;
	public static final int START = 64, END = 128;
	public static final int CENTER = CENTER_HORIZONTAL | CENTER_VERTICAL;
	public static final int H_MASK = LEFT | RIGHT | CENTER_HORIZONTAL | START | END;
	public static final int V_MASK = TOP | BOTTOM | CENTER_VERTICAL;

	private Gravity () {
	}

	/** @return 0 = left, 1 = centre, 2 = right (start/end resolved with the layout direction). */
	public static int horizontal (int gravity, boolean rtl) {
		if ((gravity & CENTER_HORIZONTAL) != 0) return 1;
		if ((gravity & LEFT) != 0) return 0;
		if ((gravity & RIGHT) != 0) return 2;
		if ((gravity & END) != 0) return rtl ? 0 : 2;
		return rtl ? 2 : 0;
	}

	/** @return 0 = top, 1 = centre, 2 = bottom. */
	public static int vertical (int gravity) {
		if ((gravity & CENTER_VERTICAL) != 0) return 1;
		if ((gravity & BOTTOM) != 0) return 2;
		return 0;
	}

	/** Parses values such as {@code "center"}, {@code "end|bottom"}, {@code "center_vertical"}. */
	public static int parse (String s) {
		int g = 0;
		String[] parts = s.split("\\|");
		for (int i = 0; i < parts.length; i++) {
			String p = parts[i].trim().toLowerCase();
			if (p.equals("center")) g |= CENTER;
			else if (p.equals("left")) g |= LEFT;
			else if (p.equals("right")) g |= RIGHT;
			else if (p.equals("top")) g |= TOP;
			else if (p.equals("bottom")) g |= BOTTOM;
			else if (p.equals("start")) g |= START;
			else if (p.equals("end")) g |= END;
			else if (p.equals("center_horizontal")) g |= CENTER_HORIZONTAL;
			else if (p.equals("center_vertical")) g |= CENTER_VERTICAL;
		}
		return g;
	}
}
