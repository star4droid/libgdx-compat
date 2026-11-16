package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

/** Small helpers. */
public final class Views {
	private static final Color TMP = new Color();

	private Views () {
	}

	/** @return true if the actor and all of its ancestors are visible and it is on a stage. */
	public static boolean isShown (Actor a) {
		if (a.getStage() == null) return false;
		for (Actor p = a; p != null; p = p.getParent())
			if (!p.isVisible()) return false;
		return true;
	}

	/** Finds a descendant by id (the actor name), like findViewById. */
	@SuppressWarnings("unchecked")
	public static <T extends Actor> T find (Actor root, String id) {
		if (id.equals(root.getName())) return (T)root;
		if (root instanceof Group) return (T)((Group)root).findActor(id);
		return null;
	}

	/** Shared temporary colour: a blended towards b. Use the result immediately. */
	public static Color lerp (Color a, Color b, float t) {
		return TMP.set(a).lerp(b, t);
	}

	/** Shared temporary colour with a different alpha. Use the result immediately. */
	public static Color alpha (Color c, float a) {
		return TMP.set(c.r, c.g, c.b, c.a * a);
	}
}
