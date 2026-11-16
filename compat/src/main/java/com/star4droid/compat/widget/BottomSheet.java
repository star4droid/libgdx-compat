package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.star4droid.compat.Compat;
import com.star4droid.compat.LayoutParams;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Anim;

/** A sheet that slides up from the bottom and follows the finger: drag or fling it between hidden, collapsed (peek) and
 * expanded. A list inside scrolls normally and hands the gesture over to the sheet when it reaches its top.
 * <p>
 * Modal sheets dim the screen and close on outside tap / back; a non-modal ("persistent") sheet stays at its peek height
 * and lets touches through to the screen behind. */
public class BottomSheet extends Overlay {
	public static final int HIDDEN = 0, COLLAPSED = 1, EXPANDED = 2;

	public interface Callback {
		void onStateChanged (BottomSheet sheet, int state);

		/** @param visibleHeight pixels of the sheet currently on screen */
		void onSlide (BottomSheet sheet, float visibleHeight);
	}

	private final Actor content;
	private final Sheet sheet = new Sheet();
	private final Anim visible = new Anim(0f);
	private float peekHeight = -1, sheetH, peekH, expandedH;
	private boolean hideable = true, dragHandle = true, fullHeight, draggable = true;
	private int state = HIDDEN;
	private Callback callback;

	private boolean dragging;
	private float startY, startX, lastY;
	private Actor downTarget;
	private final VelocityTracker tracker = new VelocityTracker();

	private class Sheet extends ViewGroup {
		Sheet () {
			setTouchable(Touchable.enabled);
		}

		@Override
		public void layout () {
			if (content == null) return;
			float top = getHeight() - (dragHandle ? Compat.dp(36) : 0);
			place(content, 0, 0, getWidth(), Math.max(0, top));
		}

		@Override
		public void draw (Batch batch, float parentAlpha) {
			Theme t = theme();
			float a = parentAlpha * getColor().a;
			float r = Compat.dp(t.cornerExtraLarge);
			float x = getX(), y = getY(), w = getWidth(), h = getHeight();
			gfx().shadow(batch, x, y, w, h + r, r, Compat.dp(8), t.shadow, a * (modal ? 0.6f : 1f));
			gfx().roundRect(batch, x, y, w, h, r, r, 0, 0, background != null ? background : t.surfaceContainerLow, a);
			if (dragHandle) {
				float hw = Compat.dp(32), hh = Compat.dp(4);
				gfx().roundRect(batch, x + (w - hw) / 2, y + h - Compat.dp(16) - hh, hw, hh, hh / 2, t.onSurfaceVariant, a * 0.4f);
			}
			validate();
			drawChildren(batch, parentAlpha);
		}
	}

	public BottomSheet (Actor content) {
		this.content = content;
		sheet.addActor(content);
		addActor(sheet);
		addCaptureListener(new InputListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer != 0 || !draggable) return false;
				if (y > sheet.getY() + sheet.getHeight() || x < sheet.getX() || x > sheet.getX() + sheet.getWidth()) return false;
				dragging = false;
				downTarget = event.getTarget();
				startX = event.getStageX();
				startY = lastY = event.getStageY();
				tracker.reset();
				tracker.add(lastY);
				return true;
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				float sy = event.getStageY();
				if (!dragging) {
					float dy = sy - startY, dx = event.getStageX() - startX;
					if (Math.abs(dy) <= Compat.dp(8) || Math.abs(dy) < Math.abs(dx)) return;
					if (dy > 0) {
						// finger moves up: first expand the sheet, then let the content scroll
						if (visible.value >= expandedH - 0.5f) return;
					} else if (innerCanScrollUp()) return;
					dragging = true;
					lastY = sy;
					if (getStage() != null) getStage().cancelTouchFocusExcept(this, BottomSheet.this);
				}
				tracker.add(sy);
				setVisibleHeight(MathUtils.clamp(visible.value + (sy - lastY), 0, expandedH));
				lastY = sy;
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				downTarget = null;
				if (!dragging) return;
				dragging = false;
				float v = tracker.velocity(), h = visible.value, fast = Compat.dp(700);
				int target;
				if (v > fast) target = h > peekH + 1 || peekH >= expandedH ? EXPANDED : (h < peekH ? COLLAPSED : EXPANDED);
				else if (v < -fast) target = h > peekH + 1 ? COLLAPSED : (hideable ? HIDDEN : COLLAPSED);
				else if (h < peekH * 0.5f && hideable) target = HIDDEN;
				else target = h > (peekH + expandedH) / 2 ? EXPANDED : COLLAPSED;
				if (target == COLLAPSED && peekH >= expandedH) target = EXPANDED;
				settle(target);
			}
		});
	}

	/** True when a vertical scroller between the touched actor and the sheet still has content above. */
	private boolean innerCanScrollUp () {
		for (Actor a = downTarget; a != null && a != sheet; a = a.getParent())
			if (a instanceof ScrollContainer && ((ScrollContainer)a).isVertical() && ((ScrollContainer)a).canScroll(-1)) return true;
		return false;
	}

	// ------------------------------------------------------------ options

	/** Height of the collapsed state in pixels. Default: the whole sheet when it is short, otherwise half the screen. */
	public BottomSheet setPeekHeight (float px) {
		peekHeight = px;
		invalidate();
		return this;
	}

	/** false = persistent sheet: no dimming, touches outside reach the screen, never fully hidden by dragging. */
	public BottomSheet setModal (boolean value) {
		modal = value;
		if (!value) hideable = false;
		return this;
	}

	public BottomSheet setHideable (boolean value) {
		hideable = value;
		return this;
	}

	public BottomSheet setDragHandle (boolean value) {
		dragHandle = value;
		invalidate();
		return this;
	}

	public BottomSheet setDraggable (boolean value) {
		draggable = value;
		return this;
	}

	/** Makes the expanded sheet as tall as the screen allows instead of wrapping its content (use for lists). */
	public BottomSheet setFullHeight (boolean value) {
		fullHeight = value;
		invalidate();
		return this;
	}

	public BottomSheet setSheetBackground (com.badlogic.gdx.graphics.Color c) {
		sheet.setBackground(c);
		return this;
	}

	public BottomSheet setCallback (Callback c) {
		callback = c;
		return this;
	}

	public int getState () {
		return state;
	}

	public float getVisibleHeight () {
		return visible.value;
	}

	/** Shorthand: creates and shows a modal sheet. */
	public static BottomSheet show (Stage stage, Actor content) {
		BottomSheet s = new BottomSheet(content);
		s.show(stage);
		return s;
	}

	// ------------------------------------------------------------ behaviour

	@Override
	protected void onShow () {
		visible.set(0);
		validate();
		settle(COLLAPSED);
	}

	@Override
	protected void onHide () {
		settle(HIDDEN);
	}

	@Override
	protected boolean isHidden () {
		return !visible.isRunning() && visible.value <= 0.5f;
	}

	public void expand () {
		settle(EXPANDED);
	}

	public void collapse () {
		settle(COLLAPSED);
	}

	private void settle (int target) {
		if (target == HIDDEN && !dismissing) {
			dismiss(); // comes back here through onHide()
			return;
		}
		float to = target == HIDDEN ? 0 : target == COLLAPSED ? peekH : expandedH;
		float distance = Math.abs(to - visible.value);
		visible.animateTo(to, MathUtils.clamp(0.16f + distance / Compat.dp(2400), 0.16f, 0.36f));
		if (!visible.isRunning()) setVisibleHeight(to);
		if (target != state) {
			state = target;
			if (callback != null) callback.onStateChanged(this, state);
		}
		Compat.requestRender();
	}

	private void setVisibleHeight (float h) {
		visible.set(h);
		positionSheet();
		Compat.requestRender();
		if (callback != null) callback.onSlide(this, h);
	}

	private void positionSheet () {
		sheet.setY(Math.round(visible.value - sheetH + (modal ? Compat.getKeyboardHeight() : 0)));
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (visible.update(delta)) {
			positionSheet();
			if (callback != null) callback.onSlide(this, visible.value);
			Compat.requestRender();
		}
	}

	@Override
	public boolean onBackPressed () {
		if (!modal) {
			if (state == EXPANDED && peekH < expandedH) {
				collapse();
				return true;
			}
			return false;
		}
		return super.onBackPressed();
	}

	@Override
	protected float scrimFraction () {
		return peekH <= 0 ? 0 : Math.min(1f, visible.value / peekH);
	}

	@Override
	public void layout () {
		float w = getWidth(), h = getHeight();
		float sw = Math.min(w, Compat.dp(640));
		float handle = dragHandle ? Compat.dp(36) : 0;
		float max = Math.max(0, h - Compat.dp(56) - (modal ? Compat.getKeyboardHeight() : 0));
		LayoutParams lp = sheet.lp(content);
		boolean fill = fullHeight || lp.height == LayoutParams.MATCH_PARENT;
		float newH = fill ? max : Math.min(max, Measure.prefHeight(content, sw) + handle);
		float oldExpanded = expandedH, oldPeek = peekH;
		sheetH = expandedH = Math.round(newH);
		if (peekHeight > 0) peekH = Math.min(peekHeight, expandedH);
		else peekH = expandedH <= h * 0.6f ? expandedH : Math.round(h * 0.5f);
		// keep the current state when the size changes (rotation, content change)
		if (!dragging && !dismissing && getStage() != null && (oldExpanded != expandedH || oldPeek != peekH)) {
			if (state == EXPANDED) visible.set(expandedH);
			else if (state == COLLAPSED && !visible.isRunning() && oldPeek > 0) visible.set(peekH);
			else if (visible.isRunning()) visible.animateTo(state == EXPANDED ? expandedH : peekH, 0.25f);
		}
		sheet.setBounds(Math.round((w - sw) / 2), 0, Math.round(sw), sheetH);
		positionSheet();
		sheet.validate();
	}
}
