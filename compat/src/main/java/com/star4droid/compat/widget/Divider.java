package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;

/** A thin separator line. Horizontal by default. */
public class Divider extends View {
	private final boolean vertical;
	private Color color = theme().outlineVariant;
	private float thickness = Math.max(1, dp(1));

	public Divider () {
		this(false);
	}

	public Divider (boolean vertical) {
		this.vertical = vertical;
		setTouchable(Touchable.disabled);
	}

	public Divider setLineColor (Color c) {
		color = c;
		return this;
	}

	public Divider setThickness (float px) {
		thickness = px;
		invalidateHierarchy();
		return this;
	}

	@Override
	public float getPrefWidth () {
		return vertical ? thickness : 0;
	}

	@Override
	public float getPrefHeight () {
		return vertical ? 0 : thickness;
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		gfx().rect(batch, getX(), getY(), getWidth(), getHeight(), color, parentAlpha);
	}
}
