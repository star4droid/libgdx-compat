package com.star4droid.compat.widget;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Gravity;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;

/** A Material dialog: optional icon, title, message, custom view or a list of items, and up to three buttons.
 *
 * <pre>
 * new AlertDialog().setTitle("Delete?").setMessage("This cannot be undone.")
 * 	.setPositiveButton("Delete", () -> remove()).setNegativeButton("Cancel", null).show(stage);
 * </pre> */
public class AlertDialog extends Overlay {
	public interface OnItemClickListener {
		void onClick (int index);
	}

	private CharSequence title, message, positive, negative, neutral;
	private Runnable onPositive, onNegative, onNeutral;
	private char icon;
	private Actor customView;
	private CharSequence[] items;
	private OnItemClickListener itemListener;
	private LinearLayout card;
	private boolean built;

	public AlertDialog () {
		showSeconds = 0.2f;
		hideSeconds = 0.12f;
	}

	public AlertDialog setTitle (CharSequence t) {
		title = t;
		return this;
	}

	public AlertDialog setMessage (CharSequence m) {
		message = m;
		return this;
	}

	public AlertDialog setIcon (char c) {
		icon = c;
		return this;
	}

	/** Any actor shown between the message and the buttons. */
	public AlertDialog setView (Actor view) {
		customView = view;
		return this;
	}

	/** A simple list of choices; tapping one closes the dialog. */
	public AlertDialog setItems (CharSequence[] items, OnItemClickListener l) {
		this.items = items;
		itemListener = l;
		return this;
	}

	public AlertDialog setPositiveButton (CharSequence text, Runnable r) {
		positive = text;
		onPositive = r;
		return this;
	}

	public AlertDialog setNegativeButton (CharSequence text, Runnable r) {
		negative = text;
		onNegative = r;
		return this;
	}

	public AlertDialog setNeutralButton (CharSequence text, Runnable r) {
		neutral = text;
		onNeutral = r;
		return this;
	}

	public AlertDialog cancelable (boolean value) {
		setCancelable(value);
		return this;
	}

	private Button button (CharSequence text, final Runnable r) {
		return new Button(text, Button.TEXT).onClick(new Runnable() {
			@Override
			public void run () {
				dismiss();
				if (r != null) r.run();
			}
		});
	}

	private void build () {
		built = true;
		Theme t = theme();
		card = new LinearLayout(LinearLayout.VERTICAL);
		card.setBackground(t.surfaceContainerHigh);
		card.setCornerRadius(Compat.dp(t.cornerExtraLarge));
		card.setElevation(Compat.dp(6));
		card.setPadding(0, Compat.dp(24), 0, Compat.dp(positive != null || negative != null || neutral != null ? 16 : 24));
		card.setTouchable(Touchable.enabled);
		float side = Compat.dp(24);
		if (icon != 0) card.add(new IconView(icon, t.secondary)).gravity(Gravity.CENTER_HORIZONTAL).marginBottom(Compat.dp(16));
		if (title != null) {
			TextView tv = new TextView(title, Typography.HEADLINE_SMALL);
			if (icon != 0) tv.setGravity(Gravity.CENTER_HORIZONTAL);
			card.add(tv).matchWidth().margins(side, 0, side, 0);
		}
		if (message != null) {
			TextView tv = new TextView(message, Typography.BODY_MEDIUM);
			tv.setTextColor(t.onSurfaceVariant);
			card.add(tv).matchWidth().margins(side, title != null ? Compat.dp(16) : 0, side, 0);
		}
		if (items != null) {
			LinearLayout list = new LinearLayout(LinearLayout.VERTICAL);
			for (int i = 0; i < items.length; i++) {
				final int index = i;
				LinearLayout row = LinearLayout.horizontal().gravity(Gravity.CENTER_VERTICAL);
				row.setPadding(side, 0, side, 0);
				row.add(new TextView(items[i], Typography.BODY_LARGE)).weight(1).width(0);
				row.setOnClick(new Runnable() {
					@Override
					public void run () {
						dismiss();
						if (itemListener != null) itemListener.onClick(index);
					}
				});
				list.add(row).matchWidth().height(Compat.dp(52));
			}
			ScrollView sv = new ScrollView(list);
			sv.lp(list).matchWidth();
			card.add(sv).matchWidth().marginTop(Compat.dp(12)).weight(1).height(LayoutParams_WRAP);
		}
		if (customView != null) card.add(customView).matchWidth().margins(side, Compat.dp(16), side, 0);
		if (positive != null || negative != null || neutral != null) {
			LinearLayout buttons = LinearLayout.horizontal().spacing(Compat.dp(8)).gravity(Gravity.CENTER_VERTICAL);
			if (neutral != null) buttons.add(button(neutral, onNeutral));
			buttons.add(new Space()).weight(1).width(0);
			if (negative != null) buttons.add(button(negative, onNegative));
			if (positive != null) buttons.add(button(positive, onPositive));
			card.add(buttons).matchWidth().margins(side - Compat.dp(8), Compat.dp(24), side, 0);
		}
		addActor(card);
	}

	private static final float LayoutParams_WRAP = com.star4droid.compat.LayoutParams.WRAP_CONTENT;

	@Override
	protected void onShow () {
		if (!built) build();
		card.setTransform(true);
	}

	@Override
	protected void onHide () {
		card.setTransform(true);
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (card == null) return;
		float v = shown.value;
		if (shown.isRunning() || dismissing) {
			float s = 0.9f + 0.1f * Interpolation.pow2Out.apply(v);
			card.setOrigin(Align.center);
			card.setScale(s);
			card.getColor().a = Math.min(1f, v * 1.4f);
		} else if (card.isTransform()) {
			// back to the cheap, matrix-free path once the animation is over
			card.setScale(1f);
			card.getColor().a = 1f;
			card.setTransform(false);
		}
	}

	@Override
	public void layout () {
		if (card == null) return;
		float w = getWidth(), h = getHeight() - Compat.getKeyboardHeight();
		float cw = Math.min(Compat.dp(560), Math.max(Math.min(Compat.dp(280), w), w - Compat.dp(48)));
		float ch = Math.min(card.measureHeight(cw), h - Compat.dp(48));
		place(card, (w - cw) / 2, Compat.getKeyboardHeight() + (h - ch) / 2, cw, ch);
	}
}
