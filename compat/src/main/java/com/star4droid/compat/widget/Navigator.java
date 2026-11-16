package com.star4droid.compat.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.star4droid.compat.BackHandler;
import com.star4droid.compat.Compat;
import com.star4droid.compat.graphics.Anim;

/** A back stack of screens with slide transitions; the back key pops the top screen.
 *
 * <pre>
 * Navigator nav = new Navigator();
 * nav.setFillParent(true);
 * stage.addActor(nav);
 * nav.push(new HomeScreen());
 * </pre> */
public class Navigator extends ViewGroup implements BackHandler {
	/** One screen of the app. Build the UI in {@link #onCreateView()}. */
	public abstract static class Screen {
		protected Navigator navigator;
		Actor view;

		protected abstract Actor onCreateView ();

		/** The screen became the visible one. */
		protected void onShow () {
		}

		/** Another screen was pushed on top or this one is being removed. */
		protected void onHide () {
		}

		/** The screen left the stack for good. */
		protected void onDestroy () {
		}

		/** @return true to consume the back key instead of popping this screen. */
		protected boolean onBackPressed () {
			return false;
		}

		public Actor getView () {
			return view;
		}

		public Navigator getNavigator () {
			return navigator;
		}
	}

	private final Array<Screen> stack = new Array<Screen>();
	private final Anim transition = new Anim(1f);
	private Screen entering, leaving;
	private boolean popping;

	public Navigator () {
		setTouchable(Touchable.childrenOnly);
	}

	public int size () {
		return stack.size;
	}

	public Screen current () {
		return stack.size > 0 ? stack.peek() : null;
	}

	public void push (Screen screen) {
		finishTransition();
		Screen old = current();
		screen.navigator = this;
		screen.view = screen.onCreateView();
		stack.add(screen);
		addActor(screen.view);
		if (old != null) old.onHide();
		start(screen, old, false, false);
		screen.onShow();
	}

	/** @return false when there is nothing to pop (only one screen left). */
	public boolean pop () {
		if (stack.size <= 1) return false;
		finishTransition();
		Screen top = stack.pop();
		Screen below = stack.peek();
		top.onHide();
		below.view.setVisible(true);
		start(below, top, true, true);
		below.onShow();
		return true;
	}

	/** Replaces the top screen. */
	public void replace (Screen screen) {
		finishTransition();
		Screen old = stack.size > 0 ? stack.pop() : null;
		screen.navigator = this;
		screen.view = screen.onCreateView();
		stack.add(screen);
		addActor(screen.view);
		if (old != null) old.onHide();
		// the replaced screen is destroyed when the transition ends
		start(screen, old, false, true);
		screen.onShow();
	}

	public void popToRoot () {
		finishTransition();
		while (stack.size > 1) {
			Screen s = stack.pop();
			s.onHide();
			s.view.remove();
			s.onDestroy();
		}
		if (stack.size > 0) {
			stack.peek().view.setVisible(true);
			stack.peek().view.getColor().a = 1;
			stack.peek().onShow();
		}
		invalidate();
	}

	private boolean destroyLeaving;

	private void start (Screen in, Screen out, boolean pop, boolean destroyOut) {
		entering = in;
		leaving = out;
		popping = pop;
		destroyLeaving = destroyOut;
		if (out == null || getStage() == null || getWidth() <= 0) {
			transition.set(1);
			finishTransition();
			return;
		}
		transition.set(0);
		transition.animateTo(1, 0.26f);
		setTouchable(Touchable.disabled);
		if (pop) leaving.view.toFront();
		invalidate();
		Compat.requestRender();
	}

	private void finishTransition () {
		transition.set(1);
		if (leaving != null) {
			if (destroyLeaving) {
				leaving.view.remove();
				leaving.onDestroy();
			} else leaving.view.setVisible(false);
		}
		if (entering != null) {
			entering.view.setVisible(true);
			entering.view.getColor().a = 1;
		}
		entering = leaving = null;
		destroyLeaving = false;
		setTouchable(Touchable.childrenOnly);
		invalidate();
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (entering != null) {
			transition.update(delta);
			if (!transition.isRunning()) finishTransition();
			else invalidate();
			Compat.requestRender();
		}
	}

	@Override
	public boolean onBackPressed () {
		Screen s = current();
		if (s == null) return false;
		if (s.onBackPressed()) return true;
		return pop();
	}

	@Override
	public void invalidateHierarchy () {
		invalidate();
	}

	@Override
	public float getPrefWidth () {
		return 0;
	}

	@Override
	public float getPrefHeight () {
		return 0;
	}

	@Override
	public void layout () {
		float w = getWidth(), h = getHeight(), t = transition.value;
		float dir = Compat.isRtl() ? -1 : 1;
		for (int i = 0; i < stack.size; i++) {
			Screen s = stack.get(i);
			if (s == entering || s == leaving || !s.view.isVisible()) continue;
			place(s.view, 0, 0, w, h);
		}
		if (entering != null) {
			float shift = Compat.dp(48);
			if (!popping) {
				// push: the new screen slides in over the old one
				place(entering.view, dir * (1 - t) * shift, 0, w, h);
				entering.view.getColor().a = Math.min(1, t * 1.6f);
				if (leaving != null) {
					place(leaving.view, 0, 0, w, h);
					leaving.view.getColor().a = 1;
				}
			} else {
				place(entering.view, 0, 0, w, h);
				entering.view.getColor().a = 1;
				if (leaving != null) {
					place(leaving.view, dir * t * shift, 0, w, h);
					leaving.view.getColor().a = Math.max(0, 1 - t * 1.6f);
				}
			}
		}
	}
}
