package com.star4droid.compat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ObjectMap;

/** Material 3 colour roles, corner sizes and type scale. Colours are mutable objects that widgets reference directly, so
 * changing them (or calling {@link #set(Theme)}) restyles the whole UI without rebuilding it. */
public class Theme {
	public final Color primary = new Color(), onPrimary = new Color(), primaryContainer = new Color(),
		onPrimaryContainer = new Color();
	public final Color secondary = new Color(), onSecondary = new Color(), secondaryContainer = new Color(),
		onSecondaryContainer = new Color();
	public final Color tertiary = new Color(), onTertiary = new Color(), tertiaryContainer = new Color(),
		onTertiaryContainer = new Color();
	public final Color error = new Color(), onError = new Color(), errorContainer = new Color(), onErrorContainer = new Color();
	public final Color background = new Color(), onBackground = new Color();
	public final Color surface = new Color(), onSurface = new Color(), surfaceVariant = new Color(),
		onSurfaceVariant = new Color();
	public final Color surfaceContainerLowest = new Color(), surfaceContainerLow = new Color(), surfaceContainer = new Color(),
		surfaceContainerHigh = new Color(), surfaceContainerHighest = new Color();
	public final Color outline = new Color(), outlineVariant = new Color();
	public final Color inverseSurface = new Color(), inverseOnSurface = new Color(), inversePrimary = new Color();
	public final Color scrim = new Color(0, 0, 0, 1), shadow = new Color(0, 0, 0, 1);

	/** Corner radii in dp. */
	public float cornerExtraSmall = 4, cornerSmall = 8, cornerMedium = 12, cornerLarge = 16, cornerExtraLarge = 28;
	public boolean dark;

	private ObjectMap<String, Color> byName;

	public Theme () {
		applyBaseline(false);
	}

	public static Theme light () {
		return new Theme();
	}

	public static Theme dark () {
		Theme t = new Theme();
		t.applyBaseline(true);
		return t;
	}

	/** Builds a full scheme from one brand colour. */
	public static Theme fromSeed (Color seed, boolean dark) {
		Theme t = new Theme();
		t.applySeed(seed, dark);
		return t;
	}

	/** Copies all values from another theme into this one (widgets keep their colour references). */
	public Theme set (Theme o) {
		ObjectMap<String, Color> mine = colors(), theirs = o.colors();
		for (ObjectMap.Entry<String, Color> e : mine)
			e.value.set(theirs.get(e.key));
		cornerExtraSmall = o.cornerExtraSmall;
		cornerSmall = o.cornerSmall;
		cornerMedium = o.cornerMedium;
		cornerLarge = o.cornerLarge;
		cornerExtraLarge = o.cornerExtraLarge;
		dark = o.dark;
		Compat.requestRender();
		return this;
	}

	/** Looks a colour role up by name, e.g. "primary" or "onSurfaceVariant" (also accepts snake_case). */
	public Color color (String name) {
		Color c = colors().get(name);
		if (c == null && name.indexOf('_') >= 0) {
			StringBuilder sb = new StringBuilder();
			boolean up = false;
			for (int i = 0; i < name.length(); i++) {
				char ch = name.charAt(i);
				if (ch == '_') up = true;
				else {
					sb.append(up ? Character.toUpperCase(ch) : ch);
					up = false;
				}
			}
			c = colors().get(sb.toString());
		}
		return c;
	}

	public ObjectMap<String, Color> colors () {
		if (byName == null) {
			ObjectMap<String, Color> m = new ObjectMap<String, Color>();
			m.put("primary", primary);
			m.put("onPrimary", onPrimary);
			m.put("primaryContainer", primaryContainer);
			m.put("onPrimaryContainer", onPrimaryContainer);
			m.put("secondary", secondary);
			m.put("onSecondary", onSecondary);
			m.put("secondaryContainer", secondaryContainer);
			m.put("onSecondaryContainer", onSecondaryContainer);
			m.put("tertiary", tertiary);
			m.put("onTertiary", onTertiary);
			m.put("tertiaryContainer", tertiaryContainer);
			m.put("onTertiaryContainer", onTertiaryContainer);
			m.put("error", error);
			m.put("onError", onError);
			m.put("errorContainer", errorContainer);
			m.put("onErrorContainer", onErrorContainer);
			m.put("background", background);
			m.put("onBackground", onBackground);
			m.put("surface", surface);
			m.put("onSurface", onSurface);
			m.put("surfaceVariant", surfaceVariant);
			m.put("onSurfaceVariant", onSurfaceVariant);
			m.put("surfaceContainerLowest", surfaceContainerLowest);
			m.put("surfaceContainerLow", surfaceContainerLow);
			m.put("surfaceContainer", surfaceContainer);
			m.put("surfaceContainerHigh", surfaceContainerHigh);
			m.put("surfaceContainerHighest", surfaceContainerHighest);
			m.put("outline", outline);
			m.put("outlineVariant", outlineVariant);
			m.put("inverseSurface", inverseSurface);
			m.put("inverseOnSurface", inverseOnSurface);
			m.put("inversePrimary", inversePrimary);
			m.put("scrim", scrim);
			m.put("shadow", shadow);
			byName = m;
		}
		return byName;
	}

	private static void hex (Color c, int rgb) {
		c.set(((rgb >> 16) & 0xff) / 255f, ((rgb >> 8) & 0xff) / 255f, (rgb & 0xff) / 255f, 1f);
	}

	private void applyBaseline (boolean dark) {
		this.dark = dark;
		if (!dark) {
			hex(primary, 0x6750A4);
			hex(onPrimary, 0xFFFFFF);
			hex(primaryContainer, 0xEADDFF);
			hex(onPrimaryContainer, 0x21005D);
			hex(secondary, 0x625B71);
			hex(onSecondary, 0xFFFFFF);
			hex(secondaryContainer, 0xE8DEF8);
			hex(onSecondaryContainer, 0x1D192B);
			hex(tertiary, 0x7D5260);
			hex(onTertiary, 0xFFFFFF);
			hex(tertiaryContainer, 0xFFD8E4);
			hex(onTertiaryContainer, 0x31111D);
			hex(error, 0xB3261E);
			hex(onError, 0xFFFFFF);
			hex(errorContainer, 0xF9DEDC);
			hex(onErrorContainer, 0x410E0B);
			hex(background, 0xFEF7FF);
			hex(onBackground, 0x1D1B20);
			hex(surface, 0xFEF7FF);
			hex(onSurface, 0x1D1B20);
			hex(surfaceVariant, 0xE7E0EC);
			hex(onSurfaceVariant, 0x49454F);
			hex(surfaceContainerLowest, 0xFFFFFF);
			hex(surfaceContainerLow, 0xF7F2FA);
			hex(surfaceContainer, 0xF3EDF7);
			hex(surfaceContainerHigh, 0xECE6F0);
			hex(surfaceContainerHighest, 0xE6E0E9);
			hex(outline, 0x79747E);
			hex(outlineVariant, 0xCAC4D0);
			hex(inverseSurface, 0x322F35);
			hex(inverseOnSurface, 0xF5EFF7);
			hex(inversePrimary, 0xD0BCFF);
		} else {
			hex(primary, 0xD0BCFF);
			hex(onPrimary, 0x381E72);
			hex(primaryContainer, 0x4F378B);
			hex(onPrimaryContainer, 0xEADDFF);
			hex(secondary, 0xCCC2DC);
			hex(onSecondary, 0x332D41);
			hex(secondaryContainer, 0x4A4458);
			hex(onSecondaryContainer, 0xE8DEF8);
			hex(tertiary, 0xEFB8C8);
			hex(onTertiary, 0x492532);
			hex(tertiaryContainer, 0x633B48);
			hex(onTertiaryContainer, 0xFFD8E4);
			hex(error, 0xF2B8B5);
			hex(onError, 0x601410);
			hex(errorContainer, 0x8C1D18);
			hex(onErrorContainer, 0xF9DEDC);
			hex(background, 0x141218);
			hex(onBackground, 0xE6E0E9);
			hex(surface, 0x141218);
			hex(onSurface, 0xE6E0E9);
			hex(surfaceVariant, 0x49454F);
			hex(onSurfaceVariant, 0xCAC4D0);
			hex(surfaceContainerLowest, 0x0F0D13);
			hex(surfaceContainerLow, 0x1D1B20);
			hex(surfaceContainer, 0x211F26);
			hex(surfaceContainerHigh, 0x2B2930);
			hex(surfaceContainerHighest, 0x36343B);
			hex(outline, 0x938F99);
			hex(outlineVariant, 0x49454F);
			hex(inverseSurface, 0xE6E0E9);
			hex(inverseOnSurface, 0x322F35);
			hex(inversePrimary, 0x6750A4);
		}
	}

	// ---- seed based scheme (HSL tonal palettes; a light-weight approximation of Material's HCT) ----

	private static void hsl (Color out, float hue, float sat, float l) {
		float c = (1f - Math.abs(2f * l - 1f)) * sat;
		float hp = (hue % 360f) / 60f;
		float x = c * (1f - Math.abs(hp % 2f - 1f));
		float r = 0, g = 0, b = 0;
		if (hp < 1) {
			r = c;
			g = x;
		} else if (hp < 2) {
			r = x;
			g = c;
		} else if (hp < 3) {
			g = c;
			b = x;
		} else if (hp < 4) {
			g = x;
			b = c;
		} else if (hp < 5) {
			r = x;
			b = c;
		} else {
			r = c;
			b = x;
		}
		float m = l - c / 2f;
		out.set(r + m, g + m, b + m, 1f);
	}

	private static float linear (float c) {
		return c <= 0.04045f ? c / 12.92f : (float)Math.pow((c + 0.055f) / 1.055f, 2.4f);
	}

	/** A colour of the given hue whose perceived lightness (L*) equals the tone, like Material's tonal palettes. */
	private static void tone (Color out, float hue, float sat, float tone) {
		float target = tone >= 100 ? 1f : tone <= 0 ? 0f : tone > 8 ? (float)Math.pow((tone + 16f) / 116f, 3) : tone / 903.3f;
		float lo = 0f, hi = 1f;
		for (int i = 0; i < 14; i++) {
			float mid = (lo + hi) / 2f;
			hsl(out, hue, sat, mid);
			float y = 0.2126f * linear(out.r) + 0.7152f * linear(out.g) + 0.0722f * linear(out.b);
			if (y < target) lo = mid;
			else hi = mid;
		}
		hsl(out, hue, sat, (lo + hi) / 2f);
	}

	private void applySeed (Color seed, boolean dark) {
		this.dark = dark;
		float max = Math.max(seed.r, Math.max(seed.g, seed.b)), min = Math.min(seed.r, Math.min(seed.g, seed.b));
		float d = max - min, h = 0;
		if (d > 0.0001f) {
			if (max == seed.r) h = ((seed.g - seed.b) / d) % 6f;
			else if (max == seed.g) h = (seed.b - seed.r) / d + 2f;
			else h = (seed.r - seed.g) / d + 4f;
			h *= 60f;
			if (h < 0) h += 360f;
		}
		float l = (max + min) / 2f;
		float sat = d < 0.0001f ? 0f : d / (1f - Math.abs(2f * l - 1f));
		float p = Math.max(0.45f, Math.min(0.9f, sat)), s = 0.18f, t = 0.3f, n = 0.06f, nv = 0.12f, h3 = (h + 60f) % 360f;
		if (!dark) {
			tone(primary, h, p, 40);
			tone(onPrimary, h, p, 100);
			tone(primaryContainer, h, p * 0.6f, 90);
			tone(onPrimaryContainer, h, p, 12);
			tone(secondary, h, s, 40);
			tone(onSecondary, h, s, 100);
			tone(secondaryContainer, h, s * 1.6f, 90);
			tone(onSecondaryContainer, h, s, 12);
			tone(tertiary, h3, t, 40);
			tone(onTertiary, h3, t, 100);
			tone(tertiaryContainer, h3, t * 1.5f, 90);
			tone(onTertiaryContainer, h3, t, 12);
			hex(error, 0xB3261E);
			hex(onError, 0xFFFFFF);
			hex(errorContainer, 0xF9DEDC);
			hex(onErrorContainer, 0x410E0B);
			tone(background, h, n * 2f, 98);
			tone(onBackground, h, n, 10);
			tone(surface, h, n * 2f, 98);
			tone(onSurface, h, n, 10);
			tone(surfaceVariant, h, nv * 1.5f, 90);
			tone(onSurfaceVariant, h, nv, 30);
			tone(surfaceContainerLowest, h, n, 100);
			tone(surfaceContainerLow, h, n * 2.2f, 96);
			tone(surfaceContainer, h, n * 2.2f, 94);
			tone(surfaceContainerHigh, h, n * 2.2f, 92);
			tone(surfaceContainerHighest, h, n * 2.2f, 90);
			tone(outline, h, nv, 50);
			tone(outlineVariant, h, nv, 80);
			tone(inverseSurface, h, n, 20);
			tone(inverseOnSurface, h, n, 95);
			tone(inversePrimary, h, p, 80);
		} else {
			tone(primary, h, p, 80);
			tone(onPrimary, h, p, 20);
			tone(primaryContainer, h, p, 30);
			tone(onPrimaryContainer, h, p, 90);
			tone(secondary, h, s, 80);
			tone(onSecondary, h, s, 20);
			tone(secondaryContainer, h, s, 30);
			tone(onSecondaryContainer, h, s, 90);
			tone(tertiary, h3, t, 80);
			tone(onTertiary, h3, t, 20);
			tone(tertiaryContainer, h3, t, 30);
			tone(onTertiaryContainer, h3, t, 90);
			hex(error, 0xF2B8B5);
			hex(onError, 0x601410);
			hex(errorContainer, 0x8C1D18);
			hex(onErrorContainer, 0xF9DEDC);
			tone(background, h, n, 7);
			tone(onBackground, h, n, 90);
			tone(surface, h, n, 7);
			tone(onSurface, h, n, 90);
			tone(surfaceVariant, h, nv, 30);
			tone(onSurfaceVariant, h, nv, 80);
			tone(surfaceContainerLowest, h, n, 4);
			tone(surfaceContainerLow, h, n, 10);
			tone(surfaceContainer, h, n, 12);
			tone(surfaceContainerHigh, h, n, 17);
			tone(surfaceContainerHighest, h, n, 22);
			tone(outline, h, nv, 60);
			tone(outlineVariant, h, nv, 30);
			tone(inverseSurface, h, n, 90);
			tone(inverseOnSurface, h, n, 20);
			tone(inversePrimary, h, p, 40);
		}
	}

	/** Parses "#RGB", "#RRGGBB" or Android style "#AARRGGBB". */
	public static Color parseColor (String s) {
		String h = s.charAt(0) == '#' ? s.substring(1) : s;
		if (h.length() == 3) {
			h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
		}
		long v = Long.parseLong(h, 16);
		float a = 1f;
		if (h.length() == 8) {
			a = ((v >> 24) & 0xff) / 255f;
		}
		return new Color(((v >> 16) & 0xff) / 255f, ((v >> 8) & 0xff) / 255f, (v & 0xff) / 255f, a);
	}
}
