package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.TextLayout;

/** A row of tabs with an animated indicator. Fixed mode shares the width equally; scrollable mode lets tabs keep their
 * natural width and scroll. */
public class TabLayout extends ScrollContainer {
	public static final int MODE_FIXED = 0, MODE_SCROLLABLE = 1;

	public interface OnTabSelectedListener {
		void onTabSelected (int index);
	}

	/** One tab. Drawn directly by the tab view (no child actors). */
	public class Tab extends View {
		final TextLayout label = new TextLayout(Typography.sizePx(Typography.TITLE_SMALL), true);
		char icon;
		int index;

		Tab (CharSequence text, char icon) {
			label.setText(text);
			label.setMaxLines(1);
			this.icon = icon;
			setOnClick(new Runnable() {
				@Override
				public void run () {
					selectTab(index, true);
				}
			});
		}

		@Override
		public float getPrefWidth () {
			return Math.max(Compat.dp(72), Math.max(label.prefWidth(), icon != 0 ? Compat.dp(24) : 0) + Compat.dp(32));
		}

		float contentWidth () {
			return Math.max(label.getText().length() > 0 ? label.prefWidth() : 0, icon != 0 ? Compat.dp(24) : 0);
		}

		@Override
		public void draw (Batch batch, float parentAlpha) {
			Theme t = theme();
			float a = parentAlpha * getColor().a;
			float f = MathUtils.clamp(1 - Math.abs(index - indicatorPos), 0, 1);
			Color c = Views.lerp(unselectedColor != null ? unselectedColor : t.onSurfaceVariant,
				selectedColor != null ? selectedColor : t.primary, f);
			float x = getX(), y = getY(), w = getWidth(), h = getHeight();
			ripple.draw(batch, gfx(), x, y, w, h, 0, t.primary, a);
			boolean hasText = label.getText().length() > 0;
			float tw = Math.max(1, w - Compat.dp(16));
			if (icon != 0) {
				float is = Compat.dp(24);
				float iy = hasText ? y + h / 2 + Compat.dp(1) : y + (h - is) / 2;
				Compat.fonts().drawIcon(batch, icon, x + (w - is) / 2, iy, is, 1f, c, a);
			}
			if (hasText) {
				label.wrap(tw);
				label.setAlign(TextLayout.ALIGN_CENTER);
				float top = icon != 0 ? y + h / 2 - Compat.dp(2) : y + (h + label.height()) / 2;
				label.draw(batch, x + (w - tw) / 2, top, tw, c, a);
			}
		}
	}

	private final Array<Tab> tabs = new Array<Tab>();
	private int mode = MODE_FIXED, selected;
	float indicatorPos;
	private float indicatorTarget;
	private boolean animating;
	private OnTabSelectedListener listener;
	private ViewPager pager;
	Color selectedColor, unselectedColor;
	private Color indicatorColor;
	private boolean hasIcons, divider = true;

	public TabLayout () {
		super(false);
		setScrollbarEnabled(false);
	}

	public TabLayout (int mode) {
		this();
		this.mode = mode;
	}

	public Tab addTab (CharSequence text) {
		return addTab(text, (char)0);
	}

	public Tab addTab (CharSequence text, char icon) {
		Tab tab = new Tab(text, icon);
		tab.index = tabs.size;
		tabs.add(tab);
		if (icon != 0 && text != null && text.length() > 0) hasIcons = true;
		addActor(tab);
		return tab;
	}

	public void clearTabs () {
		tabs.clear();
		clearChildren();
		selected = 0;
		indicatorPos = 0;
	}

	public int getTabCount () {
		return tabs.size;
	}

	public void setMode (int m) {
		mode = m;
		invalidateHierarchy();
	}

	public void setColors (Color selected, Color unselected, Color indicator) {
		selectedColor = selected;
		unselectedColor = unselected;
		indicatorColor = indicator;
	}

	public void setDivider (boolean value) {
		divider = value;
	}

	public void setOnTabSelected (OnTabSelectedListener l) {
		listener = l;
	}

	public int getSelectedTab () {
		return selected;
	}

	public void selectTab (int index) {
		selectTab(index, false);
	}

	private void selectTab (int index, boolean fromUser) {
		if (index < 0 || index >= tabs.size) return;
		boolean changed = index != selected;
		selected = index;
		if (pager != null && fromUser) {
			pager.setCurrentItem(index);
		} else if (pager == null) {
			indicatorTarget = index;
			animating = getStage() != null;
			if (!animating) indicatorPos = index;
		}
		reveal(index);
		Compat.requestRender();
		if (changed && listener != null) listener.onTabSelected(index);
	}

	private void reveal (int index) {
		if (mode != MODE_SCROLLABLE || getWidth() <= 0) return;
		Tab t = tabs.get(index);
		float start = Compat.isRtl() ? getWidth() - (t.getX() + t.getWidth()) + scroll : t.getX() + scroll;
		smoothScrollTo(start + t.getWidth() / 2 - getWidth() / 2);
	}

	/** Keeps the tabs and a pager in sync in both directions. */
	public void setupWithViewPager (ViewPager p) {
		pager = p;
		indicatorPos = p.getPosition();
		selected = p.getCurrentItem();
		p.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled (float position) {
				indicatorPos = position;
				Compat.requestRender();
			}

			@Override
			public void onPageSelected (int index) {
				if (index != selected) selectTab(index, false);
			}
		});
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (animating) {
			float d = indicatorTarget - indicatorPos;
			float step = delta * 8f * Math.max(1f, Math.abs(d));
			if (Math.abs(d) <= step) {
				indicatorPos = indicatorTarget;
				animating = false;
			} else indicatorPos += Math.signum(d) * step;
			Compat.requestRender();
		}
	}

	@Override
	public float getPrefWidth () {
		float w = 0;
		for (Tab t : tabs)
			w += t.getPrefWidth();
		return w;
	}

	@Override
	public float getPrefHeight () {
		return Compat.dp(hasIcons ? 64 : 48);
	}

	@Override
	public float measureHeight (float width) {
		return getPrefHeight();
	}

	@Override
	protected float computeContentLength () {
		if (mode == MODE_FIXED) return getWidth();
		return Math.max(getPrefWidth(), 0);
	}

	@Override
	protected void layoutContent () {
		boolean rtl = Compat.isRtl();
		float w = getWidth(), h = getHeight(), x = -scroll;
		for (int i = 0; i < tabs.size; i++) {
			Tab t = tabs.get(i);
			float tw = mode == MODE_FIXED ? w / tabs.size : t.getPrefWidth();
			place(t, rtl ? w - x - tw : x, 0, tw, h);
			x += tw;
		}
	}

	@Override
	protected void drawOverContent (Batch batch, float alpha) {
		Theme th = theme();
		if (divider) gfx().rect(batch, getX(), getY(), getWidth(), Math.max(1, Compat.dp(1)), th.surfaceVariant, alpha);
		if (tabs.size == 0) return;
		int i0 = MathUtils.clamp((int)Math.floor(indicatorPos), 0, tabs.size - 1), i1 = Math.min(tabs.size - 1, i0 + 1);
		float f = MathUtils.clamp(indicatorPos - i0, 0, 1);
		Tab a = tabs.get(i0), b = tabs.get(i1);
		float wa = a.contentWidth(), wb = b.contentWidth();
		float ca = a.getX() + a.getWidth() / 2, cb = b.getX() + b.getWidth() / 2;
		float w = wa + (wb - wa) * f, c = ca + (cb - ca) * f, hgt = Compat.dp(3);
		gfx().roundRect(batch, getX() + c - w / 2, getY(), w, hgt, hgt, hgt, 0, 0, indicatorColor != null ? indicatorColor : th.primary,
			alpha);
	}
}
