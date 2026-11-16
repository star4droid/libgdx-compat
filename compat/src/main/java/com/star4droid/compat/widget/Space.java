package com.star4droid.compat.widget;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

/** An empty view used for gaps (often with a weight). */
public class Space extends Widget {
	private final float w, h;

	public Space () {
		this(0, 0);
	}

	public Space (float width, float height) {
		w = width;
		h = height;
		setTouchable(Touchable.disabled);
	}

	@Override
	public float getPrefWidth () {
		return w;
	}

	@Override
	public float getPrefHeight () {
		return h;
	}
}
