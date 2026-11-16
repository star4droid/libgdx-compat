package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Gfx;

/** A round button showing one icon. */
public class IconButton extends View {
	public static final int STANDARD = 0, FILLED = 1, TONAL = 2, OUTLINED = 3;

	private char icon;
	private int style = STANDARD;
	private Color iconColor, bgColor;
	private float iconSize = dp(24), size = dp(40), touchSize = dp(48);

	public IconButton (char icon) {
		this.icon = icon;
		ensurePressListener();
	}

	public IconButton (char icon, Runnable onClick) {
		this(icon);
		setOnClick(onClick);
	}

	public IconButton style (int s) {
		style = s;
		return this;
	}

	public IconButton icon (char c) {
		icon = c;
		Compat.requestRender();
		return this;
	}

	public char getIcon () {
		return icon;
	}

	public IconButton onClick (Runnable r) {
		setOnClick(r);
		return this;
	}

	public IconButton colors (Color background, Color iconColor) {
		this.bgColor = background;
		this.iconColor = iconColor;
		return this;
	}

	public IconButton iconColor (Color c) {
		iconColor = c;
		return this;
	}

	/** @param container diameter of the round container, @param touch preferred size of the whole view */
	public IconButton sizes (float iconPx, float container, float touch) {
		iconSize = iconPx;
		size = container;
		touchSize = touch;
		invalidateHierarchy();
		return this;
	}

	@Override
	public float getPrefWidth () {
		return touchSize;
	}

	@Override
	public float getPrefHeight () {
		return touchSize;
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		Theme t = theme();
		Gfx g = gfx();
		float a = parentAlpha * getColor().a;
		float d = Math.min(size, Math.min(getWidth(), getHeight()));
		float x = getX() + (getWidth() - d) / 2, y = getY() + (getHeight() - d) / 2;
		Color bg = bgColor != null ? bgColor : style == FILLED ? t.primary : style == TONAL ? t.secondaryContainer : null;
		Color fg = iconColor != null ? iconColor
			: style == FILLED ? t.onPrimary : style == TONAL ? t.onSecondaryContainer : t.onSurfaceVariant;
		float fa = isEnabled() ? a : a * 0.38f;
		if (bg != null) g.roundRect(batch, x, y, d, d, d / 2, bg, isEnabled() ? a : a * 0.3f);
		if (style == OUTLINED) g.stroke(batch, x, y, d, d, d / 2, dp(1), t.outline, a);
		ripple.draw(batch, g, x, y, d, d, d / 2, fg, a);
		Compat.fonts().drawIcon(batch, icon, getX() + (getWidth() - iconSize) / 2, getY() + (getHeight() - iconSize) / 2,
			iconSize, 1f, fg, fa);
	}
}
