package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.star4droid.compat.Compat;

/** Shows one Material icon (see {@link com.star4droid.compat.Icons}). */
public class IconView extends View {
	private char icon;
	private float size = dp(24);
	private Color color = theme().onSurfaceVariant;

	public IconView (char icon) {
		this.icon = icon;
	}

	public IconView (char icon, Color color) {
		this.icon = icon;
		this.color = color;
	}

	public IconView setIcon (char c) {
		icon = c;
		Compat.requestRender();
		return this;
	}

	/** Icon size in pixels (default 24dp). */
	public IconView setIconSize (float px) {
		size = px;
		invalidateHierarchy();
		return this;
	}

	public IconView setIconColor (Color c) {
		color = c;
		return this;
	}

	@Override
	public float getPrefWidth () {
		return size;
	}

	@Override
	public float getPrefHeight () {
		return size;
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		float a = parentAlpha * getColor().a;
		float x = getX() + (getWidth() - size) / 2, y = getY() + (getHeight() - size) / 2;
		Compat.fonts().drawIcon(batch, icon, x, y, size, 1f, color, a);
	}
}
