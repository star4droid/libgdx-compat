package com.star4droid.compat.widget;

import com.star4droid.compat.graphics.Ripple;

/** Something that reacts to taps with a ripple. Implemented by {@link View} and {@link ViewGroup}. */
public interface Clickable {
	Ripple ripple ();

	boolean isEnabled ();

	void performClick ();

	/** @return true if a long press was handled (the normal click is then skipped). */
	boolean performLongClick ();
}
