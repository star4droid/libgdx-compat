package com.star4droid.compat.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

/** Helpers to ask any actor (compat view or plain scene2d widget) for its preferred size. */
public final class Measure {
	private Measure () {
	}

	public static float prefWidth (Actor a) {
		return a instanceof Layout ? ((Layout)a).getPrefWidth() : a.getWidth();
	}

	public static float prefHeight (Actor a, float width) {
		if (a instanceof Measurable) return ((Measurable)a).measureHeight(width);
		if (a instanceof Layout) return ((Layout)a).getPrefHeight();
		return a.getHeight();
	}
}
