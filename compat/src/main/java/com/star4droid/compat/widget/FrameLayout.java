package com.star4droid.compat.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Gravity;
import com.star4droid.compat.LayoutParams;

/** Stacks children on top of each other; each child is positioned with its layout gravity (like Android's FrameLayout). */
public class FrameLayout extends ViewGroup {
	@Override
	public float getPrefWidth () {
		SnapshotArray<Actor> ch = getChildren();
		float max = 0;
		for (int i = 0; i < ch.size; i++) {
			Actor c = ch.get(i);
			if (!c.isVisible()) continue;
			LayoutParams lp = lp(c);
			max = Math.max(max, (lp.width >= 0 ? lp.width : Measure.prefWidth(c)) + lp.marginLeft + lp.marginRight);
		}
		return max + padLeft + padRight;
	}

	@Override
	public float measureHeight (float width) {
		float inner = Math.max(0, width - padLeft - padRight);
		SnapshotArray<Actor> ch = getChildren();
		float max = 0;
		for (int i = 0; i < ch.size; i++) {
			Actor c = ch.get(i);
			if (!c.isVisible()) continue;
			LayoutParams lp = lp(c);
			float h = lp.height >= 0 ? lp.height : Measure.prefHeight(c, childWidth(c, lp, inner));
			max = Math.max(max, h + lp.marginTop + lp.marginBottom);
		}
		return max + padTop + padBottom;
	}

	@Override
	public float getPrefHeight () {
		return measureHeight(getWidth() > 0 ? getWidth() : getPrefWidth());
	}

	private static float childWidth (Actor c, LayoutParams lp, float inner) {
		float avail = Math.max(0, inner - lp.marginLeft - lp.marginRight);
		if (lp.width == LayoutParams.MATCH_PARENT) return avail;
		if (lp.width >= 0) return lp.width;
		return Math.min(Measure.prefWidth(c), avail);
	}

	@Override
	public void layout () {
		boolean rtl = Compat.isRtl();
		float width = getWidth(), height = getHeight();
		float pl = rtl ? padRight : padLeft, pr = rtl ? padLeft : padRight;
		float innerW = Math.max(0, width - padLeft - padRight), innerH = Math.max(0, height - padTop - padBottom);
		SnapshotArray<Actor> ch = getChildren();
		for (int i = 0; i < ch.size; i++) {
			Actor c = ch.get(i);
			if (!c.isVisible()) continue;
			LayoutParams lp = lp(c);
			float ml = rtl ? lp.marginRight : lp.marginLeft, mr = rtl ? lp.marginLeft : lp.marginRight;
			float cw = childWidth(c, lp, innerW);
			float availH = Math.max(0, innerH - lp.marginTop - lp.marginBottom);
			float chh = lp.height == LayoutParams.MATCH_PARENT ? availH
				: lp.height >= 0 ? lp.height : Math.min(Measure.prefHeight(c, cw), availH);
			int gh = Gravity.horizontal(lp.gravity, rtl), gv = Gravity.vertical(lp.gravity);
			float x = gh == 1 ? pl + ml + (innerW - ml - mr - cw) / 2 : gh == 2 ? width - pr - mr - cw : pl + ml;
			float y = gv == 1 ? padBottom + lp.marginBottom + (availH - chh) / 2
				: gv == 2 ? padBottom + lp.marginBottom : height - padTop - lp.marginTop - chh;
			place(c, x, y, cw, chh);
		}
	}
}
