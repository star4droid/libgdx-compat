package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Gfx;

/** Material 3 switch. */
public class Switch extends CompoundButton {
	public Switch () {
		super(null);
	}

	public Switch (CharSequence text) {
		super(text);
	}

	public Switch (CharSequence text, boolean checked) {
		super(text);
		setChecked(checked, false);
	}

	@Override
	protected float indicatorWidth () {
		return dp(52);
	}

	@Override
	public float getPrefWidth () {
		return indicatorWidth() + (label != null ? dp(12) + label.prefWidth() : 0);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		// the switch sits at the end of its label, like a settings row
		validate();
		float a = parentAlpha * getColor().a * (isEnabled() ? 1f : 0.38f);
		boolean rtl = Compat.isRtl();
		float iw = indicatorWidth();
		float ix = rtl ? getX() : getX() + getWidth() - iw;
		if (label != null) {
			float tw = Math.max(1, getWidth() - iw - dp(12));
			label.wrap(tw);
			label.setAlign(rtl ? com.star4droid.compat.graphics.TextLayout.ALIGN_RIGHT
				: com.star4droid.compat.graphics.TextLayout.ALIGN_LEFT);
			label.draw(batch, rtl ? getX() + iw + dp(12) : getX(), getY() + (getHeight() + label.height()) / 2, tw,
				theme().onSurface, a);
		}
		drawIndicator(batch, ix, getY(), getHeight(), a);
	}

	@Override
	protected void drawIndicator (Batch batch, float x, float y, float h, float alpha) {
		Theme t = theme();
		Gfx g = gfx();
		float v = anim.value, tw = dp(52), th = dp(32), ty = y + (h - th) / 2;
		boolean rtl = Compat.isRtl();
		if (v < 1) {
			g.roundRect(batch, x, ty, tw, th, th / 2, t.surfaceContainerHighest, alpha * (1 - v));
			g.stroke(batch, x, ty, tw, th, th / 2, dp(2), t.outline, alpha * (1 - v));
		}
		if (v > 0) g.roundRect(batch, x, ty, tw, th, th / 2, t.primary, alpha * v);
		float d = isPressed() ? dp(28) : dp(16) + dp(8) * v;
		float travel = tw - th, pos = rtl ? 1 - v : v;
		float cx = x + th / 2 + travel * pos, cy = ty + th / 2;
		ripple.draw(batch, g, cx - dp(20), cy - dp(20), dp(40), dp(40), dp(20), checked ? t.primary : t.onSurface, alpha);
		g.circle(batch, cx, cy, d / 2, Views.lerp(t.outline, t.onPrimary, v), alpha);
	}
}
