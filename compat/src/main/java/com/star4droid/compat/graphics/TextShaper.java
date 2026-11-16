package com.star4droid.compat.graphics;

/** Arabic shaping (contextual letter forms + lam-alef ligatures) and a compact bidirectional reordering, because libGDX
 * itself draws characters one by one from left to right. */
public final class TextShaper {
	private static final char[] FORM = new char[0x64B - 0x621];
	/** 0 = not a joining letter, 1 = joins only to the previous letter, 2 = joins on both sides, 3 = tatweel. */
	private static final byte[] TYPE = new byte[0x64B - 0x621];

	private static void set (int c, int form, int type) {
		FORM[c - 0x621] = (char)form;
		TYPE[c - 0x621] = (byte)type;
	}

	static {
		set(0x621, 0xFE80, 0);
		set(0x622, 0xFE81, 1);
		set(0x623, 0xFE83, 1);
		set(0x624, 0xFE85, 1);
		set(0x625, 0xFE87, 1);
		set(0x626, 0xFE89, 2);
		set(0x627, 0xFE8D, 1);
		set(0x628, 0xFE8F, 2);
		set(0x629, 0xFE93, 1);
		set(0x62A, 0xFE95, 2);
		set(0x62B, 0xFE99, 2);
		set(0x62C, 0xFE9D, 2);
		set(0x62D, 0xFEA1, 2);
		set(0x62E, 0xFEA5, 2);
		set(0x62F, 0xFEA9, 1);
		set(0x630, 0xFEAB, 1);
		set(0x631, 0xFEAD, 1);
		set(0x632, 0xFEAF, 1);
		set(0x633, 0xFEB1, 2);
		set(0x634, 0xFEB5, 2);
		set(0x635, 0xFEB9, 2);
		set(0x636, 0xFEBD, 2);
		set(0x637, 0xFEC1, 2);
		set(0x638, 0xFEC5, 2);
		set(0x639, 0xFEC9, 2);
		set(0x63A, 0xFECD, 2);
		set(0x640, 0x640, 3);
		set(0x641, 0xFED1, 2);
		set(0x642, 0xFED5, 2);
		set(0x643, 0xFED9, 2);
		set(0x644, 0xFEDD, 2);
		set(0x645, 0xFEE1, 2);
		set(0x646, 0xFEE5, 2);
		set(0x647, 0xFEE9, 2);
		set(0x648, 0xFEED, 1);
		set(0x649, 0xFEEF, 1);
		set(0x64A, 0xFEF1, 2);
	}

	private TextShaper () {
	}

	private static int type (char c) {
		return (c >= 0x621 && c < 0x64B) ? TYPE[c - 0x621] : 0;
	}

	/** Combining marks (harakat): they do not interrupt joining and stay attached to their letter. */
	public static boolean isMark (char c) {
		return (c >= 0x64B && c <= 0x65F) || c == 0x670 || (c >= 0x6D6 && c <= 0x6DC) || (c >= 0x5B0 && c <= 0x5BD)
			|| (c >= ALIAS_BELOW && c < ALIAS_RAISED + 0x40);
	}

	/** libGDX cannot apply a font's mark positioning tables, so marks that must sit below the letter (kasra...) and second
	 * marks stacked on a shadda are swapped for private aliases that {@link TextLayout} registers with an adjusted offset. */
	public static final char ALIAS_BELOW = 0xF800, ALIAS_RAISED = 0xF840;

	private static boolean isBelowMark (char c) {
		return c == 0x64D || c == 0x650 || c == 0x655 || c == 0x656;
	}

	/** @return the real mark a private alias stands for, or 0 if c is not an alias. */
	public static char aliasSource (char c) {
		if (c >= ALIAS_BELOW && c < ALIAS_RAISED) return (char)(c - ALIAS_BELOW + 0x640);
		if (c >= ALIAS_RAISED && c < ALIAS_RAISED + 0x40) return (char)(c - ALIAS_RAISED + 0x640);
		return 0;
	}

	public static boolean isRtlChar (char c) {
		return (c >= 0x590 && c <= 0x8FF && !isArabicDigit(c)) || (c >= 0xFB1D && c <= 0xFDFF) || (c >= 0xFE70 && c <= 0xFEFF);
	}

	private static boolean isArabicDigit (char c) {
		return (c >= 0x660 && c <= 0x669) || (c >= 0x6F0 && c <= 0x6F9);
	}

	/** @return true if the text contains right-to-left characters and therefore needs {@link #shape} / {@link #reorder}. */
	public static boolean hasRtl (CharSequence text) {
		for (int i = 0, n = text.length(); i < n; i++) {
			char c = text.charAt(i);
			if (c >= 0x590 && isRtlChar(c)) return true;
		}
		return false;
	}

	/** Direction of a paragraph from its first strong character. */
	public static boolean isRtlParagraph (CharSequence text) {
		for (int i = 0, n = text.length(); i < n; i++) {
			char c = text.charAt(i);
			if (isRtlChar(c)) return true;
			if (Character.isLetter(c)) return false;
		}
		return false;
	}

	/** Replaces Arabic letters by their contextual presentation forms. The result is still in logical order. */
	public static String shape (CharSequence text) {
		int n = text.length();
		StringBuilder out = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			char c = text.charAt(i);
			int t = type(c);
			if (t == 0 || t == 3) {
				if (c >= 0x64B && c <= 0x656) {
					boolean stacked = false;
					for (int j = i - 1; j >= 0 && isMark(text.charAt(j)); j--)
						if (!isBelowMark(text.charAt(j))) stacked = true;
					if (isBelowMark(c)) c = (char)(ALIAS_BELOW + (c - 0x640));
					else if (stacked && c <= 0x652) c = (char)(ALIAS_RAISED + (c - 0x640));
				}
				out.append(c);
				continue;
			}
			// previous letter (skipping marks)
			boolean prev = false;
			for (int j = i - 1; j >= 0; j--) {
				char p = text.charAt(j);
				if (isMark(p)) continue;
				int pt = type(p);
				prev = pt == 2 || pt == 3;
				break;
			}
			// lam + alef ligature
			if (c == 0x644 && i + 1 < n) {
				char a = text.charAt(i + 1);
				int lig = a == 0x622 ? 0xFEF5 : a == 0x623 ? 0xFEF7 : a == 0x625 ? 0xFEF9 : a == 0x627 ? 0xFEFB : 0;
				if (lig != 0) {
					out.append((char)(lig + (prev ? 1 : 0)));
					i++;
					continue;
				}
			}
			boolean next = false;
			if (t == 2) {
				for (int j = i + 1; j < n; j++) {
					char q = text.charAt(j);
					if (isMark(q)) continue;
					next = type(q) != 0;
					break;
				}
			}
			int form = prev ? (next ? 3 : 1) : (next ? 2 : 0);
			out.append((char)(FORM[c - 0x621] + form));
		}
		return out.toString();
	}

	private static boolean isDigit (char c) {
		return (c >= '0' && c <= '9') || isArabicDigit(c);
	}

	private static char mirror (char c) {
		switch (c) {
		case '(':
			return ')';
		case ')':
			return '(';
		case '[':
			return ']';
		case ']':
			return '[';
		case '{':
			return '}';
		case '}':
			return '{';
		case '<':
			return '>';
		case '>':
			return '<';
		case '«':
			return '»';
		case '»':
			return '«';
		default:
			return c;
		}
	}

	private static byte[] levelBuf = new byte[64];
	private static char[] charBuf = new char[64];

	/** Converts one line (already shaped, no line breaks) from logical to visual order.
	 * @param rtlParagraph base direction of the paragraph */
	public static String reorder (CharSequence line, boolean rtlParagraph) {
		int n = line.length();
		if (n == 0) return "";
		if (levelBuf.length < n) {
			levelBuf = new byte[n * 2];
			charBuf = new char[n * 2];
		}
		byte[] lv = levelBuf;
		char[] ch = charBuf;
		byte base = (byte)(rtlParagraph ? 1 : 0);
		// 1) strong characters and numbers; neutrals get -1 for now
		boolean lastStrongRtl = rtlParagraph;
		for (int i = 0; i < n; i++) {
			char c = line.charAt(i);
			ch[i] = c;
			if (isRtlChar(c) || isMark(c)) {
				lv[i] = 1;
				lastStrongRtl = true;
			} else if (isDigit(c)) {
				lv[i] = (byte)((rtlParagraph || lastStrongRtl) ? 2 : 0);
			} else if (Character.isLetter(c)) {
				lv[i] = (byte)(rtlParagraph ? 2 : 0);
				lastStrongRtl = false;
			} else {
				lv[i] = -1;
			}
		}
		// 2) neutrals: separators inside numbers join the number; otherwise take the direction both neighbours agree on
		int i = 0;
		while (i < n) {
			if (lv[i] != -1) {
				i++;
				continue;
			}
			int s = i;
			while (i < n && lv[i] == -1)
				i++;
			int before = s > 0 ? lv[s - 1] : base, after = i < n ? lv[i] : base;
			byte fill;
			if (i - s == 1 && s > 0 && i < n && isDigit(ch[s - 1]) && isDigit(ch[i]) && ".,:/-".indexOf(ch[s]) >= 0) {
				fill = lv[s - 1];
			} else {
				// numbers count as the direction of their surrounding run
				boolean bRtl = (before & 1) == 1 || (before == 2 && s > 0 && isDigit(ch[s - 1]) && base == 1);
				boolean aRtl = (after & 1) == 1 || (after == 2 && i < n && isDigit(ch[i]) && base == 1);
				if (bRtl == aRtl) fill = (byte)(bRtl ? 1 : (rtlParagraph ? 2 : 0));
				else fill = base;
			}
			for (int k = s; k < i; k++)
				lv[k] = fill;
		}
		// 3) keep marks after their base letter once the run gets reversed
		int max = 0;
		for (i = 0; i < n; i++) {
			if (lv[i] > max) max = lv[i];
			if ((lv[i] & 1) == 1) {
				ch[i] = mirror(ch[i]);
				if (!isMark(ch[i]) && i + 1 < n && isMark(ch[i + 1])) {
					int e = i + 1;
					while (e < n && isMark(ch[e]))
						e++;
					reverse(ch, lv, i, e);
					i = e - 1;
				}
			}
		}
		if (max == 0) return line.toString();
		// 4) reverse runs from the highest level down
		for (int level = max; level >= 1; level--) {
			i = 0;
			while (i < n) {
				if (lv[i] < level) {
					i++;
					continue;
				}
				int s = i;
				while (i < n && lv[i] >= level)
					i++;
				reverse(ch, lv, s, i);
			}
		}
		return new String(ch, 0, n);
	}

	private static void reverse (char[] ch, byte[] lv, int start, int end) {
		for (int a = start, b = end - 1; a < b; a++, b--) {
			char t = ch[a];
			ch[a] = ch[b];
			ch[b] = t;
			byte l = lv[a];
			lv[a] = lv[b];
			lv[b] = l;
		}
	}
}
