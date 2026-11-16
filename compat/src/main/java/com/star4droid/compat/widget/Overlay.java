package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.star4droid.compat.BackHandler;
import com.star4droid.compat.Compat;
import com.star4droid.compat.graphics.Anim;

/** Base of everything that floats above the UI (bottom sheets, dialogs, menus). Covers the stage, dims what is behind,
 * closes on outside tap / back key, and animates in and out. */
public abstract class Overlay extends ViewGroup implements BackHandler {
	protected final Anim shown = new Anim(0f);
	protected boolean dismissing, cancelable = true, modal = true;
	protected float scrimAlpha = 0.32f, showSeconds = 0.25f, hideSeconds = 0.18f;
	private Runnable onDismiss;

	protected Overlay () {
		setFillParent(true);
		setTouchable(Touchable.enabled);
		super.addListener(new ClickListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (event.getTarget() != Overlay.this) return false;
				return super.touchDown(event, x, y, pointer, button);
			}

			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (cancelable) onOutsideTap();
			}
		});
	}

	protected void onOutsideTap () {
		dismiss();
	}

	/** Whether tapping outside or pressing back closes it. */
	public void setCancelable (boolean value) {
		cancelable = value;
	}

	public void setOnDismiss (Runnable r) {
		onDismiss = r;
	}

	public boolean isShowing () {
		return getStage() != null && !dismissing;
	}

	public void show (Stage stage) {
		dismissing = false;
		if (getStage() != stage) stage.addActor(this);
		toFront();
		setTouchable(modal ? Touchable.enabled : Touchable.childrenOnly);
		shown.set(0);
		shown.animateTo(1, showSeconds);
		onShow();
		Compat.requestRender();
	}

	public void dismiss () {
		if (dismissing || getStage() == null) return;
		dismissing = true;
		setTouchable(Touchable.disabled);
		shown.animateTo(0, hideSeconds);
		onHide();
		Compat.requestRender();
	}

	protected void onShow () {
	}

	protected void onHide () {
	}

	/** @return true once the closing animation has finished. */
	protected boolean isHidden () {
		return !shown.isRunning();
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (shown.update(delta)) Compat.requestRender();
		if (dismissing && isHidden()) {
			dismissing = false;
			remove();
			if (onDismiss != null) onDismiss.run();
		}
	}

	@Override
	public boolean onBackPressed () {
		if (!modal) return false;
		if (cancelable) dismiss();
		return true;
	}

	protected float scrimFraction () {
		return shown.value;
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();
		if (modal && scrimAlpha > 0) {
			float a = parentAlpha * scrimAlpha * scrimFraction();
			if (a > 0.003f) gfx().rect(batch, getX(), getY(), getWidth(), getHeight(), theme().scrim, a);
		}
		drawChildren(batch, parentAlpha);
	}
}
