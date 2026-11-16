package com.star4droid.compat;

/** The Material 3 type scale. Sizes are in sp. */
public final class Typography {
	public static final int DISPLAY_SMALL = 0, HEADLINE_MEDIUM = 1, HEADLINE_SMALL = 2, TITLE_LARGE = 3, TITLE_MEDIUM = 4,
		TITLE_SMALL = 5, BODY_LARGE = 6, BODY_MEDIUM = 7, BODY_SMALL = 8, LABEL_LARGE = 9, LABEL_MEDIUM = 10, LABEL_SMALL = 11;

	/** Editable: change a size here before building the UI to restyle every text of that role. */
	public static final float[] SIZE = {36, 28, 24, 22, 16, 14, 16, 14, 12, 14, 12, 11};
	public static final boolean[] MEDIUM = {false, false, false, false, true, true, false, false, false, true, true, true};
	private static final String[] NAMES = {"displaySmall", "headlineMedium", "headlineSmall", "titleLarge", "titleMedium",
		"titleSmall", "bodyLarge", "bodyMedium", "bodySmall", "labelLarge", "labelMedium", "labelSmall"};

	private Typography () {
	}

	public static float sizePx (int style) {
		return Compat.sp(SIZE[style]);
	}

	/** @return the style index for a name such as "titleLarge" / "title_large", or -1. */
	public static int parse (String name) {
		String n = name.replace("_", "").replace("-", "");
		for (int i = 0; i < NAMES.length; i++)
			if (NAMES[i].equalsIgnoreCase(n)) return i;
		return -1;
	}
}
