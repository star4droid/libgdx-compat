package com.star4droid.compat.demo;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] args) {
		int w = 412, h = 892;
		float density = 1f;
		String test = null, out = "shots";
		boolean rtl = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--size")) {
				String[] p = args[++i].split("x");
				w = Integer.parseInt(p[0]);
				h = Integer.parseInt(p[1]);
			} else if (args[i].equals("--density")) density = Float.parseFloat(args[++i]);
			else if (args[i].equals("--test")) test = args[++i];
			else if (args[i].equals("--out")) out = args[++i];
			else if (args[i].equals("--rtl")) rtl = true;
		}
		Lwjgl3ApplicationConfiguration c = new Lwjgl3ApplicationConfiguration();
		c.setTitle("libgdx-compat gallery");
		c.setWindowedMode(w, h);
		c.useVsync(test == null);
		c.setForegroundFPS(test == null ? 0 : 60);
		GalleryApp app = new GalleryApp();
		app.density = density;
		app.rtl = rtl;
		if (test != null) {
			c.disableAudio(true);
			new Lwjgl3Application(new TestRunner(app, test, out), c);
		} else {
			new Lwjgl3Application(app, c);
		}
	}
}
