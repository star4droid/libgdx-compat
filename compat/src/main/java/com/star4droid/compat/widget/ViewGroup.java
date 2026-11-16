package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.ObjectMap;
import com.star4droid.compat.Compat;
import com.star4droid.compat.LayoutParams;
import com.star4droid.compat.Theme;
import com.star4droid.compat.graphics.Gfx;
import com.star4droid.compat.graphics.Ripple;

/** Base class of the layouts. Adds what an Android ViewGroup has on top of a scene2d group: per-child
 * {@link LayoutParams}, padding, a background (colour, rounded corners, outline, elevation shadow) and an optional click
 * with ripple. Groups do not use a transform matrix, so nesting them costs no extra draw calls. */
public class ViewGroup extends WidgetGroup implements Measurable, Clickable {
	protected float padLeft, padTop, padRight, padBottom;
	protected Color background, strokeColor;
	protected float radiusTL, radiusTR, radiusBR, radiusBL, strokeWidth, elevation;
	protected boolean clip;
	private final ObjectMap<Actor, LayoutParams> params = new ObjectMap<Actor, LayoutParams>();

	protected Ripple ripple;
	protected Runnable onClick, onLongClick;
	protected Color rippleColor;
	private boolean enabled = true;

	public ViewGroup () {
		setTransform(false);
		setTouchable(Touchable.childrenOnly);
	}

	protected static Gfx gfx () {
		return Compat.gfx();
	}

	protected static Theme theme () {
		return Compat.theme;
	}

	protected static float dp (float v) {
		return Compat.dp(v);
	}

	// ------------------------------------------------------------ children

	/** Adds a child and returns its layout params for chaining, e.g. {@code add(view).matchWidth().margin(dp(8))}. */
	public LayoutParams add (Actor child) {
		addActor(child);
		return lp(child);
	}

	public void add (Actor child, LayoutParams lp) {
		params.put(child, lp);
		addActor(child);
	}

	/** The layout params of a child (created on first use). */
	public LayoutParams lp (Actor child) {
		LayoutParams lp = params.get(child);
		if (lp == null) {
			lp = defaultParams();
			params.put(child, lp);
		}
		return lp;
	}

	protected LayoutParams defaultParams () {
		return new LayoutParams();
	}

	@Override
	public boolean removeActor (Actor actor, boolean unfocus) {
		params.remove(actor);
		return super.removeActor(actor, unfocus);
	}

	@Override
	public void clearChildren (boolean unfocus) {
		params.clear();
		super.clearChildren(unfocus);
	}

	@Override
	public boolean addListener (EventListener listener) {
		if (getTouchable() == Touchable.childrenOnly) setTouchable(Touchable.enabled);
		return super.addListener(listener);
	}

	// ------------------------------------------------------------ appearance

	public void setPadding (float all) {
		setPadding(all, all, all, all);
	}

	public void setPadding (float horizontal, float vertical) {
		setPadding(horizontal, vertical, horizontal, vertical);
	}

	public void setPadding (float left, float top, float right, float bottom) {
		padLeft = left;
		padTop = top;
		padRight = right;
		padBottom = bottom;
		invalidateHierarchy();
	}

	public float getPadLeft () {
		return padLeft;
	}

	public float getPadTop () {
		return padTop;
	}

	public float getPadRight () {
		return padRight;
	}

	public float getPadBottom () {
		return padBottom;
	}

	/** Background colour, or null for none. Pass a theme colour (e.g. {@code Compat.theme.surface}) to follow the theme. */
	public void setBackground (Color c) {
		background = c;
	}

	public Color getBackground () {
		return background;
	}

	public void setCornerRadius (float px) {
		radiusTL = radiusTR = radiusBR = radiusBL = px;
	}

	public void setCornerRadii (float topLeft, float topRight, float bottomRight, float bottomLeft) {
		radiusTL = topLeft;
		radiusTR = topRight;
		radiusBR = bottomRight;
		radiusBL = bottomLeft;
	}

	public void setStroke (Color c, float widthPx) {
		strokeColor = c;
		strokeWidth = widthPx;
	}

	/** Elevation shadow in pixels (needs a background). */
	public void setElevation (float px) {
		elevation = px;
	}

	/** Clips children to the bounds (uses a scissor, which costs a batch flush; off by default). */
	public void setClip (boolean value) {
		clip = value;
	}

	// ------------------------------------------------------------ clicks

	public void setOnClick (Runnable r) {
		onClick = r;
		ensurePressListener();
	}

	public void setOnLongClick (Runnable r) {
		onLongClick = r;
		ensurePressListener();
	}

	protected void ensurePressListener () {
		if (ripple == null) {
			ripple = new Ripple();
			setTouchable(Touchable.enabled);
			super.addListener(new PressListener(this));
		}
	}

	public void setRippleColor (Color c) {
		rippleColor = c;
	}

	public void setEnabled (boolean value) {
		enabled = value;
		setTouchable(value ? (ripple != null ? Touchable.enabled : Touchable.childrenOnly) : Touchable.disabled);
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
		if (ripple != null && ripple.act(delta)) Compat.requestRender();
	}

	// ------------------------------------------------------------ measuring / drawing

	@Override
	public float measureHeight (float width) {
		return getPrefHeight();
	}

	/** Sets a child's bounds on whole pixels and validates it. */
	protected static void place (Actor child, float x, float y, float w, float h) {
		child.setBounds(Math.round(x), Math.round(y), Math.round(w), Math.round(h));
		if (child instanceof Layout) ((Layout)child).validate();
	}

	protected void drawBackground (Batch batch, float alpha) {
		float x = getX(), y = getY(), w = getWidth(), h = getHeight();
		Gfx g = gfx();
		if (background != null) {
			if (elevation > 0) g.shadow(batch, x, y, w, h, radiusTL, elevation, theme().shadow, alpha);
			g.roundRect(batch, x, y, w, h, radiusTL, radiusTR, radiusBR, radiusBL, background, alpha);
		}
		if (strokeColor != null && strokeWidth > 0) g.stroke(batch, x, y, w, h, radiusTL, strokeWidth, strokeColor, alpha);
		if (ripple != null && ripple.isActive())
			ripple.draw(batch, g, x, y, w, h, radiusTL, rippleColor != null ? rippleColor : theme().onSurface, alpha);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();
		drawBackground(batch, parentAlpha * getColor().a);
		if (clip) {
			batch.flush();
			if (clipBegin(getX(), getY(), getWidth(), getHeight())) {
				super.draw(batch, parentAlpha);
				batch.flush();
				clipEnd();
			}
		} else {
			super.draw(batch, parentAlpha);
		}
	}

	/** Sets the id used by {@code findActor} / XML lookups. */
	public ViewGroup id (String id) {
		setName(id);
		return this;
	}
}
