package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.Anim;
import com.star4droid.compat.graphics.TextLayout;

/** Base of {@link CheckBox}, {@link RadioButton} and {@link Switch}: a two-state control with an optional text label. */
public abstract class CompoundButton extends View {
	public interface OnCheckedChangeListener {
		void onCheckedChanged (CompoundButton button, boolean checked);
	}

	protected boolean checked;
	protected final Anim anim = new Anim(0f);
	protected TextLayout label;
	private OnCheckedChangeListener listener;

	protected CompoundButton (CharSequence text) {
		setText(text);
		ensurePressListener();
	}

	public void setText (CharSequence text) {
		if (text == null || text.length() == 0) label = null;
		else {
			if (label == null) label = new TextLayout(Typography.sizePx(Typography.BODY_LARGE), false);
			label.setText(text);
		}
		invalidateHierarchy();
	}

	public boolean isChecked () {
		return checked;
	}

	public void setChecked (boolean value) {
		setChecked(value, true);
	}

	public void setChecked (boolean value, boolean animate) {
		if (checked == value) return;
		checked = value;
		anim.animateTo(value ? 1 : 0, animate && getStage() != null ? 0.18f : 0);
		Compat.requestRender();
		if (listener != null) listener.onCheckedChanged(this, value);
	}

	public void toggle () {
		setChecked(!checked);
	}

	public void setOnCheckedChange (OnCheckedChangeListener l) {
		listener = l;
	}

	@Override
	public void performClick () {
		toggle();
		super.performClick();
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (anim.update(delta)) Compat.requestRender();
	}

	/** Width of the indicator area (including its touch padding). */
	protected abstract float indicatorWidth ();

	/** Draws the indicator inside the box (x, y, indicatorWidth, height). */
	protected abstract void drawIndicator (Batch batch, float x, float y, float h, float alpha);

	@Override
	public float getPrefWidth () {
		return indicatorWidth() + (label != null ? dp(4) + label.prefWidth() + dp(8) : 0);
	}

	@Override
	public float getPrefHeight () {
		return Math.max(dp(40), label != null ? label.lineHeight() + dp(8) : 0);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();
		float a = parentAlpha * getColor().a * (isEnabled() ? 1f : 0.38f);
		boolean rtl = Compat.isRtl();
		float iw = indicatorWidth();
		float ix = rtl ? getX() + getWidth() - iw : getX();
		drawIndicator(batch, ix, getY(), getHeight(), a);
		if (label != null) {
			float tw = Math.max(1, getWidth() - iw - dp(4));
			label.wrap(tw);
			label.setAlign(rtl ? TextLayout.ALIGN_RIGHT : TextLayout.ALIGN_LEFT);
			float tx = rtl ? getX() : getX() + iw + dp(4);
			label.draw(batch, tx, getY() + (getHeight() + label.height()) / 2, tw, theme().onSurface, a);
		}
	}
}
