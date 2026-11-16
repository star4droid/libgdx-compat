package com.star4droid.compat.widget;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Gfx;
import com.star4droid.compat.graphics.Ripple;

/** Base class of the leaf widgets. Each widget draws itself directly (no child actors), which keeps the scene graph small. */
public class View extends Widget implements Clickable {
	protected final Ripple ripple = new Ripple();
	protected Runnable onClick, onLongClick;
	private PressListener press;
	private boolean enabled = true;

	protected static Gfx gfx () {
		return Compat.gfx();
	}

	protected static Theme theme () {
		return Compat.theme;
	}

	protected static float dp (float v) {
		return Compat.dp(v);
	}

	public void setOnClick (Runnable r) {
		onClick = r;
		ensurePressListener();
	}

	public void setOnLongClick (Runnable r) {
		onLongClick = r;
		ensurePressListener();
	}

	protected void ensurePressListener () {
		if (press == null) addListener(press = new PressListener(this));
	}

	/** @return true while the finger is down on this view. */
	public boolean isPressed () {
		return press != null && press.isVisualPressed();
	}

	public void setEnabled (boolean value) {
		enabled = value;
		setTouchable(value ? Touchable.enabled : Touchable.disabled);
		Compat.requestRender();
	}

	@Override
	public boolean isEnabled () {
		return enabled;
	}

	@Override
	public Ripple ripple () {
		return ripple;
	}

	@Override
	public void performClick () {
		if (onClick != null) onClick.run();
	}

	@Override
	public boolean performLongClick () {
		if (onLongClick == null) return false;
		onLongClick.run();
		return true;
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (ripple.act(delta)) Compat.requestRender();
	}

	/** Sets the id used by {@code findActor} / XML lookups. */
	public View id (String id) {
		setName(id);
		return this;
	}
}
