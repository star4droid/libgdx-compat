package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Anim;
import com.star4droid.compat.graphics.Gfx;

/** A draggable value picker. */
public class Slider extends View {
	public interface OnChangeListener {
		void onValueChanged (Slider slider, float value, boolean fromUser);
	}

	private float min = 0, max = 1, step = 0, value = 0;
	private boolean dragging;
	private final Anim press = new Anim(0f);
	private OnChangeListener listener;

	public Slider () {
		this(0, 1);
	}

	public Slider (float min, float max) {
		this.min = min;
		this.max = max;
		value = min;
		addListener(new InputListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != 0 || !isEnabled()) return false;
				dragging = true;
				press.animateTo(1, 0.12f);
				setFromX(x);
				// keep scrolling parents from stealing a horizontal drag that started on the slider
				event.stop();
				return true;
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				setFromX(x);
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				dragging = false;
				press.animateTo(0, 0.2f);
				Compat.requestRender();
			}
		});
	}

	private void setFromX (float x) {
		float pad = dp(10);
		float f = MathUtils.clamp((x - pad) / Math.max(1, getWidth() - 2 * pad), 0, 1);
		if (Compat.isRtl()) f = 1 - f;
		setValueInternal(min + f * (max - min), true);
	}

	private void setValueInternal (float v, boolean fromUser) {
		if (step > 0) v = min + Math.round((v - min) / step) * step;
		v = MathUtils.clamp(v, min, max);
		if (v == value) return;
		value = v;
		Compat.requestRender();
		if (listener != null) listener.onValueChanged(this, v, fromUser);
	}

	public Slider setValue (float v) {
		setValueInternal(v, false);
		return this;
	}

	public float getValue () {
		return value;
	}

	public Slider setRange (float min, float max) {
		this.min = min;
		this.max = max;
		value = MathUtils.clamp(value, min, max);
		return this;
	}

	/** Snap to multiples of step (0 = continuous). */
	public Slider setStep (float s) {
		step = s;
		return this;
	}

	public Slider setOnChange (OnChangeListener l) {
		listener = l;
		return this;
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (press.update(delta)) Compat.requestRender();
	}

	@Override
	public float getPrefWidth () {
		return dp(160);
	}

	@Override
	public float getPrefHeight () {
		return dp(44);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		Theme t = theme();
		Gfx g = gfx();
		float a = parentAlpha * getColor().a * (isEnabled() ? 1 : 0.38f);
		float pad = dp(10), x = getX() + pad, w = getWidth() - 2 * pad, cy = getY() + getHeight() / 2;
		float th = dp(4), f = max > min ? (value - min) / (max - min) : 0;
		boolean rtl = Compat.isRtl();
		float tx = x + w * (rtl ? 1 - f : f);
		g.roundRect(batch, x, cy - th / 2, w, th, th / 2, t.surfaceContainerHighest, a);
		if (rtl) g.roundRect(batch, tx, cy - th / 2, x + w - tx, th, th / 2, t.primary, a);
		else g.roundRect(batch, x, cy - th / 2, tx - x, th, th / 2, t.primary, a);
		if (press.value > 0) g.circle(batch, tx, cy, dp(10) + dp(10) * press.value, t.primary, a * 0.14f * press.value);
		g.circle(batch, tx, cy, dp(10), t.primary, a);
	}
}
