package com.star4droid.compat.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.star4droid.compat.Compat;
import com.star4droid.compat.LayoutParams;

/** Lays children out in a row and wraps to the next row when the width runs out (chip groups, tags). */
public class FlowLayout extends ViewGroup {
	private float spacingH, spacingV;

	public FlowLayout () {
		spacingH = spacingV = Compat.dp(8);
	}

	public void setSpacing (float horizontal, float vertical) {
		spacingH = horizontal;
		spacingV = vertical;
		invalidateHierarchy();
	}

	@Override
	public float getPrefWidth () {
		SnapshotArray<Actor> ch = getChildren();
		float total = 0;
		int n = 0;
		for (int i = 0; i < ch.size; i++) {
			Actor c = ch.get(i);
			if (!c.isVisible()) continue;
			LayoutParams lp = lp(c);
			if (n++ > 0) total += spacingH;
			total += (lp.width >= 0 ? lp.width : Measure.prefWidth(c)) + lp.marginLeft + lp.marginRight;
		}
		return total + padLeft + padRight;
	}

	/** Runs the flow; positions children when place is true. @return the total height */
	private float flow (float width, boolean place) {
		boolean rtl = Compat.isRtl();
		float inner = Math.max(0, width - padLeft - padRight);
		SnapshotArray<Actor> ch = getChildren();
		float x = 0, y = padTop, rowH = 0;
		for (int i = 0; i < ch.size; i++) {
			Actor c = ch.get(i);
			if (!c.isVisible()) continue;
			LayoutParams lp = lp(c);
			float cw = Math.min(inner, lp.width >= 0 ? lp.width : lp.width == LayoutParams.MATCH_PARENT ? inner : Measure.prefWidth(c));
			float chh = lp.height >= 0 ? lp.height : Measure.prefHeight(c, cw);
			float full = cw + lp.marginLeft + lp.marginRight;
			if (x > 0 && x + full > inner + 0.5f) {
				x = 0;
				y += rowH + spacingV;
				rowH = 0;
			}
			if (place) {
				float px = padLeft + x + lp.marginLeft;
				if (rtl) px = width - px - cw;
				place(c, px, getHeight() - y - lp.marginTop - chh, cw, chh);
			}
			x += full + spacingH;
			rowH = Math.max(rowH, chh + lp.marginTop + lp.marginBottom);
		}
		return y + rowH + padBottom;
	}

	@Override
	public float measureHeight (float width) {
		return flow(width, false);
	}

	@Override
	public float getPrefHeight () {
		return measureHeight(getWidth() > 0 ? getWidth() : getPrefWidth());
	}

	@Override
	public void layout () {
		flow(getWidth(), true);
	}
}
