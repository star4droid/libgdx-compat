package com.star4droid.compat.graphics;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeBitmapFontData;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;

/** Text and icon fonts. Glyphs are rasterised on demand (only the characters and sizes actually used) into the shared atlas,
 * so any language works without preparing a character list. */
public final class Fonts implements Disposable {
	private final Gfx gfx;
	private final FreeTypeFontGenerator regular, medium, icons;
	private final IntMap<BitmapFont> fonts = new IntMap<BitmapFont>();
	private final IntMap<BitmapFont> iconFonts = new IntMap<BitmapFont>();
	private final IntMap<Glyph> iconGlyphs = new IntMap<Glyph>();

	public Fonts (Gfx gfx, FileHandle regularFile, FileHandle mediumFile, FileHandle iconFile) {
		this.gfx = gfx;
		regular = new FreeTypeFontGenerator(regularFile);
		medium = mediumFile.equals(regularFile) ? regular : new FreeTypeFontGenerator(mediumFile);
		icons = new FreeTypeFontGenerator(iconFile);
	}

	private FreeTypeFontParameter parameter (int size) {
		FreeTypeFontParameter p = new FreeTypeFontParameter();
		p.size = size;
		p.incremental = true;
		p.characters = "";
		p.packer = gfx.packer();
		p.kerning = false;
		p.minFilter = TextureFilter.Linear;
		p.magFilter = TextureFilter.Linear;
		return p;
	}

	/** @param sizePx text size in pixels (see {@code Compat.sp})
	 * @param mediumWeight use the medium / bold face */
	public BitmapFont get (float sizePx, boolean mediumWeight) {
		int size = Math.max(6, Math.round(sizePx));
		int key = size * 2 + (mediumWeight ? 1 : 0);
		BitmapFont f = fonts.get(key);
		if (f == null) {
			f = (mediumWeight ? medium : regular).generateFont(parameter(size));
			f.setUseIntegerPositions(true);
			fonts.put(key, f);
		}
		return f;
	}

	private BitmapFont iconFont (int size) {
		BitmapFont f = iconFonts.get(size);
		if (f == null) {
			FreeTypeBitmapFontData data = new FreeTypeBitmapFontData();
			data.xChars = new char[] {''};
			data.capChars = new char[] {''};
			f = icons.generateFont(parameter(size), data);
			iconFonts.put(size, f);
		}
		return f;
	}

	/** Draws a Material icon inside the square (x, y, size, size).
	 * @param scale extra scale around the centre (1 = normal) */
	public void drawIcon (Batch b, char icon, float x, float y, float size, float scale, Color c, float alpha) {
		if (icon == 0) return;
		int px = Math.max(4, Math.round(size));
		int key = (px << 16) | icon;
		BitmapFont f = iconFont(px);
		Glyph g = iconGlyphs.get(key);
		if (g == null) {
			g = f.getData().getGlyph(icon);
			if (g == null) return;
			gfx.flushAtlas();
			iconGlyphs.put(key, g);
		}
		if (g.width == 0) return;
		b.setPackedColor(Color.toFloatBits(c.r, c.g, c.b, c.a * alpha));
		float gx = Math.round(x) + g.xoffset, gy = Math.round(y) + px + g.yoffset;
		if (scale != 1f) {
			float cx = x + size / 2f, cy = y + size / 2f;
			b.draw(f.getRegion(g.page).getTexture(), cx + (gx - cx) * scale, cy + (gy - cy) * scale, g.width * scale,
				g.height * scale, g.u, g.v, g.u2, g.v2);
		} else {
			b.draw(f.getRegion(g.page).getTexture(), gx, gy, g.width, g.height, g.u, g.v, g.u2, g.v2);
		}
	}

	/** Sum of the advances of the characters in [start, end). */
	public static float width (BitmapFont font, CharSequence text, int start, int end) {
		BitmapFont.BitmapFontData data = font.getData();
		float w = 0;
		for (int i = start; i < end; i++) {
			Glyph g = data.getGlyph(text.charAt(i));
			if (g != null) w += g.xadvance;
		}
		return w * data.scaleX;
	}

	@Override
	public void dispose () {
		// the atlas pages are owned by Gfx; only the generators are released here
		regular.dispose();
		if (medium != regular) medium.dispose();
		icons.dispose();
		fonts.clear();
		iconFonts.clear();
		iconGlyphs.clear();
	}
}
