package com.star4droid.compat.widget;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.star4droid.compat.LayoutParams;

/** Scrolls one child (usually a LinearLayout) vertically or horizontally. Off-screen children of that child are skipped
 * when drawing. */
public class ScrollView extends ScrollContainer {
	private Actor content;
	private final Rectangle cull = new Rectangle();
	private float contentLength;

	public ScrollView () {
		super(true);
	}

	/** @param vertical false for a horizontal scroller */
	public ScrollView (boolean vertical) {
		super(vertical);
	}

	public ScrollView (Actor content) {
		super(true);
		setContent(content);
	}

	public void setContent (Actor a) {
		if (content != null) content.remove();
		content = a;
		if (a != null) addActor(a);
	}

	public Actor getContent () {
		return content;
	}

	@Override
	protected void childrenChanged () {
		super.childrenChanged();
		if (getChildren().size > 0 && content == null) content = getChildren().first();
	}

	@Override
	public float getPrefWidth () {
		if (content == null) return 0;
		LayoutParams lp = lp(content);
		return (lp.width >= 0 ? lp.width : Measure.prefWidth(content)) + padLeft + padRight;
	}

	@Override
	public float measureHeight (float width) {
		if (content == null) return 0;
		return Measure.prefHeight(content, width - padLeft - padRight) + padTop + padBottom;
	}

	@Override
	public float getPrefHeight () {
		return measureHeight(getWidth() > 0 ? getWidth() : getPrefWidth());
	}

	@Override
	protected float computeContentLength () {
		if (content == null) return 0;
		if (vertical) contentLength = Measure.prefHeight(content, getWidth() - padLeft - padRight) + padTop + padBottom;
		else contentLength = Measure.prefWidth(content) + padLeft + padRight;
		return contentLength;
	}

	@Override
	protected void layoutContent () {
		if (content == null) return;
		if (vertical) {
			float h = Math.max(contentLength, getHeight()) - padTop - padBottom;
			place(content, padLeft, getHeight() - padTop - h + scroll, getWidth() - padLeft - padRight, h);
		} else {
			float w = Math.max(contentLength, getWidth()) - padLeft - padRight;
			float x = com.star4droid.compat.Compat.isRtl() ? getWidth() - padRight - w + scroll : padLeft - scroll;
			place(content, x, padBottom, w, getHeight() - padTop - padBottom);
		}
		if (content instanceof Group) {
			cull.set(-content.getX(), -content.getY(), getWidth(), getHeight());
			((Group)content).setCullingArea(cull);
		}
	}
}
