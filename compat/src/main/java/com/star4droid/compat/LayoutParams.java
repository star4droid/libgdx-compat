package com.star4droid.compat;

/** Per-child layout options, like Android's LayoutParams. All setters return {@code this} for chaining. */
public class LayoutParams {
	public static final float MATCH_PARENT = -1f, WRAP_CONTENT = -2f;

	public float width = WRAP_CONTENT, height = WRAP_CONTENT;
	public float weight;
	public int gravity = Gravity.NONE;
	/** Margins in pixels. In RTL layouts left acts as start and right as end. */
	public float marginLeft, marginTop, marginRight, marginBottom;

	public LayoutParams () {
	}

	public LayoutParams (float width, float height) {
		this.width = width;
		this.height = height;
	}

	public LayoutParams width (float px) {
		width = px;
		return this;
	}

	public LayoutParams height (float px) {
		height = px;
		return this;
	}

	public LayoutParams size (float w, float h) {
		width = w;
		height = h;
		return this;
	}

	/** width = height = match_parent. */
	public LayoutParams match () {
		width = height = MATCH_PARENT;
		return this;
	}

	public LayoutParams matchWidth () {
		width = MATCH_PARENT;
		return this;
	}

	public LayoutParams matchHeight () {
		height = MATCH_PARENT;
		return this;
	}

	public LayoutParams wrap () {
		width = height = WRAP_CONTENT;
		return this;
	}

	public LayoutParams weight (float w) {
		weight = w;
		return this;
	}

	public LayoutParams gravity (int g) {
		gravity = g;
		return this;
	}

	public LayoutParams margin (float all) {
		marginLeft = marginTop = marginRight = marginBottom = all;
		return this;
	}

	public LayoutParams margin (float horizontal, float vertical) {
		marginLeft = marginRight = horizontal;
		marginTop = marginBottom = vertical;
		return this;
	}

	public LayoutParams margins (float left, float top, float right, float bottom) {
		marginLeft = left;
		marginTop = top;
		marginRight = right;
		marginBottom = bottom;
		return this;
	}

	public LayoutParams marginTop (float v) {
		marginTop = v;
		return this;
	}

	public LayoutParams marginBottom (float v) {
		marginBottom = v;
		return this;
	}

	public LayoutParams marginLeft (float v) {
		marginLeft = v;
		return this;
	}

	public LayoutParams marginRight (float v) {
		marginRight = v;
		return this;
	}
}
