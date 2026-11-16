package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.TextLayout;

/** The content of a navigation drawer: an optional header, then destinations with icon and label. */
public class NavigationView extends LinearLayout {
	public interface OnItemSelectedListener {
		void onItemSelected (int index);
	}

	private class Item extends View {
		final TextLayout label = new TextLayout(Typography.sizePx(Typography.LABEL_LARGE), true);
		final char icon;
		final int index;

		Item (CharSequence text, char icon, int index, final Runnable onClick) {
			label.setText(text);
			label.setMaxLines(1);
			this.icon = icon;
			this.index = index;
			setOnClick(new Runnable() {
				@Override
				public void run () {
					setCheckedItem(Item.this.index);
					if (listener != null) listener.onItemSelected(Item.this.index);
					if (onClick != null) onClick.run();
				}
			});
		}

		@Override
		public float getPrefHeight () {
			return Compat.dp(56);
		}

		@Override
		public float getPrefWidth () {
			return Compat.dp(200);
		}

		@Override
		public void draw (Batch batch, float parentAlpha) {
			Theme t = theme();
			float a = parentAlpha * getColor().a;
			float x = getX(), y = getY(), w = getWidth(), h = getHeight(), r = h / 2;
			boolean on = checked == index;
			if (on) gfx().roundRect(batch, x, y, w, h, r, t.secondaryContainer, a);
			ripple.draw(batch, gfx(), x, y, w, h, r, t.onSurface, a);
			boolean rtl = Compat.isRtl();
			float pad = Compat.dp(16), is = Compat.dp(24);
			float textStart = pad;
			if (icon != 0) {
				Compat.fonts().drawIcon(batch, icon, rtl ? x + w - pad - is : x + pad, y + (h - is) / 2, is, 1f,
					on ? t.onSecondaryContainer : t.onSurfaceVariant, a);
				textStart += is + Compat.dp(12);
			}
			float tw = Math.max(1, w - textStart - Compat.dp(24));
			label.wrap(tw);
			label.setAlign(rtl ? TextLayout.ALIGN_RIGHT : TextLayout.ALIGN_LEFT);
			label.draw(batch, rtl ? x + Compat.dp(24) : x + textStart, y + (h + label.height()) / 2, tw,
				on ? t.onSecondaryContainer : t.onSurfaceVariant, a);
		}
	}

	private final Array<Item> items = new Array<Item>();
	private int checked = -1;
	private OnItemSelectedListener listener;

	public NavigationView () {
		super(VERTICAL);
		Theme t = theme();
		setBackground(t.surfaceContainerLow);
		setTouchable(Touchable.enabled);
		float r = Compat.dp(t.cornerLarge);
		if (Compat.isRtl()) setCornerRadii(r, 0, 0, r);
		else setCornerRadii(0, r, r, 0);
		setPadding(Compat.dp(12), Compat.dp(12), Compat.dp(12), Compat.dp(12));
	}

	public NavigationView addHeader (Actor header) {
		add(header).matchWidth().marginBottom(Compat.dp(8));
		return this;
	}

	public NavigationView addItem (char icon, CharSequence text, Runnable onClick) {
		Item item = new Item(text, icon, items.size, onClick);
		items.add(item);
		add(item).matchWidth();
		return this;
	}

	public NavigationView addSectionTitle (CharSequence text) {
		TextView tv = new TextView(text, Typography.TITLE_SMALL);
		tv.setTextColor(theme().onSurfaceVariant);
		add(tv).matchWidth().margins(Compat.dp(16), Compat.dp(16), Compat.dp(16), Compat.dp(8));
		return this;
	}

	public NavigationView addDivider () {
		add(new Divider()).matchWidth().margin(Compat.dp(16), Compat.dp(8));
		return this;
	}

	public void setCheckedItem (int index) {
		checked = index;
		Compat.requestRender();
	}

	public int getCheckedItem () {
		return checked;
	}

	public void setOnItemSelected (OnItemSelectedListener l) {
		listener = l;
	}
}
