package com.star4droid.compat.widget;


/** A surface for grouped content: elevated (default), filled or outlined. It is a {@link FrameLayout}; put a LinearLayout
 * inside for more than one child. */
public class CardView extends FrameLayout {
	public static final int ELEVATED = 0, FILLED = 1, OUTLINED = 2;
	private int style = -1;

	public CardView () {
		this(ELEVATED);
	}

	public CardView (int style) {
		setCornerRadius(dp(theme().cornerMedium));
		setStyle(style);
	}

	public void setStyle (int s) {
		style = s;
		strokeColor = null;
		strokeWidth = 0;
		elevation = 0;
		if (s == ELEVATED) {
			background = theme().surfaceContainerLow;
			elevation = dp(1.5f);
		} else if (s == FILLED) {
			background = theme().surfaceContainerHighest;
		} else {
			background = theme().surface;
			strokeColor = theme().outlineVariant;
			strokeWidth = dp(1);
		}
	}

	public int getStyle () {
		return style;
	}
}
