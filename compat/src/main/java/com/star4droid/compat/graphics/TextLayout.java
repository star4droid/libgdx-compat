package com.star4droid.compat.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.star4droid.compat.Compat;

/** A block of text that is measured, wrapped, shaped (Arabic / RTL) and cached once, then drawn cheaply every frame. Used
 * by every widget that shows text. */
public final class TextLayout {
	public static final int ALIGN_START = 0, ALIGN_CENTER = 1, ALIGN_END = 2, ALIGN_LEFT = 3, ALIGN_RIGHT = 4;
	private static final Color TMP = new Color();
	private static final Array<String> LINES = new Array<String>();

	private CharSequence text = "";
	private String shaped = "";
	private float sizePx;
	private boolean medium;
	private int maxLines = Integer.MAX_VALUE;
	private int align = ALIGN_START;
	private boolean rtl, hasRtl, hasMarks, marksReady;

	private BitmapFont font;
	private float naturalWidth = -1;
	// result of the last wrap
	private float wrapWidth = -1;
	private String visual = "";
	private int lineCount = 1;
	private float layoutWidth;
	private boolean cacheDirty = true;
	private BitmapFontCache cache;
	private final GlyphLayout glyphs = new GlyphLayout();
	private float drawnWidth = -1;

	public TextLayout (float sizePx, boolean medium) {
		this.sizePx = sizePx;
		this.medium = medium;
	}

	public void setText (CharSequence t) {
		if (t == null) t = "";
		if (text.equals(t)) return;
		text = t;
		hasRtl = TextShaper.hasRtl(t);
		rtl = hasRtl && TextShaper.isRtlParagraph(t);
		shaped = hasRtl ? TextShaper.shape(t) : t.toString();
		hasMarks = false;
		if (hasRtl) for (int i = 0, n = shaped.length(); i < n; i++)
			if (TextShaper.isMark(shaped.charAt(i))) {
				hasMarks = true;
				break;
			}
		dirty();
	}

	public CharSequence getText () {
		return text;
	}

	public void setSize (float px) {
		if (px == sizePx) return;
		sizePx = px;
		font = null;
		cache = null;
		dirty();
	}

	public float getSize () {
		return sizePx;
	}

	public void setMedium (boolean m) {
		if (m == medium) return;
		medium = m;
		font = null;
		cache = null;
		dirty();
	}

	public void setMaxLines (int n) {
		if (n <= 0) n = Integer.MAX_VALUE;
		if (n == maxLines) return;
		maxLines = n;
		dirty();
	}

	public void setAlign (int a) {
		if (a == align) return;
		align = a;
		cacheDirty = true;
	}

	/** @return true when the paragraph direction is right-to-left. */
	public boolean isRtl () {
		return rtl;
	}

	/** Registers the alias glyphs (lowered / raised copies of marks) this text needs on the current font. */
	private void prepareMarks () {
		if (!hasMarks || marksReady) return;
		marksReady = true;
		BitmapFont.BitmapFontData data = font().getData();
		for (int i = 0, n = shaped.length(); i < n; i++) {
			char c = shaped.charAt(i);
			char src = TextShaper.aliasSource(c);
			if (src == 0 || data.getGlyph(c) != null) continue;
			BitmapFont.Glyph g = data.getGlyph(src);
			if (g == null) continue;
			BitmapFont.Glyph a = new BitmapFont.Glyph();
			a.id = c;
			a.srcX = g.srcX;
			a.srcY = g.srcY;
			a.width = g.width;
			a.height = g.height;
			a.u = g.u;
			a.v = g.v;
			a.u2 = g.u2;
			a.v2 = g.v2;
			a.xoffset = g.xoffset;
			a.xadvance = g.xadvance;
			a.page = g.page;
			float em = Math.round(sizePx);
			a.yoffset = g.yoffset + Math.round(c < TextShaper.ALIAS_RAISED ? -0.6f * em : 0.2f * em);
			data.setGlyph(c, a);
		}
	}

	/** Centres combining marks over their base letter (the font's own mark tables are not available to libGDX). */
	private void positionMarks () {
		for (int r = 0; r < glyphs.runs.size; r++) {
			GlyphLayout.GlyphRun run = glyphs.runs.get(r);
			Array<BitmapFont.Glyph> gl = run.glyphs;
			com.badlogic.gdx.utils.FloatArray adv = run.xAdvances;
			if (adv.size < gl.size + 1) continue;
			float[] a = adv.items;
			float pos = a[0], prevNew = 0, basePos = pos;
			BitmapFont.Glyph base = null;
			for (int k = 0; k < gl.size; k++) {
				BitmapFont.Glyph g = gl.get(k);
				float advance = a[k + 1];
				float newPos;
				if (TextShaper.isMark((char)g.id) && base != null) {
					newPos = basePos + base.xoffset + (base.width - g.width) / 2f - g.xoffset;
				} else {
					newPos = pos;
					base = g;
					basePos = pos;
					pos += advance;
				}
				a[k] = k == 0 ? newPos : newPos - prevNew;
				prevNew = newPos;
			}
		}
	}

	private void dirty () {
		marksReady = false;
		naturalWidth = -1;
		wrapWidth = -1;
		cacheDirty = true;
	}

	public BitmapFont font () {
		if (font == null) font = Compat.fonts().get(sizePx, medium);
		return font;
	}

	public float lineHeight () {
		return font().getLineHeight();
	}

	/** Width of the text when nothing is wrapped (the widest paragraph). */
	public float prefWidth () {
		if (naturalWidth < 0) {
			BitmapFont f = font();
			prepareMarks();
			float max = 0;
			int start = 0, n = shaped.length();
			for (int i = 0; i <= n; i++) {
				if (i == n || shaped.charAt(i) == '\n') {
					max = Math.max(max, Fonts.width(f, shaped, start, i));
					start = i + 1;
				}
			}
			naturalWidth = (float)Math.ceil(max) + 1;
		}
		return naturalWidth;
	}

	/** Height of the text when wrapped into the given width. */
	public float heightFor (float width) {
		wrap(width);
		return lineCount * lineHeight();
	}

	public float height () {
		return lineCount * lineHeight();
	}

	public int lineCount () {
		return lineCount;
	}

	/** Width of the widest line after the last wrap. */
	public float width () {
		return layoutWidth;
	}

	/** Wraps the text for the given width. Cheap when nothing changed. */
	public void wrap (float width) {
		if (width < 1) width = 1;
		if (wrapWidth == width) return;
		wrapWidth = width;
		cacheDirty = true;
		BitmapFont f = font();
		prepareMarks();
		String s = shaped;
		int n = s.length();
		// fast path: one line that fits
		if (s.indexOf('\n') < 0 && prefWidth() <= width + 0.5f) {
			visual = hasRtl ? TextShaper.reorder(s, rtl) : s;
			lineCount = 1;
			layoutWidth = prefWidth();
			return;
		}
		Array<String> lines = LINES;
		lines.clear();
		BitmapFont.BitmapFontData data = f.getData();
		int lineStart = 0, lastBreak = -1;
		float w = 0, wAtBreak = 0;
		boolean truncated = false;
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			if (c == '\n') {
				lines.add(s.substring(lineStart, i));
				lineStart = i + 1;
				lastBreak = -1;
				w = 0;
			} else {
				BitmapFont.Glyph g = data.getGlyph(c);
				float adv = g == null ? 0 : g.xadvance * data.scaleX;
				if (w + adv > width && i > lineStart && c != ' ') {
					if (lastBreak > lineStart) {
						lines.add(s.substring(lineStart, lastBreak));
						lineStart = lastBreak + 1;
						w -= wAtBreak;
					} else {
						lines.add(s.substring(lineStart, i));
						lineStart = i;
						w = 0;
					}
					lastBreak = -1;
				}
				if (c == ' ') {
					lastBreak = i;
					wAtBreak = w + adv;
				}
				w += adv;
			}
			if (lines.size >= maxLines) {
				truncated = true;
				break;
			}
		}
		if (!truncated) lines.add(s.substring(lineStart));
		else {
			// the last allowed line gets an ellipsis
			int last = lines.size - 1;
			String l = lines.get(last);
			float ell = Fonts.width(f, "…", 0, 1);
			int end = l.length();
			while (end > 0 && Fonts.width(f, l, 0, end) + ell > width)
				end--;
			while (end > 0 && l.charAt(end - 1) == ' ')
				end--;
			lines.set(last, l.substring(0, end) + "…");
		}
		if (lines.size > maxLines) lines.truncate(maxLines);
		StringBuilder sb = new StringBuilder(n + 8);
		float max = 0;
		for (int i = 0; i < lines.size; i++) {
			String l = lines.get(i);
			max = Math.max(max, Fonts.width(f, l, 0, l.length()));
			if (i > 0) sb.append('\n');
			sb.append(hasRtl ? TextShaper.reorder(l, rtl) : l);
		}
		visual = sb.toString();
		lineCount = Math.max(1, lines.size);
		layoutWidth = Math.min(width, (float)Math.ceil(max) + 1);
		lines.clear();
	}

	/** Shortens a single line with an ellipsis when it is wider than the given width (for labels that must not wrap). */
	public void fitSingleLine (float width) {
		int saved = maxLines;
		maxLines = 1;
		wrapWidth = -1;
		wrap(width);
		maxLines = saved;
	}

	/** Draws the text inside a box whose left edge is x, top edge is top and width is the wrap width (or any wider box).
	 * Call {@link #wrap(float)} first. */
	public void draw (Batch b, float x, float top, float boxWidth, Color color, float alpha) {
		if (visual.length() == 0) return;
		BitmapFont f = font();
		if (cache == null) {
			cache = new BitmapFontCache(f, true);
			cacheDirty = true;
		}
		if (cacheDirty || drawnWidth != boxWidth) {
			int a;
			switch (align) {
			case ALIGN_CENTER:
				a = Align.center;
				break;
			case ALIGN_END:
				a = rtl ? Align.left : Align.right;
				break;
			case ALIGN_LEFT:
				a = Align.left;
				break;
			case ALIGN_RIGHT:
				a = Align.right;
				break;
			default:
				a = rtl ? Align.right : Align.left;
			}
			glyphs.setText(f, visual, 0, visual.length(), Color.WHITE, boxWidth, a, false, null);
			if (hasMarks) positionMarks();
			cache.setText(glyphs, 0, 0);
			cacheDirty = false;
			drawnWidth = boxWidth;
		}
		TMP.set(color);
		TMP.a *= alpha;
		cache.tint(TMP);
		cache.setPosition(Math.round(x), Math.round(top - f.getAscent()));
		cache.draw(b);
	}
}
