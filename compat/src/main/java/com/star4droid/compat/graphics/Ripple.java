package com.star4droid.compat.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

/** Touch feedback. The ripple grows from the touch point as a scaled copy of the widget's own rounded shape, so it is
 * clipped exactly to the shape without scissors or stencil (no extra draw calls). */
public final class Ripple {
	public static float PRESSED_ALPHA = 0.12f;
	private float px, py, grow, fade;
	private boolean down, active;

	public void press (float localX, float localY) {
		px = localX;
		py = localY;
		grow = 0;
		fade = 1;
		down = true;
		active = true;
	}

	public void release () {
		down = false;
	}

	public void cancel () {
		down = false;
		if (grow < 0.3f) active = false;
	}

	public boolean isActive () {
		return active;
	}

	/** @return true while animating. */
	public boolean act (float delta) {
		if (!active) return false;
		grow = Math.min(1f, grow + delta / 0.32f);
		if (!down && grow >= 0.55f) {
			fade -= delta / 0.22f;
			if (fade <= 0) {
				fade = 0;
				active = false;
			}
		}
		return true;
	}

	public void draw (Batch b, Gfx g, float x, float y, float w, float h, float radius, Color c, float alpha) {
		if (!active) return;
		float k = 0.12f + 0.88f * Interpolation.pow2Out.apply(grow);
		float cx = x + MathUtils.clamp(px, 0, w), cy = y + MathUtils.clamp(py, 0, h);
		g.roundRectSmooth(b, cx + (x - cx) * k, cy + (y - cy) * k, w * k, h * k, Math.min(radius, Math.min(w, h) / 2) * k, c,
			alpha * fade * PRESSED_ALPHA);
	}
}
