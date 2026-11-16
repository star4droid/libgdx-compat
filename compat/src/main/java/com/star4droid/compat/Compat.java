package com.star4droid.compat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.star4droid.compat.graphics.Fonts;
import com.star4droid.compat.graphics.Gfx;

/** Entry point of libgdx-compat. Call {@link #init()} once in {@code create()} and {@link #dispose()} in {@code dispose()}.
 * <p>
 * The library works in real pixels with a {@link ScreenViewport}; use {@link #dp(float)} / {@link #sp(float)} for sizes. */
public final class Compat {
	/** The single theme instance. Its colours are mutable: {@link Theme#set(Theme)} restyles every widget at once. */
	public static final Theme theme = new Theme();

	private static float density = 1f, fontScale = 1f;
	private static boolean rtl, initialized;
	private static Gfx gfx;
	private static Fonts fonts;
	private static float keyboardHeight;
	/** Exit the app when BACK is pressed and no overlay / navigator consumed it (Android). */
	public static boolean exitOnBack = true;

	/** Optional settings for {@link #init(Config)}. */
	public static class Config {
		/** Pixels per dp. 0 = automatic (device density on mobile, 1 on desktop). */
		public float density = 0f;
		/** Extra multiplier for text sizes. */
		public float fontScale = 1f;
		/** Custom fonts (any .ttf/.otf). For Arabic the font must contain the presentation forms block (U+FE70..U+FEFF). */
		public FileHandle regularFont, mediumFont, iconFont;
		/** Size of one atlas page shared by shapes, text and icons. */
		public int atlasPageSize = 1024;
		/** Mirror layouts for right-to-left languages. */
		public boolean rtl = false;
		public boolean dark = false;
		/** Catch the Android back key so overlays and the Navigator can handle it. */
		public boolean catchBackKey = true;
	}

	private Compat () {
	}

	public static void init () {
		init(new Config());
	}

	public static void init (Config c) {
		if (initialized) dispose();
		density = c.density > 0 ? c.density : defaultDensity();
		fontScale = c.fontScale;
		rtl = c.rtl;
		if (c.dark) theme.set(Theme.dark());
		gfx = new Gfx(c.atlasPageSize);
		FileHandle regular = c.regularFont != null ? c.regularFont : Gdx.files.classpath("compat/fonts/IBMPlexSansArabic-Regular.ttf");
		FileHandle medium = c.mediumFont != null ? c.mediumFont
			: (c.regularFont != null ? c.regularFont : Gdx.files.classpath("compat/fonts/IBMPlexSansArabic-Medium.ttf"));
		FileHandle icons = c.iconFont != null ? c.iconFont : Gdx.files.classpath("compat/fonts/MaterialIcons-Regular.ttf");
		fonts = new Fonts(gfx, regular, medium, icons);
		if (c.catchBackKey) Gdx.input.setCatchKey(Input.Keys.BACK, true);
		initialized = true;
	}

	private static float defaultDensity () {
		Application.ApplicationType t = Gdx.app.getType();
		if (t == Application.ApplicationType.Android || t == Application.ApplicationType.iOS) {
			float d = Gdx.graphics.getDensity();
			return d > 0.5f ? d : 1f;
		}
		return 1f;
	}

	public static boolean isInitialized () {
		return initialized;
	}

	/** Creates a stage with a pixel-perfect viewport and back-key dispatching already attached. */
	public static Stage createStage () {
		Stage stage = new Stage(new ScreenViewport());
		attach(stage);
		return stage;
	}

	/** Adds back-key (Android BACK / desktop ESC) dispatching to an existing stage. */
	public static void attach (final Stage stage) {
		stage.getRoot().addListener(new InputListener() {
			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if (keycode != Input.Keys.BACK && keycode != Input.Keys.ESCAPE) return false;
				if (dispatchBack(stage)) return true;
				// nothing left to close: leave the app, as the system would have done had the key not been caught
				if (keycode == Input.Keys.BACK && exitOnBack) Gdx.app.exit();
				return false;
			}
		});
	}

	/** Lets the top-most {@link BackHandler} consume a back press. @return true if something handled it. */
	public static boolean dispatchBack (Stage stage) {
		return dispatchBack(stage.getRoot());
	}

	private static boolean dispatchBack (Group group) {
		SnapshotArray<Actor> children = group.getChildren();
		for (int i = children.size - 1; i >= 0; i--) {
			Actor a = children.get(i);
			if (!a.isVisible()) continue;
			if (a instanceof Group && dispatchBack((Group)a)) return true;
			if (a instanceof BackHandler && ((BackHandler)a).onBackPressed()) return true;
		}
		return false;
	}

	/** dp to pixels, rounded so edges stay crisp. */
	public static float dp (float dp) {
		return Math.round(dp * density);
	}

	/** dp to pixels without rounding. */
	public static float dpf (float dp) {
		return dp * density;
	}

	/** sp (text size) to pixels. */
	public static float sp (float sp) {
		return sp * density * fontScale;
	}

	public static float getDensity () {
		return density;
	}

	public static float getFontScale () {
		return fontScale;
	}

	public static boolean isRtl () {
		return rtl;
	}

	/** Mirrors layouts (start/end, horizontal order). Call before building the UI, or invalidate the root afterwards. */
	public static void setRtl (boolean value) {
		rtl = value;
	}

	/** Bottom inset (px) taken by the on-screen keyboard; overlays keep clear of it. */
	public static float getKeyboardHeight () {
		return keyboardHeight;
	}

	public static void setKeyboardHeight (float px) {
		keyboardHeight = px;
		requestRender();
	}

	/** Asks for another frame; lets apps use {@code Gdx.graphics.setContinuousRendering(false)} to save battery. */
	public static void requestRender () {
		if (Gdx.graphics != null) Gdx.graphics.requestRendering();
	}

	public static Gfx gfx () {
		return gfx;
	}

	public static Fonts fonts () {
		return fonts;
	}

	public static void dispose () {
		if (!initialized) return;
		initialized = false;
		fonts.dispose();
		gfx.dispose();
		fonts = null;
		gfx = null;
	}
}
