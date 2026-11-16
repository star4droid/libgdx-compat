package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Icons;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.Anim;
import com.star4droid.compat.graphics.Gfx;
import com.star4droid.compat.graphics.TextLayout;

/** Compact element for a choice, filter or action. A checkable chip toggles on tap and shows a check mark. */
public class Chip extends View {
	public interface OnSelectedListener {
		void onSelected (Chip chip, boolean selected);
	}

	private final TextLayout label = new TextLayout(Typography.sizePx(Typography.LABEL_LARGE), true);
	private char icon;
	private boolean checkable, selected;
	private final Anim sel = new Anim(0f);
	private OnSelectedListener listener;
	private Color selectedColor;

	public Chip (CharSequence text) {
		label.setText(text);
		label.setMaxLines(1);
		ensurePressListener();
	}

	public Chip icon (char c) {
		icon = c;
		invalidateHierarchy();
		return this;
	}

	public Chip checkable (boolean value) {
		checkable = value;
		return this;
	}

	public Chip onClick (Runnable r) {
		setOnClick(r);
		return this;
	}

	public Chip onSelected (OnSelectedListener l) {
		listener = l;
		checkable = true;
		return this;
	}

	public Chip selectedColor (Color c) {
		selectedColor = c;
		return this;
	}

	public Chip text (CharSequence t) {
		label.setText(t);
		invalidateHierarchy();
		return this;
	}

	public boolean isSelected () {
		return selected;
	}

	public void setSelected (boolean value) {
		if (selected == value) return;
		selected = value;
		sel.animateTo(value ? 1 : 0, getStage() != null ? 0.15f : 0);
		invalidateHierarchy();
		Compat.requestRender();
		if (listener != null) listener.onSelected(this, value);
	}

	@Override
	public void performClick () {
		if (checkable) setSelected(!selected);
		super.performClick();
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (sel.update(delta)) Compat.requestRender();
	}

	private boolean hasLeading () {
		return icon != 0 || (checkable && selected);
	}

	@Override
	public float getPrefWidth () {
		return (hasLeading() ? dp(8) + dp(18) + dp(8) : dp(16)) + label.prefWidth() + dp(16);
	}

	@Override
	public float getPrefHeight () {
		return dp(32);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();
		Theme t = theme();
		Gfx g = gfx();
		float a = parentAlpha * getColor().a * (isEnabled() ? 1 : 0.38f);
		float x = getX(), y = getY(), w = getWidth(), h = getHeight(), r = dp(8), v = sel.value;
		if (v > 0) g.roundRect(batch, x, y, w, h, r, selectedColor != null ? selectedColor : t.secondaryContainer, a * v);
		if (v < 1) g.stroke(batch, x, y, w, h, r, dp(1), t.outline, a * (1 - v));
		Color fg = selected ? t.onSecondaryContainer : t.onSurfaceVariant;
		ripple.draw(batch, g, x, y, w, h, r, fg, a);
		boolean rtl = Compat.isRtl();
		float lead = hasLeading() ? dp(8) + dp(18) + dp(8) : dp(16);
		if (hasLeading()) {
			char c = checkable && selected ? Icons.CHECK : icon;
			float ix = rtl ? x + w - dp(8) - dp(18) : x + dp(8);
			Compat.fonts().drawIcon(batch, c, ix, y + (h - dp(18)) / 2, dp(18), 1f, selected ? fg : t.primary, a);
		}
		float tw = Math.max(1, w - lead - dp(16));
		label.wrap(tw);
		label.setAlign(TextLayout.ALIGN_CENTER);
		label.draw(batch, rtl ? x + dp(16) : x + lead, y + (h + label.height()) / 2, tw, fg, a);
	}
}
