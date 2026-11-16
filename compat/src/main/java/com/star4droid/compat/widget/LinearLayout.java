package com.star4droid.compat.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Gravity;
import com.star4droid.compat.LayoutParams;

/** Arranges children in a row or a column, with weights, margins and gravity, like Android's LinearLayout. Horizontal
 * layouts are mirrored automatically in RTL mode. */
public class LinearLayout extends ViewGroup {
	public static final int HORIZONTAL = 0, VERTICAL = 1;

	private int orientation;
	private int gravity = Gravity.NONE;
	private float spacing;
	private float[] sizes = new float[8];
	private float contentMain;
	private boolean anyWeight;

	public LinearLayout () {
		this(VERTICAL);
	}

	public LinearLayout (int orientation) {
		this.orientation = orientation;
	}

	public static LinearLayout vertical () {
		return new LinearLayout(VERTICAL);
	}

	public static LinearLayout horizontal () {
		return new LinearLayout(HORIZONTAL);
	}

	public void setOrientation (int o) {
		orientation = o;
		invalidateHierarchy();
	}

	public int getOrientation () {
		return orientation;
	}

	/** How children are packed when they do not fill the layout. */
	public void setGravity (int g) {
		gravity = g;
		invalidate();
	}

	/** Gap between neighbouring children, in pixels. */
	public void setSpacing (float px) {
		spacing = px;
		invalidateHierarchy();
	}

	// chaining helpers
	public LinearLayout gravity (int g) {
		setGravity(g);
		return this;
	}

	public LinearLayout spacing (float px) {
		setSpacing(px);
		return this;
	}

	public LinearLayout padding (float all) {
		setPadding(all);
		return this;
	}

	public LinearLayout padding (float h, float v) {
		setPadding(h, v);
		return this;
	}

	private void ensure (int n) {
		if (sizes.length < n) sizes = new float[n + 8];
	}

	/** Main-axis sizes of a horizontal layout for the given inner width. A wrap_content child (typically text) shrinks so
	 * that the children after it still fit. */
	private void computeWidths (float inner) {
		SnapshotArray<Actor> ch = getChildren();
		ensure(ch.size);
		float overhead = 0, natural = 0, totalWeight = 0;
		int visible = 0;
		// pass 1: natural sizes
		for (int i = 0; i < ch.size; i++) {
			Actor c = ch.get(i);
			if (!c.isVisible()) {
				sizes[i] = -1;
				continue;
			}
			LayoutParams lp = lp(c);
			if (visible++ > 0) overhead += spacing;
			overhead += lp.marginLeft + lp.marginRight;
			float w;
			if (lp.weight > 0) {
				w = lp.width >= 0 ? lp.width : (lp.width == LayoutParams.WRAP_CONTENT ? Measure.prefWidth(c) : 0);
				totalWeight += lp.weight;
			} else if (lp.width == LayoutParams.MATCH_PARENT) w = 0;
			else w = lp.width >= 0 ? lp.width : Measure.prefWidth(c);
			sizes[i] = w;
			natural += w;
		}
		// pass 2: fit match_parent / wrap_content children into what is left
		float used = 0, after = natural;
		for (int i = 0; i < ch.size; i++) {
			if (sizes[i] < 0) continue;
			LayoutParams lp = lp(ch.get(i));
			after -= sizes[i];
			if (lp.weight <= 0 && lp.width < 0) {
				float room = Math.max(0, inner - overhead - used - after);
				sizes[i] = lp.width == LayoutParams.MATCH_PARENT ? (inner < 1e6f ? room : Measure.prefWidth(ch.get(i)))
					: Math.min(sizes[i], room);
			}
			used += sizes[i];
		}
		used += overhead;
		anyWeight = totalWeight > 0;
		if (anyWeight && inner < 1e6f) {
			float extra = inner - used;
			for (int i = 0; i < ch.size; i++) {
				if (sizes[i] < 0) continue;
				LayoutParams lp = lp(ch.get(i));
				if (lp.weight > 0) sizes[i] = Math.max(0, sizes[i] + extra * lp.weight / totalWeight);
			}
			used = inner;
		}
		contentMain = used;
	}

	private static float childWidthVertical (Actor c, LayoutParams lp, float inner) {
		float avail = inner - lp.marginLeft - lp.marginRight;
		if (lp.width == LayoutParams.MATCH_PARENT) return Math.max(0, avail);
		if (lp.width >= 0) return lp.width;
		return Math.max(0, Math.min(Measure.prefWidth(c), avail));
	}

	/** Main-axis sizes of a vertical layout. innerH &lt; 0 means "unbounded" (measuring). */
	private void computeHeights (float innerW, float innerH) {
		SnapshotArray<Actor> ch = getChildren();
		ensure(ch.size);
		float used = 0, totalWeight = 0;
		int visible = 0;
		for (int i = 0; i < ch.size; i++) {
			Actor c = ch.get(i);
			if (!c.isVisible()) {
				sizes[i] = -1;
				continue;
			}
			LayoutParams lp = lp(c);
			if (visible++ > 0) used += spacing;
			used += lp.marginTop + lp.marginBottom;
			float h;
			if (lp.weight > 0) {
				h = lp.height >= 0 ? lp.height
					: (lp.height == LayoutParams.WRAP_CONTENT || innerH < 0 ? Measure.prefHeight(c, childWidthVertical(c, lp, innerW))
						: 0);
				totalWeight += lp.weight;
			} else if (lp.height == LayoutParams.MATCH_PARENT && innerH >= 0) {
				h = Math.max(0, innerH - used);
			} else if (lp.height >= 0) {
				h = lp.height;
			} else {
				h = Measure.prefHeight(c, childWidthVertical(c, lp, innerW));
			}
			sizes[i] = h;
			used += h;
		}
		anyWeight = totalWeight > 0;
		if (anyWeight && innerH >= 0) {
			float extra = innerH - used;
			for (int i = 0; i < ch.size; i++) {
				if (sizes[i] < 0) continue;
				LayoutParams lp = lp(ch.get(i));
				if (lp.weight > 0) sizes[i] = Math.max(0, sizes[i] + extra * lp.weight / totalWeight);
			}
			used = innerH;
		}
		contentMain = used;
	}

	@Override
	public float getPrefWidth () {
		SnapshotArray<Actor> ch = getChildren();
		float total = 0, max = 0;
		int visible = 0;
		for (int i = 0; i < ch.size; i++) {
			Actor c = ch.get(i);
			if (!c.isVisible()) continue;
			LayoutParams lp = lp(c);
			float w = (lp.width >= 0 ? lp.width : Measure.prefWidth(c)) + lp.marginLeft + lp.marginRight;
			if (visible++ > 0) total += spacing;
			total += w;
			if (w > max) max = w;
		}
		return (orientation == HORIZONTAL ? total : max) + padLeft + padRight;
	}

	@Override
	public float measureHeight (float width) {
		float inner = Math.max(0, width - padLeft - padRight);
		SnapshotArray<Actor> ch = getChildren();
		if (orientation == VERTICAL) {
			computeHeights(inner, -1);
			return contentMain + padTop + padBottom;
		}
		computeWidths(inner);
		float max = 0;
		for (int i = 0; i < ch.size; i++) {
			if (sizes[i] < 0) continue;
			Actor c = ch.get(i);
			LayoutParams lp = lp(c);
			float h = (lp.height >= 0 ? lp.height : Measure.prefHeight(c, sizes[i])) + lp.marginTop + lp.marginBottom;
			if (h > max) max = h;
		}
		return max + padTop + padBottom;
	}

	@Override
	public float getPrefHeight () {
		return measureHeight(getWidth() > 0 ? getWidth() : getPrefWidth());
	}

	@Override
	public void layout () {
		boolean rtl = Compat.isRtl();
		float width = getWidth(), height = getHeight();
		float pl = rtl ? padRight : padLeft, pr = rtl ? padLeft : padRight;
		float innerW = Math.max(0, width - padLeft - padRight), innerH = Math.max(0, height - padTop - padBottom);
		SnapshotArray<Actor> ch = getChildren();
		if (orientation == VERTICAL) {
			computeHeights(innerW, innerH);
			float offset = 0;
			if (!anyWeight && contentMain < innerH) {
				int gv = Gravity.vertical(gravity);
				offset = gv == 1 ? (innerH - contentMain) / 2 : gv == 2 ? innerH - contentMain : 0;
			}
			float top = height - padTop - offset;
			boolean first = true;
			for (int i = 0; i < ch.size; i++) {
				if (sizes[i] < 0) continue;
				Actor c = ch.get(i);
				LayoutParams lp = lp(c);
				float ml = rtl ? lp.marginRight : lp.marginLeft, mr = rtl ? lp.marginLeft : lp.marginRight;
				float cw = childWidthVertical(c, lp, innerW), chh = sizes[i];
				int g = (lp.gravity & Gravity.H_MASK) != 0 ? lp.gravity : gravity;
				int gh = Gravity.horizontal(g, rtl);
				float x = gh == 1 ? pl + ml + (innerW - ml - mr - cw) / 2 : gh == 2 ? width - pr - mr - cw : pl + ml;
				if (!first) top -= spacing;
				first = false;
				top -= lp.marginTop;
				place(c, x, top - chh, cw, chh);
				top -= chh + lp.marginBottom;
			}
		} else {
			computeWidths(innerW);
			float offset = 0;
			if (!anyWeight && contentMain < innerW) {
				int gh = Gravity.horizontal(gravity, rtl);
				// offset is measured from the start edge
				if (gh == 1) offset = (innerW - contentMain) / 2;
				else if ((gh == 2) != rtl) offset = innerW - contentMain;
			}
			float cursor = pl + offset; // distance from the start edge
			boolean first = true;
			for (int i = 0; i < ch.size; i++) {
				if (sizes[i] < 0) continue;
				Actor c = ch.get(i);
				LayoutParams lp = lp(c);
				float cw = sizes[i];
				float avail = innerH - lp.marginTop - lp.marginBottom;
				float chh = lp.height == LayoutParams.MATCH_PARENT ? avail
					: lp.height >= 0 ? lp.height : Math.min(Measure.prefHeight(c, cw), Math.max(avail, 0));
				int g = (lp.gravity & Gravity.V_MASK) != 0 ? lp.gravity : gravity;
				int gv = Gravity.vertical(g);
				float y = gv == 1 ? padBottom + lp.marginBottom + (avail - chh) / 2
					: gv == 2 ? padBottom + lp.marginBottom : height - padTop - lp.marginTop - chh;
				if (!first) cursor += spacing;
				first = false;
				cursor += lp.marginLeft;
				float x = rtl ? width - cursor - cw : cursor;
				place(c, x, y, cw, chh);
				cursor += cw + lp.marginRight;
			}
		}
	}
}
