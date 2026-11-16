package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.star4droid.compat.BackHandler;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Anim;

/** Main content plus a navigation drawer that slides in from the start edge (swipe from the edge, tap outside or press
 * back to close). */
public class DrawerLayout extends ViewGroup implements BackHandler {
	public interface DrawerListener {
		void onDrawerSlide (float fraction);
	}

	private Actor content, drawer;
	private final Anim open = new Anim(0f);
	private boolean dragging, target, swipeEnabled = true;
	private float startX, startY, lastX;
	private final VelocityTracker tracker = new VelocityTracker();
	private DrawerListener listener;

	public DrawerLayout () {
		setTouchable(Touchable.enabled);
		addCaptureListener(new InputListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != 0 || drawer == null || !swipeEnabled) return false;
				boolean rtl = Compat.isRtl();
				float edge = rtl ? getWidth() - x : x;
				if (open.value <= 0 && edge > Compat.dp(20)) return false;
				dragging = false;
				startX = lastX = event.getStageX();
				startY = event.getStageY();
				tracker.reset();
				tracker.add(lastX);
				return true;
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				float sx = event.getStageX();
				if (!dragging) {
					float dx = sx - startX, dy = event.getStageY() - startY;
					if (Math.abs(dx) <= Compat.dp(8) || Math.abs(dx) < Math.abs(dy)) return;
					dragging = true;
					lastX = sx;
					if (getStage() != null) getStage().cancelTouchFocusExcept(this, DrawerLayout.this);
				}
				tracker.add(sx);
				float d = (sx - lastX) / Math.max(1, drawerWidth());
				lastX = sx;
				setFraction(open.value + (Compat.isRtl() ? -d : d));
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				if (!dragging) return;
				dragging = false;
				float v = tracker.velocity() * (Compat.isRtl() ? -1 : 1);
				boolean toOpen = Math.abs(v) > Compat.dp(400) ? v > 0 : open.value > 0.5f;
				animate(toOpen);
			}
		});
		// taps on the dimmed area close the drawer
		super.addListener(new ClickListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (event.getTarget() != DrawerLayout.this) return false;
				return super.touchDown(event, x, y, pointer, button);
			}

			@Override
			public void clicked (InputEvent event, float x, float y) {
				close();
			}
		});
	}

	public void setContent (Actor a) {
		if (content != null) content.remove();
		content = a;
		addActorAt(0, a);
	}

	/** The drawer view; give it a background (a {@link NavigationView} has one). */
	public void setDrawer (Actor a) {
		if (drawer != null) drawer.remove();
		drawer = a;
		a.setVisible(open.value > 0);
		addActor(a);
	}

	@Override
	protected void childrenChanged () {
		super.childrenChanged();
		// XML: first child = content, second = drawer
		if (content == null && getChildren().size > 0) content = getChildren().get(0);
		if (drawer == null && getChildren().size > 1 && getChildren().get(1) != content) {
			drawer = getChildren().get(1);
			drawer.setVisible(open.value > 0);
		}
	}

	public void open () {
		animate(true);
	}

	public void close () {
		animate(false);
	}

	public void toggle () {
		animate(!target);
	}

	public boolean isOpen () {
		return target;
	}

	public void setSwipeEnabled (boolean value) {
		swipeEnabled = value;
	}

	public void setDrawerListener (DrawerListener l) {
		listener = l;
	}

	private float drawerWidth () {
		return Math.min(Compat.dp(320), getWidth() - Compat.dp(56));
	}

	private void animate (boolean toOpen) {
		target = toOpen;
		open.animateTo(toOpen ? 1 : 0, 0.25f);
		if (drawer != null && toOpen) drawer.setVisible(true);
		Compat.requestRender();
	}

	private void setFraction (float f) {
		f = MathUtils.clamp(f, 0, 1);
		open.set(f);
		target = f > 0.5f;
		applyFraction();
	}

	private void applyFraction () {
		if (drawer == null) return;
		float f = open.value, dw = drawerWidth();
		drawer.setVisible(f > 0);
		drawer.setX(Math.round(Compat.isRtl() ? getWidth() - dw * f : -dw + dw * f));
		Compat.requestRender();
		if (listener != null) listener.onDrawerSlide(f);
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (open.update(delta)) applyFraction();
	}

	@Override
	public boolean onBackPressed () {
		if (target || open.value > 0) {
			close();
			return true;
		}
		return false;
	}

	@Override
	public Actor hit (float x, float y, boolean touchable) {
		if (open.value > 0 && drawer != null) {
			if (touchable && getTouchable() == Touchable.disabled) return null;
			if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return null;
			// while the drawer is out only the drawer itself is touchable; everything else is the scrim
			Actor h = drawer.hit(x - drawer.getX(), y - drawer.getY(), touchable);
			if (h != null) return h;
			if (x >= drawer.getX() && x < drawer.getX() + drawer.getWidth()) return drawer;
			return this;
		}
		Actor h = super.hit(x, y, touchable);
		return h == this ? null : h;
	}

	@Override
	public float getPrefWidth () {
		return content != null ? Measure.prefWidth(content) : 0;
	}

	@Override
	public float getPrefHeight () {
		return content != null ? Measure.prefHeight(content, getWidth()) : 0;
	}

	@Override
	public void layout () {
		float w = getWidth(), h = getHeight();
		if (content != null) place(content, 0, 0, w, h);
		if (drawer != null) {
			float dw = drawerWidth();
			drawer.setSize(Math.round(dw), h);
			drawer.setY(0);
			applyFraction();
			if (drawer instanceof com.badlogic.gdx.scenes.scene2d.utils.Layout)
				((com.badlogic.gdx.scenes.scene2d.utils.Layout)drawer).validate();
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();
		float a = parentAlpha * getColor().a;
		Theme t = theme();
		drawBackground(batch, a);
		float x = getX(), y = getY();
		if (content != null && content.isVisible()) {
			content.setPosition(content.getX() + x, content.getY() + y);
			content.draw(batch, a);
			content.setPosition(content.getX() - x, content.getY() - y);
		}
		if (open.value > 0 && drawer != null) {
			gfx().rect(batch, x, y, getWidth(), getHeight(), t.scrim, a * 0.32f * open.value);
			drawer.setPosition(drawer.getX() + x, drawer.getY() + y);
			drawer.draw(batch, a);
			drawer.setPosition(drawer.getX() - x, drawer.getY() - y);
		}
	}
}
