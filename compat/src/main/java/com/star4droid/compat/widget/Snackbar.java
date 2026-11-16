package com.star4droid.compat.widget;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.ObjectMap;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Gravity;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.Anim;

/** A brief message at the bottom of the screen with an optional action.
 *
 * <pre>
 * Snackbar.make(stage, "Message deleted", Snackbar.LENGTH_LONG).setAction("Undo", () -> restore()).show();
 * </pre> */
public class Snackbar extends LinearLayout {
	public static final float LENGTH_SHORT = 2f, LENGTH_LONG = 3.5f, LENGTH_INDEFINITE = -1f;
	private static final ObjectMap<Stage, Snackbar> CURRENT = new ObjectMap<Stage, Snackbar>();

	private final Stage stage;
	private final float duration;
	private float time, bottomOffset;
	private final Anim shown = new Anim(0f);
	private boolean dismissing;
	private final TextView text;

	private Snackbar (Stage stage, CharSequence message, float duration) {
		super(HORIZONTAL);
		this.stage = stage;
		this.duration = duration;
		Theme t = theme();
		setBackground(t.inverseSurface);
		setCornerRadius(Compat.dp(t.cornerExtraSmall));
		setElevation(Compat.dp(4));
		setGravity(Gravity.CENTER_VERTICAL);
		setPadding(Compat.dp(16), Compat.dp(6), Compat.dp(8), Compat.dp(6));
		setTouchable(Touchable.enabled);
		text = new TextView(message, Typography.BODY_MEDIUM);
		text.setTextColor(t.inverseOnSurface);
		text.setMaxLines(2);
		add(text).weight(1).width(0).margins(0, Compat.dp(8), Compat.dp(8), Compat.dp(8));
	}

	public static Snackbar make (Stage stage, CharSequence message, float durationSeconds) {
		return new Snackbar(stage, message, durationSeconds);
	}

	public Snackbar setAction (CharSequence label, final Runnable r) {
		Button b = new Button(label, Button.TEXT).colors(null, theme().inversePrimary).onClick(new Runnable() {
			@Override
			public void run () {
				dismiss();
				if (r != null) r.run();
			}
		});
		add(b);
		return this;
	}

	/** Lifts the snackbar above a bottom bar or FAB (pixels from the bottom of the screen). */
	public Snackbar setBottomOffset (float px) {
		bottomOffset = px;
		return this;
	}

	public void show () {
		Snackbar old = CURRENT.get(stage);
		if (old != null) old.dismiss();
		CURRENT.put(stage, this);
		stage.addActor(this);
		shown.animateTo(1, 0.2f);
		Compat.requestRender();
	}

	public void dismiss () {
		if (dismissing) return;
		dismissing = true;
		shown.animateTo(0, 0.15f);
		if (CURRENT.get(stage) == this) CURRENT.remove(stage);
		Compat.requestRender();
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (shown.update(delta)) Compat.requestRender();
		if (!dismissing && duration > 0) {
			time += delta;
			if (time >= duration) dismiss();
			else Compat.requestRender();
		}
		if (dismissing && !shown.isRunning()) {
			remove();
			return;
		}
		float sw = stage.getWidth(), m = Compat.dp(8);
		float w = Math.min(sw - 2 * m, Compat.dp(600));
		float h = measureHeight(w);
		float y = bottomOffset + Compat.getKeyboardHeight() + m - (1 - shown.value) * Compat.dp(24);
		if (getWidth() != w || getHeight() != h || getX() != Math.round((sw - w) / 2) || getY() != Math.round(y))
			setBounds(Math.round((sw - w) / 2), Math.round(y), w, h);
		getColor().a = shown.value;
	}
}
