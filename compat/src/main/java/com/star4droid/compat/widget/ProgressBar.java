package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Anim;
import com.star4droid.compat.graphics.Gfx;

/** Progress indicator, linear (default) or circular, determinate or indeterminate. */
public class ProgressBar extends View {
	public static final int LINEAR = 0, CIRCULAR = 1;

	private final int type;
	private boolean indeterminate = true;
	private final Anim progress = new Anim(0f);
	private float time;
	private Color color, trackColor;
	private float size = dp(40), thickness = dp(4);

	public ProgressBar () {
		this(LINEAR);
	}

	public ProgressBar (int type) {
		this.type = type;
		setTouchable(Touchable.disabled);
	}

	public static ProgressBar circular () {
		return new ProgressBar(CIRCULAR);
	}

	/** @param value 0..1; switches the bar to determinate mode */
	public ProgressBar setProgress (float value) {
		indeterminate = false;
		progress.animateTo(MathUtils.clamp(value, 0, 1), getStage() != null ? 0.25f : 0);
		Compat.requestRender();
		return this;
	}

	public float getProgress () {
		return progress.target();
	}

	public ProgressBar setIndeterminate (boolean value) {
		indeterminate = value;
		return this;
	}

	public ProgressBar setColors (Color indicator, Color track) {
		color = indicator;
		trackColor = track;
		return this;
	}

	/** Diameter of a circular indicator / stroke thickness, in pixels. */
	public ProgressBar setIndicatorSize (float diameter, float stroke) {
		size = diameter;
		thickness = stroke;
		invalidateHierarchy();
		return this;
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		boolean moving = progress.update(delta);
		if (indeterminate && Views.isShown(this)) {
			time += delta;
			moving = true;
		}
		if (moving) Compat.requestRender();
	}

	@Override
	public float getPrefWidth () {
		return type == CIRCULAR ? size : dp(120);
	}

	@Override
	public float getPrefHeight () {
		return type == CIRCULAR ? size : thickness;
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		Theme t = theme();
		Gfx g = gfx();
		float a = parentAlpha * getColor().a;
		Color c = color != null ? color : t.primary, tr = trackColor != null ? trackColor : t.surfaceContainerHighest;
		float x = getX(), y = getY(), w = getWidth(), h = getHeight();
		if (type == LINEAR) {
			float th = Math.min(h, thickness), ty = y + (h - th) / 2;
			g.roundRect(batch, x, ty, w, th, th / 2, tr, a);
			if (!indeterminate) {
				float f = Compat.isRtl() ? 1 - progress.value : 0;
				g.roundRect(batch, x + w * f, ty, w * progress.value, th, th / 2, c, a);
			} else {
				float p = (time % 1.8f) / 1.8f;
				float head = Interpolation.pow2.apply(MathUtils.clamp(p * 1.4f, 0, 1));
				float tail = Interpolation.pow2.apply(MathUtils.clamp(p * 1.4f - 0.4f, 0, 1));
				float sx = x + w * tail, ex = x + w * head;
				if (ex - sx > 1) g.roundRectSmooth(batch, sx, ty, ex - sx, th, th / 2, c, a);
			}
		} else {
			float d = Math.min(size, Math.min(w, h)), radius = (d - thickness) / 2;
			float cx = x + w / 2, cy = y + h / 2;
			if (!indeterminate) {
				g.arc(batch, cx, cy, radius, thickness, 0, 360, tr, a, false);
				if (progress.value > 0.001f) g.arc(batch, cx, cy, radius, thickness, 90, -360 * progress.value, c, a, true);
			} else {
				float cycle = 1.4f, p = (time % cycle) / cycle;
				float sweep = 30 + 250 * (p < 0.5f ? Interpolation.pow2.apply(p * 2) : 1 - Interpolation.pow2.apply(p * 2 - 1));
				float base = -time * 220f - (p >= 0.5f ? 250 * Interpolation.pow2.apply(p * 2 - 1) : 0)
					- 250f * (float)Math.floor(time / cycle);
				g.arc(batch, cx, cy, radius, thickness, base, -sweep, c, a, true);
			}
		}
	}
}
