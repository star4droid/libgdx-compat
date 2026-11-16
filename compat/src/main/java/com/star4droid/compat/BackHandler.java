package com.star4droid.compat;

/** Implemented by actors (overlays, navigators) that want to consume the back key. */
public interface BackHandler {
	/** @return true if the back press was consumed. */
	boolean onBackPressed ();
}
