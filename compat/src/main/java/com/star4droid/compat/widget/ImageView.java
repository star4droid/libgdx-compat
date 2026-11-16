package com.star4droid.compat.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.star4droid.compat.Compat;

/** Shows a texture region with a scale type and optional rounded corners (use a radius of half the size for a circle). */
public class ImageView extends View {
	public static final int FIT_CENTER = 0, CENTER_CROP = 1, FIT_XY = 2, CENTER = 3;

	private TextureRegion region;
	private int scaleType = FIT_CENTER;
	private float radius;
	private Color tint = Color.WHITE, placeholder;
	private float prefW = -1, prefH = -1;

	public ImageView () {
	}

	public ImageView (TextureRegion region) {
		this.region = region;
	}

	public ImageView (Texture texture) {
		this.region = new TextureRegion(texture);
	}

	public ImageView setImage (TextureRegion r) {
		region = r;
		invalidateHierarchy();
		Compat.requestRender();
		return this;
	}

	public ImageView setImage (Texture t) {
		return setImage(t == null ? null : new TextureRegion(t));
	}

	public ImageView setScaleType (int type) {
		scaleType = type;
		return this;
	}

	public ImageView setCornerRadius (float px) {
		radius = px;
		return this;
	}

	public ImageView setTint (Color c) {
		tint = c;
		return this;
	}

	/** Colour shown behind / instead of the image. */
	public ImageView setPlaceholder (Color c) {
		placeholder = c;
		return this;
	}

	public ImageView setPrefSize (float w, float h) {
		prefW = w;
		prefH = h;
		invalidateHierarchy();
		return this;
	}

	@Override
	public float getPrefWidth () {
		return prefW >= 0 ? prefW : region != null ? region.getRegionWidth() : 0;
	}

	@Override
	public float getPrefHeight () {
		return prefH >= 0 ? prefH : region != null ? region.getRegionHeight() : 0;
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		float a = parentAlpha * getColor().a;
		float x = getX(), y = getY(), w = getWidth(), h = getHeight();
		if (placeholder != null) gfx().roundRect(batch, x, y, w, h, radius, placeholder, a);
		if (region == null || w <= 0 || h <= 0) return;
		float rw = region.getRegionWidth(), rh = region.getRegionHeight();
		float u = region.getU(), v = region.getV(), u2 = region.getU2(), v2 = region.getV2();
		float dx = x, dy = y, dw = w, dh = h;
		if (scaleType == CENTER_CROP) {
			float viewAspect = w / h, imgAspect = rw / rh;
			if (imgAspect > viewAspect) {
				float keep = viewAspect / imgAspect, cut = (u2 - u) * (1 - keep) / 2;
				u += cut;
				u2 -= cut;
			} else {
				float keep = imgAspect / viewAspect, cut = (v2 - v) * (1 - keep) / 2;
				v += cut;
				v2 -= cut;
			}
		} else if (scaleType == FIT_CENTER || scaleType == CENTER) {
			float s = scaleType == CENTER ? 1f : Math.min(w / rw, h / rh);
			dw = rw * s;
			dh = rh * s;
			dx = x + (w - dw) / 2;
			dy = y + (h - dh) / 2;
		}
		gfx().image(batch, region.getTexture(), u, v, u2, v2, dx, dy, dw, dh, radius, tint, a);
	}
}
