package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.Anim;
import com.star4droid.compat.graphics.Gfx;
import com.star4droid.compat.graphics.TextLayout;

/** Floating action button: small / normal / large, or extended with a label. {@link #show()} / {@link #hide()} animate it,
 * {@link #extend()} / {@link #shrink()} animate the label (handy while a list scrolls). */
public class Fab extends View {
	public static final int SMALL = 0, NORMAL = 1, LARGE = 2;

	private char icon;
	private int size = NORMAL;
	private TextLayout label;
	private Color bgColor, fgColor;
	private float elevation = dp(6);
	private final Anim shown = new Anim(1f), extended = new Anim(0f);

	public Fab (char icon) {
		this.icon = icon;
		ensurePressListener();
	}

	/** Extended FAB. */
	public Fab (char icon, CharSequence text) {
		this(icon);
		text(text);
	}

	public Fab size (int s) {
		size = s;
		invalidateHierarchy();
		return this;
	}

	public Fab icon (char c) {
		icon = c;
		Compat.requestRender();
		return this;
	}

	public Fab text (CharSequence text) {
		if (text == null || text.length() == 0) {
			label = null;
			extended.set(0);
		} else {
			if (label == null) label = new TextLayout(Typography.sizePx(Typography.LABEL_LARGE), true);
			label.setText(text);
			label.setMaxLines(1);
			extended.set(1);
		}
		invalidateHierarchy();
		return this;
	}

	public Fab onClick (Runnable r) {
		setOnClick(r);
		return this;
	}

	public Fab colors (Color background, Color content) {
		bgColor = background;
		fgColor = content;
		return this;
	}

	public Fab elevation (float px) {
		elevation = px;
		return this;
	}

	public void show () {
		setVisible(true);
		setTouchable(Touchable.enabled);
		shown.animateTo(1, 0.2f);
		Compat.requestRender();
	}

	public void hide () {
		setTouchable(Touchable.disabled);
		shown.animateTo(0, 0.15f);
		Compat.requestRender();
	}

	public boolean isShown () {
		return shown.target() > 0;
	}

	/** Shows the label of an extended FAB. */
	public void extend () {
		if (label != null) extended.animateTo(1, 0.22f);
		Compat.requestRender();
	}

	/** Collapses an extended FAB to its icon. */
	public void shrink () {
		extended.animateTo(0, 0.22f);
		Compat.requestRender();
	}

	public boolean isExtended () {
		return extended.target() > 0;
	}

	private float box () {
		return dp(size == SMALL ? 40 : size == LARGE ? 96 : 56);
	}

	private float iconSize () {
		return dp(size == LARGE ? 36 : 24);
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		boolean s = shown.update(delta), e = extended.update(delta);
		if (e) invalidateHierarchy();
		if (s || e) Compat.requestRender();
	}

	@Override
	public float getPrefWidth () {
		float b = box();
		if (label == null || extended.value <= 0) return b;
		float full = dp(16) + iconSize() + dp(12) + label.prefWidth() + dp(20);
		return b + (Math.max(b, full) - b) * extended.value;
	}

	@Override
	public float getPrefHeight () {
		return box();
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		float s = shown.value;
		if (s <= 0.01f) return;
		Theme t = theme();
		Gfx g = gfx();
		float a = parentAlpha * getColor().a * Math.min(1, s * 1.5f);
		float w = getWidth() * s, h = getHeight() * s;
		float x = getX() + (getWidth() - w) / 2, y = getY() + (getHeight() - h) / 2;
		float r = dp(size == SMALL ? 12 : size == LARGE ? 28 : 16) * s;
		Color bg = bgColor != null ? bgColor : t.primaryContainer, fg = fgColor != null ? fgColor : t.onPrimaryContainer;
		g.shadow(batch, x, y, w, h, r, elevation * s, t.shadow, a);
		g.roundRectSmooth(batch, x, y, w, h, r, bg, a);
		ripple.draw(batch, g, x, y, w, h, r, fg, a);
		float is = iconSize();
		float e = label != null ? extended.value : 0;
		boolean rtl = Compat.isRtl();
		float iconOffset = (box() - is) / 2 + (dp(16) - (box() - is) / 2) * e;
		float ix = rtl ? getX() + getWidth() - iconOffset - is : getX() + iconOffset;
		float iy = getY() + (getHeight() - is) / 2;
		if (s < 1) {
			// keep the icon inside the shrinking container
			float cx = getX() + getWidth() / 2, cy = getY() + getHeight() / 2;
			ix = cx + (ix + is / 2 - cx) * s - is / 2;
			iy = cy + (iy + is / 2 - cy) * s - is / 2;
		}
		Compat.fonts().drawIcon(batch, icon, ix, iy, is, s, fg, a);
		if (e > 0.5f && s >= 1) {
			label.wrap(100000);
			float tx = rtl ? getX() + dp(20) : getX() + dp(16) + is + dp(12);
			label.setAlign(TextLayout.ALIGN_LEFT);
			label.draw(batch, tx, getY() + (getHeight() + label.height()) / 2, label.prefWidth() + 2, fg, a * (e - 0.5f) * 2f);
		}
	}
}
