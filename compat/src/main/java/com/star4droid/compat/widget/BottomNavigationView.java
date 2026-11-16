package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.Anim;
import com.star4droid.compat.graphics.TextLayout;

/** Bottom navigation bar with 3-5 destinations, an animated active indicator and optional badges. */
public class BottomNavigationView extends ViewGroup {
	public interface OnItemSelectedListener {
		void onItemSelected (int index);
	}

	private class Item extends View {
		final TextLayout label = new TextLayout(Typography.sizePx(Typography.LABEL_MEDIUM), true);
		final char icon;
		char selectedIcon;
		final int index;
		final Anim sel = new Anim(0f);
		int badge = -1;
		TextLayout badgeText;

		Item (CharSequence text, char icon, int index) {
			label.setText(text);
			label.setMaxLines(1);
			this.icon = icon;
			this.index = index;
			setOnClick(new Runnable() {
				@Override
				public void run () {
					select(Item.this.index, true);
				}
			});
		}

		@Override
		public void act (float delta) {
			super.act(delta);
			if (sel.update(delta)) Compat.requestRender();
		}

		@Override
		public void draw (Batch batch, float parentAlpha) {
			Theme t = theme();
			float a = parentAlpha * getColor().a;
			float x = getX(), y = getY(), w = getWidth(), h = getHeight(), v = sel.value;
			float pw = Compat.dp(64), ph = Compat.dp(32), is = Compat.dp(24);
			float py = y + h - Compat.dp(12) - ph, cx = x + w / 2;
			if (v > 0) {
				float cw = pw * (0.4f + 0.6f * v);
				gfx().roundRectSmooth(batch, cx - cw / 2, py, cw, ph, ph / 2, t.secondaryContainer, a * v);
			}
			ripple.draw(batch, gfx(), cx - pw / 2, py, pw, ph, ph / 2, t.onSurface, a);
			Color ic = Views.lerp(t.onSurfaceVariant, t.onSecondaryContainer, v);
			Compat.fonts().drawIcon(batch, v > 0.5f && selectedIcon != 0 ? selectedIcon : icon, cx - is / 2, py + (ph - is) / 2, is,
				1f, ic, a);
			if (badge >= 0) {
				if (badge == 0) gfx().circle(batch, cx + Compat.dp(10), py + ph - Compat.dp(7), Compat.dp(3), t.error, a);
				else {
					if (badgeText == null) badgeText = new TextLayout(Typography.sizePx(Typography.LABEL_SMALL), true);
					badgeText.setText(badge > 99 ? "99+" : Integer.toString(badge));
					badgeText.wrap(1000);
					float bh = Compat.dp(16), bw = Math.max(bh, badgeText.prefWidth() + Compat.dp(8));
					float bx = cx + Compat.dp(4), by = py + ph - Compat.dp(12);
					gfx().roundRect(batch, bx, by, bw, bh, bh / 2, t.error, a);
					badgeText.setAlign(TextLayout.ALIGN_CENTER);
					badgeText.draw(batch, bx, by + (bh + badgeText.height()) / 2, bw, t.onError, a);
				}
			}
			float tw = Math.max(1, w - Compat.dp(4));
			label.wrap(tw);
			label.setAlign(TextLayout.ALIGN_CENTER);
			label.draw(batch, x + (w - tw) / 2, py - Compat.dp(4), tw, Views.lerp(t.onSurfaceVariant, t.onSurface, v), a);
		}
	}

	private final Array<Item> items = new Array<Item>();
	private int selected = -1;
	private OnItemSelectedListener listener;

	public BottomNavigationView () {
		setTouchable(Touchable.enabled);
		setBackground(theme().surfaceContainer);
	}

	public BottomNavigationView addItem (char icon, CharSequence text) {
		Item item = new Item(text, icon, items.size);
		items.add(item);
		addActor(item);
		if (selected < 0) select(0, false);
		return this;
	}

	/** Uses a different (usually filled) icon while the item is selected. */
	public BottomNavigationView setSelectedIcon (int index, char icon) {
		items.get(index).selectedIcon = icon;
		return this;
	}

	/** @param count -1 = no badge, 0 = small dot, otherwise the number */
	public void setBadge (int index, int count) {
		items.get(index).badge = count;
		Compat.requestRender();
	}

	public void setOnItemSelected (OnItemSelectedListener l) {
		listener = l;
	}

	public int getSelectedItem () {
		return selected;
	}

	public void setSelectedItem (int index) {
		select(index, false);
	}

	private void select (int index, boolean fromUser) {
		if (index == selected || index < 0 || index >= items.size) return;
		selected = index;
		for (Item it : items)
			it.sel.animateTo(it.index == index ? 1 : 0, getStage() != null ? 0.2f : 0);
		Compat.requestRender();
		if (listener != null && fromUser) listener.onItemSelected(index);
	}

	@Override
	public float getPrefWidth () {
		return Compat.dp(80) * items.size;
	}

	@Override
	public float getPrefHeight () {
		return Compat.dp(80);
	}

	@Override
	public float measureHeight (float width) {
		return Compat.dp(80);
	}

	@Override
	public void layout () {
		if (items.size == 0) return;
		boolean rtl = Compat.isRtl();
		float w = getWidth() / items.size;
		for (int i = 0; i < items.size; i++)
			place(items.get(i), (rtl ? items.size - 1 - i : i) * w, 0, w, getHeight());
	}
}
