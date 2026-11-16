package com.star4droid.compat.graphics;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;

/** Draws anti-aliased rounded rectangles, outlines, circles, shadows, lines and arcs with a plain {@link Batch}.
 * <p>
 * No custom shader and no image files: small corner / shadow tiles are generated once per radius into the same atlas page
 * that text and icons use, so a whole screen normally renders in one or two draw calls. */
public final class Gfx implements Disposable {
	/** Largest corner tile kept in the atlas; bigger radii reuse it scaled. */
	public static final int MAX_CORNER = 96;
	private static final int LINE_PROFILE = 64;

	static final class Tile {
		Texture tex;
		float u, v, u2, v2;
		/** A fully opaque texel inside the same texture, used for the straight parts. */
		float wu, wv;
	}

	static final class ShadowTiles {
		Tile corner, h, v;
		int size;
	}

	private final PixmapPacker packer;
	private final Tile white;
	private final IntMap<Tile> corners = new IntMap<Tile>();
	private final IntMap<Tile> rings = new IntMap<Tile>();
	private final IntMap<ShadowTiles> shadows = new IntMap<ShadowTiles>();
	private Tile line;
	private int packed;
	private final float[] quad = new float[40];
	private float[] poly = new float[20 * 80];

	public Gfx (int pageSize) {
		packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 2, false, new PixmapPacker.GuillotineStrategy());
		packer.setTransparentColor(new Color(1, 1, 1, 0));
		Pixmap pm = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
		pm.setColor(Color.WHITE);
		pm.fill();
		white = pack(pm, 1, 1, 2, 2);
		white.wu = (white.u + white.u2) * 0.5f;
		white.wv = (white.v + white.v2) * 0.5f;
	}

	public PixmapPacker packer () {
		return packer;
	}

	/** The atlas texture holding a white pixel (page 0). */
	public Texture whiteTexture () {
		return white.tex;
	}

	public float whiteU () {
		return white.wu;
	}

	public float whiteV () {
		return white.wv;
	}

	/** Copies a small image (avatar, thumbnail, custom icon) into the shared atlas and returns its region. Images drawn from
	 * the atlas do not break batching, so a list full of them still renders in one draw call. Keep them small: a page is
	 * {@code Config.atlasPageSize} pixels wide. The pixmap is not disposed. */
	public TextureRegion pack (Pixmap image) {
		int w = image.getWidth(), h = image.getHeight();
		// one pixel of duplicated border so that linear filtering never picks up a neighbour
		Pixmap pm = new Pixmap(w + 2, h + 2, Pixmap.Format.RGBA8888);
		pm.setBlending(Pixmap.Blending.None);
		pm.drawPixmap(image, 1, 1);
		pm.drawPixmap(image, 0, 0, w, 1, 1, 0, w, 1);
		pm.drawPixmap(image, 0, h - 1, w, 1, 1, h + 1, w, 1);
		pm.drawPixmap(image, 0, 0, 1, h, 0, 1, 1, h);
		pm.drawPixmap(image, w - 1, 0, 1, h, w + 1, 1, 1, h);
		Tile t = pack(pm, 1, 1, w, h);
		return new TextureRegion(t.tex, t.u, t.v, t.u2, t.v2);
	}

	/** Uploads pending atlas changes. Called automatically. */
	public void flushAtlas () {
		packer.updatePageTextures(TextureFilter.Linear, TextureFilter.Linear, false);
	}

	// PixmapPacker.pack() changed its return type between libGDX versions, so it is called reflectively (a handful of times
	// in the life of the app). If that ever fails the tiles go to a small private texture instead.
	private com.badlogic.gdx.utils.reflect.Method packMethod;
	private boolean packFailed;
	private Pixmap ownPixmap;
	private Texture ownTexture;
	private int ownX = 1, ownY = 1, ownRow;

	private Tile pack (Pixmap pm, int ix, int iy, int iw, int ih) {
		Tile t = new Tile();
		int rx = 0, ry = 0;
		if (!packFailed) {
			try {
				String name = "compat" + (packed++);
				if (packMethod == null)
					packMethod = com.badlogic.gdx.utils.reflect.ClassReflection.getMethod(PixmapPacker.class, "pack", String.class,
						Pixmap.class);
				Object r = packMethod.invoke(packer, name, pm);
				rx = ((Number)com.badlogic.gdx.utils.reflect.ClassReflection.getMethod(r.getClass(), "getX").invoke(r)).intValue();
				ry = ((Number)com.badlogic.gdx.utils.reflect.ClassReflection.getMethod(r.getClass(), "getY").invoke(r)).intValue();
				PixmapPacker.Page page = packer.getPage(name);
				page.updateTexture(TextureFilter.Linear, TextureFilter.Linear, false);
				packer.setPackToTexture(true);
				t.tex = page.getTexture();
			} catch (Throwable e) {
				packFailed = true;
				com.badlogic.gdx.Gdx.app.error("Compat", "Shared atlas unavailable, using a separate shape texture", e);
			}
		}
		if (packFailed) {
			if (ownPixmap == null) {
				ownPixmap = new Pixmap(1024, 1024, Pixmap.Format.RGBA8888);
				ownPixmap.setBlending(Pixmap.Blending.None);
				ownPixmap.setColor(1, 1, 1, 0);
				ownPixmap.fill();
				ownTexture = new Texture(new com.badlogic.gdx.graphics.glutils.PixmapTextureData(ownPixmap, null, false, false, true));
				ownTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			}
			if (ownX + pm.getWidth() + 2 > 1024) {
				ownX = 1;
				ownY += ownRow + 2;
				ownRow = 0;
			}
			rx = ownX;
			ry = ownY;
			ownX += pm.getWidth() + 2;
			ownRow = Math.max(ownRow, pm.getHeight());
			ownPixmap.drawPixmap(pm, rx, ry);
			ownTexture.load(ownTexture.getTextureData());
			t.tex = ownTexture;
		}
		pm.dispose();
		float w = t.tex.getWidth(), h = t.tex.getHeight();
		t.u = (rx + ix) / w;
		t.v = (ry + iy) / h;
		t.u2 = (rx + ix + iw) / w;
		t.v2 = (ry + iy + ih) / h;
		return t;
	}

	private static Pixmap alphaPixmap (int w, int h, float[] alpha) {
		Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
		ByteBuffer buf = pm.getPixels();
		buf.position(0);
		for (int i = 0, n = w * h; i < n; i++) {
			buf.put((byte)255).put((byte)255).put((byte)255).put((byte)(int)(MathUtils.clamp(alpha[i], 0f, 1f) * 255f + 0.5f));
		}
		buf.position(0);
		return pm;
	}

	private Tile corner (int r) {
		if (r < 1) r = 1;
		if (r > MAX_CORNER) r = MAX_CORNER;
		Tile t = corners.get(r);
		if (t != null) return t;
		int s = r + 2;
		float[] a = new float[s * s];
		for (int j = 0; j < s; j++) {
			for (int i = 0; i < s; i++) {
				float dx = r - (i - 0.5f), dy = r - (j - 0.5f);
				float d = (float)Math.sqrt(dx * dx + dy * dy);
				a[j * s + i] = (dx <= 0 || dy <= 0) ? 1f : r - d + 0.5f;
			}
		}
		t = pack(alphaPixmap(s, s, a), 1, 1, r, r);
		float tw = t.tex.getWidth(), th = t.tex.getHeight();
		// the border texel on the centre side is always opaque
		t.wu = t.u2 + 0.5f / tw;
		t.wv = t.v2 + 0.5f / th;
		corners.put(r, t);
		return t;
	}

	private Tile ring (int r, int w) {
		if (r > MAX_CORNER) r = MAX_CORNER;
		if (w < 1) w = 1;
		if (r < w) r = w;
		int key = r * 256 + w;
		Tile t = rings.get(key);
		if (t != null) return t;
		int s = r + 2;
		float[] a = new float[s * s];
		for (int j = 0; j < s; j++) {
			for (int i = 0; i < s; i++) {
				float dx = Math.max(0, r - (i - 0.5f)), dy = Math.max(0, r - (j - 0.5f));
				float d = (float)Math.sqrt(dx * dx + dy * dy);
				a[j * s + i] = MathUtils.clamp(r - d + 0.5f, 0, 1) * MathUtils.clamp(d - (r - w) + 0.5f, 0, 1);
			}
		}
		t = pack(alphaPixmap(s, s, a), 1, 1, r, r);
		float tw = t.tex.getWidth(), th = t.tex.getHeight();
		// centre-side border column, first row: lies in the middle of the stroke band
		t.wu = t.u2 + 0.5f / tw;
		t.wv = t.v + 0.5f / th;
		rings.put(key, t);
		return t;
	}

	private static float shadowFalloff (float d, float blur) {
		float t = MathUtils.clamp((d + blur) / (2f * blur), 0f, 1f);
		t = t * t * (3f - 2f * t);
		return 1f - t;
	}

	private ShadowTiles shadowTiles (int r, int blur) {
		if (r > MAX_CORNER) r = MAX_CORNER;
		if (blur < 1) blur = 1;
		if (blur > 48) blur = 48;
		int key = r * 64 + blur;
		ShadowTiles st = shadows.get(key);
		if (st != null) return st;
		int in = Math.max(r, blur), size = blur + in;
		float[] a = new float[size * size];
		for (int j = 0; j < size; j++) {
			for (int i = 0; i < size; i++) {
				float px = i + 0.5f - blur, py = j + 0.5f - blur;
				float qx = r - px, qy = r - py;
				float d = (qx > 0 && qy > 0) ? (float)Math.sqrt(qx * qx + qy * qy) - r : Math.max(qx, qy) - r;
				a[j * size + i] = shadowFalloff(d, blur);
			}
		}
		st = new ShadowTiles();
		st.size = size;
		st.corner = pack(alphaPixmap(size, size, a), 0, 0, size, size);
		float tw = st.corner.tex.getWidth(), th = st.corner.tex.getHeight();
		st.corner.wu = st.corner.u2 - 0.5f / tw;
		st.corner.wv = st.corner.v2 - 0.5f / th;
		float[] hs = new float[size * 3], vs = new float[3 * size];
		for (int i = 0; i < size; i++) {
			float f = shadowFalloff(-(i + 0.5f - blur), blur);
			hs[i] = hs[size + i] = hs[2 * size + i] = f;
			vs[i * 3] = vs[i * 3 + 1] = vs[i * 3 + 2] = f;
		}
		st.h = pack(alphaPixmap(size, 3, hs), 0, 1, size, 1);
		st.h.wv = (st.h.v + st.h.v2) * 0.5f;
		st.v = pack(alphaPixmap(3, size, vs), 1, 0, 1, size);
		st.v.wu = (st.v.u + st.v.u2) * 0.5f;
		shadows.put(key, st);
		return st;
	}

	private Tile lineTile () {
		if (line == null) {
			float[] a = new float[LINE_PROFILE * 3];
			for (int i = 1; i < LINE_PROFILE; i++)
				a[i] = a[LINE_PROFILE + i] = a[2 * LINE_PROFILE + i] = 1f;
			line = pack(alphaPixmap(LINE_PROFILE, 3, a), 0, 1, LINE_PROFILE, 1);
			line.wv = (line.v + line.v2) * 0.5f;
		}
		return line;
	}

	private static float pack (Color c, float alpha) {
		return Color.toFloatBits(c.r, c.g, c.b, c.a * alpha);
	}

	// ------------------------------------------------------------------ filled shapes

	/** Plain rectangle. */
	public void rect (Batch b, float x, float y, float w, float h, Color c, float alpha) {
		if (w <= 0 || h <= 0) return;
		b.setPackedColor(pack(c, alpha));
		b.draw(white.tex, x, y, w, h, white.wu, white.wv, white.wu, white.wv);
	}

	/** Rounded rectangle with the same radius on every corner (a radius of half the size gives a pill / circle). */
	public void roundRect (Batch b, float x, float y, float w, float h, float r, Color c, float alpha) {
		roundRectRaw(b, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, pack(c, alpha));
	}

	/** Same as {@link #roundRect} but without snapping to whole pixels (for shapes that are being animated). */
	public void roundRectSmooth (Batch b, float x, float y, float w, float h, float r, Color c, float alpha) {
		roundRectRaw(b, x, y, w, h, r, pack(c, alpha));
	}

	private void roundRectRaw (Batch b, float x, float y, float w, float h, float r, float color) {
		if (w <= 0 || h <= 0) return;
		float m = Math.min(w, h) * 0.5f;
		if (r > m) r = m;
		b.setPackedColor(color);
		if (r < 0.75f) {
			b.draw(white.tex, x, y, w, h, white.wu, white.wv, white.wu, white.wv);
			return;
		}
		Tile t = corner(Math.round(r));
		Texture tex = t.tex;
		float wu = t.wu, wv = t.wv;
		if (h - 2 * r > 0) b.draw(tex, x, y + r, w, h - 2 * r, wu, wv, wu, wv);
		if (w - 2 * r > 0) {
			b.draw(tex, x + r, y + h - r, w - 2 * r, r, wu, wv, wu, wv);
			b.draw(tex, x + r, y, w - 2 * r, r, wu, wv, wu, wv);
		}
		b.draw(tex, x, y + h - r, r, r, t.u, t.v2, t.u2, t.v);
		b.draw(tex, x + w - r, y + h - r, r, r, t.u2, t.v2, t.u, t.v);
		b.draw(tex, x, y, r, r, t.u, t.v, t.u2, t.v2);
		b.draw(tex, x + w - r, y, r, r, t.u2, t.v, t.u, t.v2);
	}

	/** Rounded rectangle with a radius per corner (top-left, top-right, bottom-right, bottom-left). */
	public void roundRect (Batch b, float x, float y, float w, float h, float tl, float tr, float br, float bl, Color c,
		float alpha) {
		x = Math.round(x);
		y = Math.round(y);
		w = Math.round(w);
		h = Math.round(h);
		if (w <= 0 || h <= 0) return;
		if (tl == tr && tr == br && br == bl) {
			roundRectRaw(b, x, y, w, h, tl, pack(c, alpha));
			return;
		}
		float m = Math.min(w, h) * 0.5f;
		tl = Math.min(tl, m);
		tr = Math.min(tr, m);
		br = Math.min(br, m);
		bl = Math.min(bl, m);
		float top = Math.max(tl, tr), bot = Math.max(bl, br);
		b.setPackedColor(pack(c, alpha));
		Texture wt = white.tex;
		float wu = white.wu, wv = white.wv;
		b.draw(wt, x, y + bot, w, h - bot - top, wu, wv, wu, wv);
		if (top > 0) {
			b.draw(wt, x + tl, y + h - top, w - tl - tr, top, wu, wv, wu, wv);
			cornerPiece(b, x, y + h - top, tl, top, 0);
			cornerPiece(b, x + w - tr, y + h - top, tr, top, 1);
		}
		if (bot > 0) {
			b.draw(wt, x + bl, y, w - bl - br, bot, wu, wv, wu, wv);
			cornerPiece(b, x, y, bl, bot, 2);
			cornerPiece(b, x + w - br, y, br, bot, 3);
		}
	}

	/** Draws one corner of radius r inside a band of the given height; kind 0=TL 1=TR 2=BL 3=BR. */
	private void cornerPiece (Batch b, float x, float y, float r, float band, int kind) {
		if (r < 0.75f) return;
		Tile t = corner(Math.round(r));
		boolean topSide = kind < 2;
		float cy = topSide ? y + band - r : y;
		if (band - r > 0) b.draw(t.tex, x, topSide ? y : y + r, r, band - r, t.wu, t.wv, t.wu, t.wv);
		switch (kind) {
		case 0:
			b.draw(t.tex, x, cy, r, r, t.u, t.v2, t.u2, t.v);
			break;
		case 1:
			b.draw(t.tex, x, cy, r, r, t.u2, t.v2, t.u, t.v);
			break;
		case 2:
			b.draw(t.tex, x, cy, r, r, t.u, t.v, t.u2, t.v2);
			break;
		default:
			b.draw(t.tex, x, cy, r, r, t.u2, t.v, t.u, t.v2);
		}
	}

	public void circle (Batch b, float cx, float cy, float radius, Color c, float alpha) {
		roundRectRaw(b, cx - radius, cy - radius, radius * 2, radius * 2, radius, pack(c, alpha));
	}

	// ------------------------------------------------------------------ outlines

	/** Outline of a rounded rectangle, drawn inside the given bounds. */
	public void stroke (Batch b, float x, float y, float w, float h, float r, float strokeWidth, Color c, float alpha) {
		strokeRaw(b, Math.round(x), Math.round(y), Math.round(w), Math.round(h), r, Math.max(1, Math.round(strokeWidth)),
			pack(c, alpha));
	}

	private void strokeRaw (Batch b, float x, float y, float w, float h, float r, float sw, float color) {
		if (w <= 0 || h <= 0) return;
		float m = Math.min(w, h) * 0.5f;
		if (r > m) r = m;
		b.setPackedColor(color);
		if (r < sw + 0.5f) {
			Texture wt = white.tex;
			float wu = white.wu, wv = white.wv;
			b.draw(wt, x, y + h - sw, w, sw, wu, wv, wu, wv);
			b.draw(wt, x, y, w, sw, wu, wv, wu, wv);
			b.draw(wt, x, y + sw, sw, h - 2 * sw, wu, wv, wu, wv);
			b.draw(wt, x + w - sw, y + sw, sw, h - 2 * sw, wu, wv, wu, wv);
			return;
		}
		Tile t = ring(Math.round(r), Math.round(sw));
		Texture tex = t.tex;
		float wu = t.wu, wv = t.wv;
		if (w - 2 * r > 0) {
			b.draw(tex, x + r, y + h - sw, w - 2 * r, sw, wu, wv, wu, wv);
			b.draw(tex, x + r, y, w - 2 * r, sw, wu, wv, wu, wv);
		}
		if (h - 2 * r > 0) {
			b.draw(tex, x, y + r, sw, h - 2 * r, wu, wv, wu, wv);
			b.draw(tex, x + w - sw, y + r, sw, h - 2 * r, wu, wv, wu, wv);
		}
		b.draw(tex, x, y + h - r, r, r, t.u, t.v2, t.u2, t.v);
		b.draw(tex, x + w - r, y + h - r, r, r, t.u2, t.v2, t.u, t.v);
		b.draw(tex, x, y, r, r, t.u, t.v, t.u2, t.v2);
		b.draw(tex, x + w - r, y, r, r, t.u2, t.v, t.u, t.v2);
	}

	public void ring (Batch b, float cx, float cy, float radius, float strokeWidth, Color c, float alpha) {
		strokeRaw(b, cx - radius, cy - radius, radius * 2, radius * 2, radius, Math.max(1, Math.round(strokeWidth)),
			pack(c, alpha));
	}

	// ------------------------------------------------------------------ shadows

	/** Soft drop shadow for a rounded rectangle. Draw it before the shape itself.
	 * @param elevation elevation in pixels (use {@code Compat.dp(...)}); 0 draws nothing */
	public void shadow (Batch b, float x, float y, float w, float h, float r, float elevation, Color c, float alpha) {
		if (elevation <= 0 || w <= 0 || h <= 0) return;
		x = Math.round(x);
		y = Math.round(y);
		w = Math.round(w);
		h = Math.round(h);
		int blur = Math.max(2, Math.round(elevation * 1.6f));
		float oy = Math.round(elevation * 0.55f);
		float m = Math.min(w, h) * 0.5f;
		if (r > m) r = m;
		ShadowTiles st = shadowTiles(Math.round(r), blur);
		float strength = Math.min(0.34f, 0.16f + elevation * 0.012f);
		b.setPackedColor(Color.toFloatBits(c.r, c.g, c.b, c.a * alpha * strength));
		float x0 = x - blur, y0 = y - blur - oy, W = w + 2 * blur, H = h + 2 * blur;
		float t = Math.min(st.size, Math.min(W, H) * 0.5f);
		Tile ct = st.corner, ht = st.h, vt = st.v;
		Texture tex = ct.tex;
		// corners
		b.draw(tex, x0, y0 + H - t, t, t, ct.u, ct.v2, ct.u2, ct.v);
		b.draw(tex, x0 + W - t, y0 + H - t, t, t, ct.u2, ct.v2, ct.u, ct.v);
		b.draw(tex, x0, y0, t, t, ct.u, ct.v, ct.u2, ct.v2);
		b.draw(tex, x0 + W - t, y0, t, t, ct.u2, ct.v, ct.u, ct.v2);
		float iw = W - 2 * t, ih = H - 2 * t;
		if (ih > 0) {
			b.draw(ht.tex, x0, y0 + t, t, ih, ht.u, ht.wv, ht.u2, ht.wv);
			b.draw(ht.tex, x0 + W - t, y0 + t, t, ih, ht.u2, ht.wv, ht.u, ht.wv);
		}
		if (iw > 0) {
			b.draw(vt.tex, x0 + t, y0 + H - t, iw, t, vt.wu, vt.v2, vt.wu, vt.v);
			b.draw(vt.tex, x0 + t, y0, iw, t, vt.wu, vt.v, vt.wu, vt.v2);
		}
		if (iw > 0 && ih > 0) b.draw(tex, x0 + t, y0 + t, iw, ih, ct.wu, ct.wv, ct.wu, ct.wv);
	}

	// ------------------------------------------------------------------ lines and arcs

	private void halfQuad (float[] q, int o, float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy,
		float color, float uEdge, float uMid, float v) {
		q[o] = ax;
		q[o + 1] = ay;
		q[o + 2] = color;
		q[o + 3] = uEdge;
		q[o + 4] = v;
		q[o + 5] = bx;
		q[o + 6] = by;
		q[o + 7] = color;
		q[o + 8] = uEdge;
		q[o + 9] = v;
		q[o + 10] = cx;
		q[o + 11] = cy;
		q[o + 12] = color;
		q[o + 13] = uMid;
		q[o + 14] = v;
		q[o + 15] = dx;
		q[o + 16] = dy;
		q[o + 17] = color;
		q[o + 18] = uMid;
		q[o + 19] = v;
	}

	/** Anti-aliased line with round caps. */
	public void line (Batch b, float x1, float y1, float x2, float y2, float thickness, Color c, float alpha) {
		float dx = x2 - x1, dy = y2 - y1;
		float len = (float)Math.sqrt(dx * dx + dy * dy);
		if (len < 0.001f) return;
		Tile t = lineTile();
		float half = Math.min(thickness * 0.5f + 0.5f, LINE_PROFILE - 2);
		float nx = -dy / len * half, ny = dx / len * half;
		float color = pack(c, alpha);
		float tw = t.tex.getWidth();
		float uE = t.u + 0.5f / tw, uM = t.u + (0.5f + half) / tw;
		halfQuad(quad, 0, x1 + nx, y1 + ny, x2 + nx, y2 + ny, x2, y2, x1, y1, color, uE, uM, t.wv);
		halfQuad(quad, 20, x1 - nx, y1 - ny, x2 - nx, y2 - ny, x2, y2, x1, y1, color, uE, uM, t.wv);
		b.draw(t.tex, quad, 0, 40);
		if (thickness >= 2.5f) {
			roundRectRaw(b, x1 - thickness / 2, y1 - thickness / 2, thickness, thickness, thickness / 2, color);
			roundRectRaw(b, x2 - thickness / 2, y2 - thickness / 2, thickness, thickness, thickness / 2, color);
		}
	}

	/** Anti-aliased circular arc. Angles in degrees, counter-clockwise, 0 = right.
	 * @param roundCaps draw rounded ends */
	public void arc (Batch b, float cx, float cy, float radius, float thickness, float startDeg, float sweepDeg, Color c,
		float alpha, boolean roundCaps) {
		if (sweepDeg == 0) return;
		Tile t = lineTile();
		float half = Math.min(thickness * 0.5f + 0.5f, LINE_PROFILE - 2);
		float color = pack(c, alpha);
		float tw = t.tex.getWidth();
		float uE = t.u + 0.5f / tw, uM = t.u + (0.5f + half) / tw;
		int segs = Math.max(2, (int)Math.ceil(Math.abs(sweepDeg) / 360f * Math.max(16, radius * 0.9f + 12)));
		float step = sweepDeg / segs * MathUtils.degreesToRadians, a = startDeg * MathUtils.degreesToRadians;
		float ro = radius + half, ri = radius - half;
		float c0 = MathUtils.cos(a), s0 = MathUtils.sin(a);
		for (int i = 0; i < segs; i++) {
			float a1 = a + step;
			float c1 = MathUtils.cos(a1), s1 = MathUtils.sin(a1);
			halfQuad(quad, 0, cx + c0 * ro, cy + s0 * ro, cx + c1 * ro, cy + s1 * ro, cx + c1 * radius, cy + s1 * radius,
				cx + c0 * radius, cy + s0 * radius, color, uE, uM, t.wv);
			halfQuad(quad, 20, cx + c0 * ri, cy + s0 * ri, cx + c1 * ri, cy + s1 * ri, cx + c1 * radius, cy + s1 * radius,
				cx + c0 * radius, cy + s0 * radius, color, uE, uM, t.wv);
			b.draw(t.tex, quad, 0, 40);
			a = a1;
			c0 = c1;
			s0 = s1;
		}
		if (roundCaps && thickness >= 2.5f) {
			float sa = startDeg * MathUtils.degreesToRadians, ea = (startDeg + sweepDeg) * MathUtils.degreesToRadians;
			float hr = thickness / 2;
			roundRectRaw(b, cx + MathUtils.cos(sa) * radius - hr, cy + MathUtils.sin(sa) * radius - hr, thickness, thickness, hr,
				color);
			roundRectRaw(b, cx + MathUtils.cos(ea) * radius - hr, cy + MathUtils.sin(ea) * radius - hr, thickness, thickness, hr,
				color);
		}
	}

	// ------------------------------------------------------------------ images

	/** Draws a texture region clipped to a rounded rectangle (anti-aliased, no stencil or scissor needed). */
	public void image (Batch b, TextureRegion region, float x, float y, float w, float h, float r, Color tint, float alpha) {
		image(b, region.getTexture(), region.getU(), region.getV(), region.getU2(), region.getV2(), x, y, w, h, r, tint, alpha);
	}

	/** @param u left, v top, u2 right, v2 bottom texture coordinates */
	public void image (Batch b, Texture tex, float u, float v, float u2, float v2, float x, float y, float w, float h, float r,
		Color tint, float alpha) {
		if (w <= 0 || h <= 0) return;
		float m = Math.min(w, h) * 0.5f;
		if (r > m) r = m;
		if (r < 0.75f) {
			b.setPackedColor(pack(tint, alpha));
			b.draw(tex, x, y, w, h, u, v2, u2, v);
			return;
		}
		int k = MathUtils.clamp((int)(r / 2.5f) + 2, 3, 12);
		int n = 4 * (k + 1);
		if (poly.length < n * 40) poly = new float[n * 40];
		float solid = pack(tint, alpha), clear = Color.toFloatBits(tint.r, tint.g, tint.b, 0f);
		float cxm = x + w / 2, cym = y + h / 2, cu = (u + u2) / 2, cv = (v + v2) / 2;
		float ri = Math.max(0, r - 0.5f), ro = r + 0.5f;
		float px = 0, py = 0, pox = 0, poy = 0, fx = 0, fy = 0, fox = 0, foy = 0;
		int o = 0;
		for (int i = 0; i <= n; i++) {
			float ix, iy, ox, oy;
			if (i == n) {
				ix = fx;
				iy = fy;
				ox = fox;
				oy = foy;
			} else {
				int cornerIdx = i / (k + 1), s = i % (k + 1);
				// corner centres: 0 = top-right, 1 = top-left, 2 = bottom-left, 3 = bottom-right (counter-clockwise)
				float ccx = (cornerIdx == 0 || cornerIdx == 3) ? x + w - r : x + r;
				float ccy = (cornerIdx < 2) ? y + h - r : y + r;
				float ang = (cornerIdx * 90f + 90f * s / k) * MathUtils.degreesToRadians;
				float ca = MathUtils.cos(ang), sa = MathUtils.sin(ang);
				ix = ccx + ca * ri;
				iy = ccy + sa * ri;
				ox = ccx + ca * ro;
				oy = ccy + sa * ro;
			}
			if (i == 0) {
				fx = ix;
				fy = iy;
				fox = ox;
				foy = oy;
			} else {
				// fan triangle (centre, previous inner, inner) as a degenerate quad
				o = vert(poly, o, cxm, cym, solid, cu, cv);
				o = vert(poly, o, px, py, solid, u + (px - x) / w * (u2 - u), v2 + (py - y) / h * (v - v2));
				o = vert(poly, o, ix, iy, solid, u + (ix - x) / w * (u2 - u), v2 + (iy - y) / h * (v - v2));
				o = vert(poly, o, ix, iy, solid, u + (ix - x) / w * (u2 - u), v2 + (iy - y) / h * (v - v2));
				// anti-aliasing fringe
				o = vert(poly, o, px, py, solid, u + (px - x) / w * (u2 - u), v2 + (py - y) / h * (v - v2));
				o = vert(poly, o, pox, poy, clear, u + (pox - x) / w * (u2 - u), v2 + (poy - y) / h * (v - v2));
				o = vert(poly, o, ox, oy, clear, u + (ox - x) / w * (u2 - u), v2 + (oy - y) / h * (v - v2));
				o = vert(poly, o, ix, iy, solid, u + (ix - x) / w * (u2 - u), v2 + (iy - y) / h * (v - v2));
			}
			px = ix;
			py = iy;
			pox = ox;
			poy = oy;
		}
		b.draw(tex, poly, 0, o);
	}

	private static int vert (float[] a, int o, float x, float y, float color, float u, float v) {
		a[o] = x;
		a[o + 1] = y;
		a[o + 2] = color;
		a[o + 3] = u;
		a[o + 4] = v;
		return o + 5;
	}

	@Override
	public void dispose () {
		packer.dispose();
		if (ownTexture != null) ownTexture.dispose();
		if (ownPixmap != null) ownPixmap.dispose();
	}
}
