package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Gravity;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.TextLayout;

/** Text with wrapping, max lines + ellipsis, alignment and automatic Arabic shaping / RTL. */
public class TextView extends View implements Measurable {
	private final TextLayout layout;
	private Color color = theme().onSurface;
	private int gravity = Gravity.NONE;
	private float padLeft, padTop, padRight, padBottom;
	private float maxWidth = Float.MAX_VALUE;

	public TextView () {
		this("", Typography.BODY_LARGE);
	}

	public TextView (CharSequence text) {
		this(text, Typography.BODY_LARGE);
	}

	/** @param style one of the {@link Typography} constants */
	public TextView (CharSequence text, int style) {
		layout = new TextLayout(Typography.sizePx(style), Typography.MEDIUM[style]);
		layout.setText(text);
	}

	public TextView setText (CharSequence text) {
		float before = layout.prefWidth();
		int lines = layout.lineCount();
		layout.setText(text);
		if (layout.prefWidth() != before || lines != 1 || layout.getText().toString().indexOf('\n') >= 0) invalidateHierarchy();
		Compat.requestRender();
		return this;
	}

	public CharSequence getText () {
		return layout.getText();
	}

	public TextView setTextAppearance (int style) {
		layout.setSize(Typography.sizePx(style));
		layout.setMedium(Typography.MEDIUM[style]);
		invalidateHierarchy();
		return this;
	}

	/** Text size in pixels (use {@code Compat.sp}). */
	public TextView setTextSize (float px) {
		layout.setSize(px);
		invalidateHierarchy();
		return this;
	}

	public TextView setBold (boolean bold) {
		layout.setMedium(bold);
		invalidateHierarchy();
		return this;
	}

	public TextView setTextColor (Color c) {
		color = c;
		return this;
	}

	public Color getTextColor () {
		return color;
	}

	/** Where the text sits inside the view, e.g. {@code Gravity.CENTER}. Without a horizontal flag the text follows its own
	 * direction (Arabic starts at the right). */
	public TextView setGravity (int g) {
		gravity = g;
		int a = TextLayout.ALIGN_START;
		if ((g & Gravity.CENTER_HORIZONTAL) != 0) a = TextLayout.ALIGN_CENTER;
		else if ((g & Gravity.END) != 0) a = TextLayout.ALIGN_END;
		else if ((g & Gravity.LEFT) != 0) a = TextLayout.ALIGN_LEFT;
		else if ((g & Gravity.RIGHT) != 0) a = TextLayout.ALIGN_RIGHT;
		layout.setAlign(a);
		return this;
	}

	public TextView setMaxLines (int n) {
		layout.setMaxLines(n);
		invalidateHierarchy();
		return this;
	}

	public TextView setSingleLine () {
		return setMaxLines(1);
	}

	public TextView setMaxWidth (float px) {
		maxWidth = px;
		invalidateHierarchy();
		return this;
	}

	public TextView setPadding (float left, float top, float right, float bottom) {
		padLeft = left;
		padTop = top;
		padRight = right;
		padBottom = bottom;
		invalidateHierarchy();
		return this;
	}

	public TextView setPadding (float all) {
		return setPadding(all, all, all, all);
	}

	@Override
	public float getPrefWidth () {
		return Math.min(maxWidth, layout.prefWidth() + padLeft + padRight);
	}

	@Override
	public float measureHeight (float width) {
		return layout.heightFor(Math.min(width, maxWidth) - padLeft - padRight) + padTop + padBottom;
	}

	@Override
	public float getPrefHeight () {
		return measureHeight(getWidth() > 0 ? getWidth() : getPrefWidth());
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();
		float w = getWidth() - padLeft - padRight, h = getHeight() - padTop - padBottom;
		if (w <= 0) return;
		layout.wrap(w);
		float th = layout.height();
		int gv = Gravity.vertical(gravity);
		float top = getY() + padBottom + (gv == 1 ? (h + th) / 2 : gv == 2 ? th : h);
		float a = parentAlpha * getColor().a;
		if (ripple.isActive()) ripple.draw(batch, gfx(), getX(), getY(), getWidth(), getHeight(), dp(4), color, a);
		layout.draw(batch, getX() + padLeft, top, w, color, a);
	}
}
