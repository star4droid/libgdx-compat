package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.Gfx;
import com.star4droid.compat.graphics.TextLayout;

/** Material button: filled, tonal, outlined, text or elevated, with an optional leading icon. */
public class Button extends View {
	public static final int FILLED = 0, TONAL = 1, OUTLINED = 2, TEXT = 3, ELEVATED = 4;

	private final TextLayout label = new TextLayout(Typography.sizePx(Typography.LABEL_LARGE), true);
	private int style = FILLED;
	private char icon;
	private Color bgColor, fgColor;
	private float radius = -1, minHeight = dp(40), padH = -1;

	public Button (CharSequence text) {
		this(text, FILLED);
	}

	public Button (CharSequence text, int style) {
		this.style = style;
		label.setText(text);
		label.setMaxLines(1);
		ensurePressListener();
	}

	// ---- chaining setters
	public Button style (int s) {
		style = s;
		invalidateHierarchy();
		return this;
	}

	public Button icon (char c) {
		icon = c;
		invalidateHierarchy();
		return this;
	}

	public Button onClick (Runnable r) {
		setOnClick(r);
		return this;
	}

	public Button text (CharSequence t) {
		label.setText(t);
		invalidateHierarchy();
		Compat.requestRender();
		return this;
	}

	public CharSequence getText () {
		return label.getText();
	}

	/** Overrides the container and content colours (null = from the style). */
	public Button colors (Color background, Color content) {
		bgColor = background;
		fgColor = content;
		return this;
	}

	/** Corner radius in pixels; negative = fully rounded (default). */
	public Button cornerRadius (float px) {
		radius = px;
		return this;
	}

	public Button textSize (float px) {
		label.setSize(px);
		invalidateHierarchy();
		return this;
	}

	public Button height (float px) {
		minHeight = px;
		invalidateHierarchy();
		return this;
	}

	public Button paddingHorizontal (float px) {
		padH = px;
		invalidateHierarchy();
		return this;
	}

	private float padStart () {
		if (padH >= 0) return padH;
		return style == TEXT ? dp(12) : icon != 0 ? dp(16) : dp(24);
	}

	private float padEnd () {
		if (padH >= 0) return padH;
		return style == TEXT ? (icon != 0 ? dp(16) : dp(12)) : dp(24);
	}

	@Override
	public float getPrefWidth () {
		float w = padStart() + padEnd() + label.prefWidth();
		if (icon != 0) w += dp(18) + (label.getText().length() > 0 ? dp(8) : 0);
		return Math.max(w, dp(48));
	}

	@Override
	public float getPrefHeight () {
		return minHeight;
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();
		Theme t = theme();
		Gfx g = gfx();
		float a = parentAlpha * getColor().a;
		float x = getX(), y = getY(), w = getWidth(), h = getHeight();
		float r = radius < 0 ? h / 2 : radius;
		boolean on = isEnabled();
		Color bg = bgColor, fg = fgColor;
		if (bg == null)
			bg = style == FILLED ? t.primary : style == TONAL ? t.secondaryContainer : style == ELEVATED ? t.surfaceContainerLow : null;
		if (fg == null) fg = style == FILLED ? t.onPrimary : style == TONAL ? t.onSecondaryContainer : t.primary;
		float fgAlpha = a;
		if (!on) {
			fg = t.onSurface;
			fgAlpha = a * 0.38f;
			if (bg != null) g.roundRect(batch, x, y, w, h, r, t.onSurface, a * 0.12f);
		} else if (bg != null) {
			if (style == ELEVATED) g.shadow(batch, x, y, w, h, r, dp(1.5f), t.shadow, a);
			g.roundRect(batch, x, y, w, h, r, bg, a);
		}
		if (style == OUTLINED) g.stroke(batch, x, y, w, h, r, dp(1), on ? t.outline : t.onSurface, on ? a : a * 0.12f);
		ripple.draw(batch, g, x, y, w, h, r, fg, a);

		float iconSize = icon != 0 ? dp(18) : 0, gap = icon != 0 && label.getText().length() > 0 ? dp(8) : 0;
		float avail = w - padStart() - padEnd() - iconSize - gap;
		label.wrap(Math.max(1, avail));
		float tw = Math.min(label.width(), avail);
		float contentW = iconSize + gap + tw;
		float start = x + (w - contentW) / 2;
		boolean rtl = Compat.isRtl();
		float textX = rtl ? start : start + iconSize + gap;
		if (icon != 0) {
			float ix = rtl ? start + tw + gap : start;
			Compat.fonts().drawIcon(batch, icon, ix, y + (h - iconSize) / 2, iconSize, 1f, fg, fgAlpha);
		}
		label.setAlign(TextLayout.ALIGN_LEFT);
		label.draw(batch, textX, y + (h + label.height()) / 2, tw + 2, fg, fgAlpha);
	}
}
