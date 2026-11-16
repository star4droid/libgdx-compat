package com.star4droid.compat.widget;

/** Implemented by views whose height depends on the width they are given (wrapped text, layouts containing it). */
public interface Measurable {
	/** @return the preferred height when laid out with the given width. */
	float measureHeight (float width);
}
