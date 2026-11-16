package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Gravity;
import com.star4droid.compat.LayoutParams;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.Anim;

/** Top app bar: navigation icon, title and action icons. {@link #setLifted} tints it while content scrolls underneath. */
public class Toolbar extends LinearLayout {
	private final TextView title;
	private final Space spacer = new Space();
	private IconButton nav;
	private final Anim lift = new Anim(0f);
	private final Color tmp = new Color();
	private Color baseColor, liftedColor;

	public Toolbar () {
		this("");
	}

	public Toolbar (CharSequence text) {
		super(HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);
		setPadding(Compat.dp(4), 0, Compat.dp(4), 0);
		setTouchable(Touchable.enabled);
		title = new TextView(text, Typography.TITLE_LARGE);
		title.setSingleLine();
		add(title).margins(Compat.dp(12), 0, Compat.dp(12), 0);
		add(spacer).weight(1).width(0);
	}

	public Toolbar setTitle (CharSequence text) {
		title.setText(text);
		return this;
	}

	public TextView getTitleView () {
		return title;
	}

	public Toolbar setCentered (boolean centered) {
		// centred: the title takes the free space itself; otherwise an invisible spacer pushes the actions to the end
		title.setGravity(centered ? Gravity.CENTER_HORIZONTAL : Gravity.NONE);
		lp(title).weight(centered ? 1 : 0).width(centered ? 0 : LayoutParams.WRAP_CONTENT);
		spacer.setVisible(!centered);
		invalidateHierarchy();
		return this;
	}

	/** Icon at the start of the bar (menu, back arrow...). */
	public Toolbar setNavigationIcon (char icon, Runnable onClick) {
		if (nav == null) {
			nav = new IconButton(icon).iconColor(theme().onSurface);
			addActorAt(0, nav);
		} else nav.icon(icon);
		nav.setOnClick(onClick);
		return this;
	}

	/** Adds an action icon at the end of the bar. */
	public IconButton addAction (char icon, Runnable onClick) {
		IconButton b = new IconButton(icon, onClick);
		add(b);
		return b;
	}

	public Toolbar setColors (Color background, Color lifted, Color content) {
		baseColor = background;
		liftedColor = lifted;
		if (content != null) title.setTextColor(content);
		return this;
	}

	/** true while the content below is scrolled away from its top. */
	public void setLifted (boolean lifted) {
		lift.animateTo(lifted ? 1 : 0, 0.2f);
		Compat.requestRender();
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (lift.update(delta)) Compat.requestRender();
	}

	@Override
	public float getPrefHeight () {
		return Compat.dp(64);
	}

	@Override
	public float measureHeight (float width) {
		return Compat.dp(64);
	}

	@Override
	protected void drawBackground (Batch batch, float alpha) {
		Theme t = theme();
		Color a = baseColor != null ? baseColor : t.surface, b = liftedColor != null ? liftedColor : t.surfaceContainer;
		tmp.set(a).lerp(b, lift.value);
		gfx().rect(batch, getX(), getY(), getWidth(), getHeight(), tmp, alpha);
	}
}
