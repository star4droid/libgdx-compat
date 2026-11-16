package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Icons;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Gfx;

public class CheckBox extends CompoundButton {
	public CheckBox () {
		super(null);
	}

	public CheckBox (CharSequence text) {
		super(text);
	}

	public CheckBox (CharSequence text, boolean checked) {
		super(text);
		setChecked(checked, false);
	}

	@Override
	protected float indicatorWidth () {
		return dp(40);
	}

	@Override
	protected void drawIndicator (Batch batch, float x, float y, float h, float alpha) {
		Theme t = theme();
		Gfx g = gfx();
		float s = dp(18), cx = x + dp(20), cy = y + h / 2, v = anim.value;
		ripple.draw(batch, g, cx - dp(20), cy - dp(20), dp(40), dp(40), dp(20), checked ? t.primary : t.onSurface, alpha);
		float bx = cx - s / 2, by = cy - s / 2;
		if (v < 1) g.stroke(batch, bx, by, s, s, dp(2), dp(2), t.onSurfaceVariant, alpha * (1 - v));
		if (v > 0) {
			g.roundRect(batch, bx, by, s, s, dp(2), t.primary, alpha * v);
			Compat.fonts().drawIcon(batch, Icons.CHECK, bx, by, s, 0.4f + 0.6f * v, t.onPrimary, alpha * v);
		}
	}
}
