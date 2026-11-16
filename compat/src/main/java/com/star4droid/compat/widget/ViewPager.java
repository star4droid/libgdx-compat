package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.star4droid.compat.Compat;
import com.star4droid.compat.graphics.Anim;

/** Swipeable pages with snap. Pages may be added lazily with a {@link PageFactory}; only the visible page(s) are laid out,
 * drawn and touchable. Connect a {@link TabLayout} with {@link TabLayout#setupWithViewPager}. */
public class ViewPager extends ViewGroup {
	public interface OnPageChangeListener {
		/** @param position fractional page index while swiping (1.5 = half way between page 1 and 2) */
		void onPageScrolled (float position);

		void onPageSelected (int index);
	}

	public interface PageFactory {
		Actor create (int index);
	}

	private final Array<Actor> pages = new Array<Actor>();
	private final Array<PageFactory> factories = new Array<PageFactory>();
	private final Array<OnPageChangeListener> listeners = new Array<OnPageChangeListener>();
	private float position;
	private int current;
	private final Anim anim = new Anim();
	private boolean settling, dragging, swipeEnabled = true;
	private float startX, startY, lastX;
	private final VelocityTracker tracker = new VelocityTracker();

	public ViewPager () {
		setTouchable(Touchable.enabled);
		super.addListener(new InputListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != 0 || button != 0 || !swipeEnabled || pages.size < 2) return false;
				dragging = false;
				if (settling) {
					// catch a page that is still sliding
					settling = false;
					dragging = true;
					if (getStage() != null) getStage().cancelTouchFocusExcept(this, ViewPager.this);
				}
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
					if (Math.abs(dx) <= Compat.dp(10) || Math.abs(dx) < Math.abs(dy) * 1.2f) return;
					float dir = pageDelta(dx);
					if ((dir > 0 && position >= pages.size - 1) || (dir < 0 && position <= 0)) return;
					dragging = true;
					lastX = sx;
					if (getStage() != null) getStage().cancelTouchFocusExcept(this, ViewPager.this);
				}
				tracker.add(sx);
				setPosition(position + pageDelta(sx - lastX));
				lastX = sx;
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				if (!dragging) return;
				dragging = false;
				float v = pageDelta(tracker.velocity()); // pages per second
				int target;
				if (v > 1.2f) target = (int)Math.floor(position) + 1;
				else if (v < -1.2f) target = (int)Math.ceil(position) - 1;
				else target = Math.round(position);
				settle(MathUtils.clamp(target, 0, pages.size - 1), 0.28f);
			}
		});
	}

	/** Finger movement in pixels to page units. */
	private float pageDelta (float dx) {
		float w = Math.max(1, getWidth());
		return (Compat.isRtl() ? dx : -dx) / w;
	}

	public void addPage (Actor page) {
		pages.add(page);
		factories.add(null);
		page.setVisible(false);
		addActor(page);
	}

	/** Adds a page that is only built when the user first gets near it. */
	public void addPage (PageFactory factory) {
		pages.add(null);
		factories.add(factory);
		invalidate();
	}

	public int getPageCount () {
		return pages.size;
	}

	public int getCurrentItem () {
		return current;
	}

	/** Fractional position. */
	public float getPosition () {
		return position;
	}

	public void setCurrentItem (int index) {
		setCurrentItem(index, true);
	}

	public void setCurrentItem (int index, boolean smooth) {
		index = MathUtils.clamp(index, 0, Math.max(0, pages.size - 1));
		if (smooth && getStage() != null && getWidth() > 0) settle(index, 0.3f);
		else {
			settling = false;
			setPosition(index);
		}
	}

	public void setSwipeEnabled (boolean value) {
		swipeEnabled = value;
	}

	public void addOnPageChangeListener (OnPageChangeListener l) {
		listeners.add(l);
	}

	private void settle (int target, float seconds) {
		anim.set(position);
		anim.animateTo(target, seconds);
		settling = anim.isRunning();
		if (!settling) setPosition(target);
		Compat.requestRender();
	}

	private void setPosition (float p) {
		p = MathUtils.clamp(p, 0, Math.max(0, pages.size - 1));
		if (p == position) return;
		position = p;
		invalidate();
		Compat.requestRender();
		for (int i = 0; i < listeners.size; i++)
			listeners.get(i).onPageScrolled(p);
		int nearest = Math.round(p);
		if (nearest != current) {
			current = nearest;
			for (int i = 0; i < listeners.size; i++)
				listeners.get(i).onPageSelected(nearest);
		}
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (settling) {
			anim.update(delta);
			setPosition(anim.value);
			if (!anim.isRunning()) settling = false;
			Compat.requestRender();
		}
	}

	// the pager's size does not depend on its pages
	@Override
	public void invalidateHierarchy () {
		invalidate();
	}

	@Override
	public float getPrefWidth () {
		return Compat.dp(200);
	}

	@Override
	public float getPrefHeight () {
		return Compat.dp(200);
	}

	@Override
	public void layout () {
		float w = getWidth(), h = getHeight();
		boolean rtl = Compat.isRtl();
		for (int i = 0; i < pages.size; i++) {
			float d = i - position;
			boolean visible = Math.abs(d) < 1f;
			Actor page = pages.get(i);
			if (page == null) {
				if (!visible) continue;
				page = factories.get(i).create(i);
				pages.set(i, page);
				addActor(page);
			}
			page.setVisible(visible);
			if (visible) place(page, (rtl ? -d : d) * w, 0, w, h);
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();
		drawBackground(batch, parentAlpha * getColor().a);
		boolean between = position != Math.round(position);
		if (between) {
			batch.flush();
			if (!clipBegin(getX(), getY(), getWidth(), getHeight())) return;
		}
		drawChildren(batch, parentAlpha);
		if (between) {
			batch.flush();
			clipEnd();
		}
	}
}
