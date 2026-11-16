package com.star4droid.compat.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.star4droid.compat.Compat;

/** Click handling shared by all widgets: ripple, tap slop, cancel when a scroll container takes over, long press, and only
 * the inner-most clickable under the finger reacts (like Android). */
public class PressListener extends ClickListener {
	public static float LONG_PRESS_SECONDS = 0.5f;
	private static InputEvent claimedEvent;
	private static long claimedFrame = -1;
	private static int claimedPointer = -1;

	private final Clickable target;
	private boolean longFired;
	private Timer.Task longTask;

	public PressListener (Clickable target) {
		this.target = target;
		setTapSquareSize(Compat.dp(14));
	}

	@Override
	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
		if (!target.isEnabled()) return false;
		long frame = Gdx.graphics.getFrameId();
		if (claimedEvent == event && claimedFrame == frame && claimedPointer == pointer) return false;
		if (!super.touchDown(event, x, y, pointer, button)) return false;
		claimedEvent = event;
		claimedFrame = frame;
		claimedPointer = pointer;
		longFired = false;
		target.ripple().press(x, y);
		Compat.requestRender();
		cancelLong();
		longTask = Timer.schedule(new Timer.Task() {
			@Override
			public void run () {
				if (isPressed() && target.performLongClick()) {
					longFired = true;
					target.ripple().release();
				}
			}
		}, LONG_PRESS_SECONDS);
		return true;
	}

	@Override
	public void touchDragged (InputEvent event, float x, float y, int pointer) {
		super.touchDragged(event, x, y, pointer);
		if (!isPressed()) {
			cancelLong();
			target.ripple().release();
		}
	}

	@Override
	public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
		boolean cancelled = event.isTouchFocusCancel();
		cancelLong();
		super.touchUp(event, x, y, pointer, button);
		if (cancelled) target.ripple().cancel();
		else target.ripple().release();
		Compat.requestRender();
	}

	@Override
	public void clicked (InputEvent event, float x, float y) {
		if (longFired) return;
		target.performClick();
	}

	private void cancelLong () {
		if (longTask != null) {
			longTask.cancel();
			longTask = null;
		}
	}
}
