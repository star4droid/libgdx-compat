package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Anim;
import com.star4droid.compat.graphics.Gfx;

/** Shared scrolling behaviour (drag, fling, mouse wheel, scroll bar, pull-to-refresh, nested-scroll hand-off) for
 * {@link ScrollView}, {@link RecyclerView} and {@link TabLayout}. */
public abstract class ScrollContainer extends ViewGroup {
	public interface OnScrollListener {
		/** @param scroll current offset in pixels, @param delta change since the last call */
		void onScrolled (ScrollContainer view, float scroll, float delta);
	}

	/** How quickly a fling slows down (higher = shorter flings). */
	public static float FLING_FRICTION = 2.4f;

	protected final boolean vertical;
	protected float scroll, maxScroll;
	private boolean scrollEnabled = true, scrollbarEnabled = true;

	private float velocity;
	private boolean flinging, dragging, touching;
	private final Anim smooth = new Anim();
	private boolean smoothing;
	private float lastMain, startMain, startCross;
	private final VelocityTracker tracker = new VelocityTracker();
	private float barAlpha, barIdle;
	private OnScrollListener scrollListener;

	// pull to refresh
	private Runnable onRefresh;
	private boolean refreshing, pulling;
	private final Anim pull = new Anim(0f);
	private float spin;

	protected ScrollContainer (boolean vertical) {
		this.vertical = vertical;
		setTouchable(Touchable.enabled);
		super.addListener(new InputListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != 0 || button != 0 || !scrollEnabled) return false;
				touching = true;
				dragging = false;
				pulling = false;
				if (flinging || smoothing) {
					// a touch stops a running fling and must not click what is under the finger
					flinging = smoothing = false;
					if (getStage() != null) getStage().cancelTouchFocusExcept(this, ScrollContainer.this);
				}
				startMain = lastMain = main(event);
				startCross = cross(event);
				tracker.reset();
				tracker.add(lastMain);
				return true;
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				float m = main(event);
				if (!dragging) {
					float dm = m - startMain, dc = cross(event) - startCross;
					if (Math.abs(dm) <= touchSlop() || Math.abs(dm) < Math.abs(dc)) return;
					int dir = toScroll(dm) > 0 ? 1 : -1;
					boolean canPull = onRefresh != null && dir < 0 && scroll <= 0.5f && !refreshing;
					if (!canScroll(dir) && !canPull) return;
					dragging = true;
					pulling = canPull;
					lastMain = m;
					if (getStage() != null) getStage().cancelTouchFocusExcept(this, ScrollContainer.this);
				}
				float d = toScroll(m - lastMain);
				lastMain = m;
				tracker.add(m);
				if (pulling) {
					pull.set(MathUtils.clamp(pull.value - d * 0.5f, 0, Compat.dp(92)));
					if (pull.value <= 0 && d > 0) {
						pulling = false;
						scrollBy(d);
					}
					Compat.requestRender();
				} else {
					scrollBy(d);
				}
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				touching = false;
				if (!dragging) return;
				dragging = false;
				if (pulling) {
					pulling = false;
					if (pull.value >= Compat.dp(64)) {
						refreshing = true;
						pull.animateTo(Compat.dp(64), 0.15f);
						onRefresh.run();
					} else pull.animateTo(0, 0.2f);
					return;
				}
				float v = toScroll(tracker.velocity());
				if (Math.abs(v) > Compat.dp(60)) fling(v);
			}

			@Override
			public boolean scrolled (InputEvent event, float x, float y, float amountX, float amountY) {
				if (!scrollEnabled || maxScroll <= 0) return false;
				flinging = false;
				scrollBy((vertical ? amountY : (amountX != 0 ? amountX : amountY)) * Compat.dp(64));
				return true;
			}

			@Override
			public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				if (pointer == -1 && getStage() != null && vertical) getStage().setScrollFocus(ScrollContainer.this);
			}
		});
	}

	private float main (InputEvent e) {
		return vertical ? e.getStageY() : e.getStageX();
	}

	private float cross (InputEvent e) {
		return vertical ? e.getStageX() : e.getStageY();
	}

	/** Converts a finger movement along the main axis into a scroll change. */
	private float toScroll (float d) {
		return vertical ? d : (Compat.isRtl() ? d : -d);
	}

	protected float touchSlop () {
		return Compat.dp(8);
	}

	// ------------------------------------------------------------ public API

	public float getScroll () {
		return scroll;
	}

	public float getMaxScroll () {
		return maxScroll;
	}

	public void setScroll (float value) {
		float v = MathUtils.clamp(value, 0, Math.max(0, maxScroll));
		if (v == scroll) return;
		float old = scroll;
		scroll = v;
		barAlpha = 1;
		barIdle = 0;
		invalidate();
		Compat.requestRender();
		if (scrollListener != null) scrollListener.onScrolled(this, scroll, scroll - old);
	}

	/** For subclasses that move {@link #scroll} themselves. */
	protected void notifyScrolled (float old) {
		barAlpha = 1;
		barIdle = 0;
		if (scrollListener != null && old != scroll) scrollListener.onScrolled(this, scroll, scroll - old);
	}

	public void scrollBy (float delta) {
		setScroll(scroll + delta);
	}

	public void smoothScrollTo (float value) {
		flinging = false;
		smooth.set(scroll);
		smooth.animateTo(MathUtils.clamp(value, 0, Math.max(0, maxScroll)), 0.3f);
		smoothing = true;
		Compat.requestRender();
	}

	/** Starts a fling with the given velocity in pixels per second (positive scrolls forward). */
	public void fling (float pixelsPerSecond) {
		velocity = MathUtils.clamp(pixelsPerSecond, -Compat.dp(9000), Compat.dp(9000));
		flinging = true;
		smoothing = false;
		Compat.requestRender();
	}

	public void stopScroll () {
		flinging = smoothing = false;
	}

	/** @param direction +1 = towards the end, -1 = towards the start */
	public boolean canScroll (int direction) {
		return direction > 0 ? scroll < maxScroll - 0.5f : scroll > 0.5f;
	}

	public boolean isVertical () {
		return vertical;
	}

	public void setScrollEnabled (boolean value) {
		scrollEnabled = value;
	}

	public void setScrollbarEnabled (boolean value) {
		scrollbarEnabled = value;
	}

	public void setOnScroll (OnScrollListener l) {
		scrollListener = l;
	}

	/** Enables pull-to-refresh. Call {@link #setRefreshing(boolean) setRefreshing(false)} when loading is done. */
	public void setOnRefresh (Runnable r) {
		onRefresh = r;
	}

	public void setRefreshing (boolean value) {
		refreshing = value;
		pull.animateTo(value ? Compat.dp(64) : 0, 0.25f);
		Compat.requestRender();
	}

	public boolean isRefreshing () {
		return refreshing;
	}

	// ------------------------------------------------------------ internals

	protected float viewportLength () {
		return vertical ? getHeight() : getWidth();
	}

	/** Total length of the content along the scroll axis. */
	protected abstract float computeContentLength ();

	/** Positions the children for the current {@link #scroll}. */
	protected abstract void layoutContent ();

	/** Extra drawing above the children but still clipped (indicators, dividers). */
	protected void drawOverContent (Batch batch, float alpha) {
	}

	@Override
	public void layout () {
		float content = computeContentLength();
		maxScroll = Math.max(0, content - viewportLength());
		if (scroll > maxScroll) scroll = maxScroll;
		if (scroll < 0) scroll = 0;
		layoutContent();
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (flinging) {
			float before = scroll;
			scrollBy(velocity * delta);
			velocity *= (float)Math.exp(-FLING_FRICTION * delta);
			if (Math.abs(velocity) < Compat.dp(40) || scroll == before) flinging = false;
			Compat.requestRender();
		}
		if (smoothing) {
			if (smooth.update(delta)) setScroll(smooth.value);
			if (!smooth.isRunning()) smoothing = false;
			Compat.requestRender();
		}
		if (barAlpha > 0 && !touching && !flinging) {
			barIdle += delta;
			if (barIdle > 0.6f) barAlpha = Math.max(0, barAlpha - delta * 3f);
			Compat.requestRender();
		}
		if (pull.update(delta)) Compat.requestRender();
		if (refreshing && Views.isShown(this)) {
			spin += delta;
			Compat.requestRender();
		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();
		float a = parentAlpha * getColor().a;
		drawBackground(batch, a);
		boolean needClip = maxScroll > 0 || pull.value > 0 || clip;
		if (needClip) {
			batch.flush();
			if (!clipBegin(getX(), getY(), getWidth(), getHeight())) return;
		}
		drawChildren(batch, parentAlpha);
		drawOverContent(batch, a);
		drawScrollbar(batch, a);
		if (pull.value > 0) drawRefresh(batch, a);
		if (needClip) {
			batch.flush();
			clipEnd();
		}
	}

	private void drawScrollbar (Batch batch, float a) {
		if (!scrollbarEnabled || barAlpha <= 0 || maxScroll <= 0) return;
		Theme t = theme();
		float view = viewportLength(), content = view + maxScroll;
		float len = Math.max(Compat.dp(32), view * view / content), pos = (view - len) * (scroll / maxScroll);
		float th = Compat.dp(4), m = Compat.dp(2);
		float alpha = a * barAlpha * 0.35f;
		if (vertical) {
			float x = Compat.isRtl() ? getX() + m : getX() + getWidth() - th - m;
			gfx().roundRect(batch, x, getY() + getHeight() - pos - len, th, len, th / 2, t.onSurface, alpha);
		} else {
			float x = Compat.isRtl() ? getX() + getWidth() - pos - len : getX() + pos;
			gfx().roundRect(batch, x, getY() + m, len, th, th / 2, t.onSurface, alpha);
		}
	}

	private void drawRefresh (Batch batch, float a) {
		Theme t = theme();
		Gfx g = gfx();
		float d = Compat.dp(40), p = pull.value, threshold = Compat.dp(64);
		float cx = getX() + getWidth() / 2, cy = getY() + getHeight() + d / 2 - p;
		float f = Math.min(1, p / threshold);
		g.shadow(batch, cx - d / 2, cy - d / 2, d, d, d / 2, Compat.dp(3), t.shadow, a * f);
		g.circle(batch, cx, cy, d / 2, t.surfaceContainerHigh, a);
		float r = Compat.dp(9), th = Compat.dp(2.5f);
		if (refreshing) {
			float cycle = 1.3f, q = (spin % cycle) / cycle;
			float sweep = 30 + 240 * (q < 0.5f ? Interpolation.pow2.apply(q * 2) : 1 - Interpolation.pow2.apply(q * 2 - 1));
			float base = -spin * 240f - (q >= 0.5f ? 240 * Interpolation.pow2.apply(q * 2 - 1) : 0)
				- 240f * (float)Math.floor(spin / cycle);
			g.arc(batch, cx, cy, r, th, base, -sweep, t.primary, a, true);
		} else {
			g.arc(batch, cx, cy, r, th, 90 - p * 1.5f, -300 * f, t.primary, a * (0.4f + 0.6f * f), true);
		}
	}
}
