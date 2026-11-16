package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Gfx;

/** Put several inside a {@link RadioGroup} to make them exclusive. */
public class RadioButton extends CompoundButton {
	public RadioButton () {
		super(null);
	}

	public RadioButton (CharSequence text) {
		super(text);
	}

	@Override
	public void performClick () {
		if (!checked) setChecked(true);
		if (onClick != null) onClick.run();
	}

	@Override
	protected float indicatorWidth () {
		return dp(40);
	}

	@Override
	protected void drawIndicator (Batch batch, float x, float y, float h, float alpha) {
		Theme t = theme();
		Gfx g = gfx();
		float cx = x + dp(20), cy = y + h / 2, v = anim.value;
		ripple.draw(batch, g, cx - dp(20), cy - dp(20), dp(40), dp(40), dp(20), checked ? t.primary : t.onSurface, alpha);
		g.ring(batch, cx, cy, dp(10), dp(2), Views.lerp(t.onSurfaceVariant, t.primary, v), alpha);
		if (v > 0) g.circle(batch, cx, cy, dp(5) * v, t.primary, alpha);
	}
}
