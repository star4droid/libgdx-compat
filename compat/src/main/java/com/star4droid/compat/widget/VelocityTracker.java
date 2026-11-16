package com.star4droid.compat.widget;

import com.badlogic.gdx.utils.TimeUtils;

/** Estimates the finger speed from the last few drag samples. */
public final class VelocityTracker {
	private final float[] pos = new float[10];
	private final long[] time = new long[10];
	private int count, next;

	public void reset () {
		count = 0;
		next = 0;
	}

	public void add (float position) {
		pos[next] = position;
		time[next] = TimeUtils.nanoTime();
		next = (next + 1) % pos.length;
		if (count < pos.length) count++;
	}

	/** @return velocity in units per second over roughly the last 100 ms. */
	public float velocity () {
		if (count < 2) return 0;
		int newest = (next - 1 + pos.length) % pos.length;
		long now = time[newest];
		// a finger that rested before lifting has no velocity
		if (TimeUtils.nanoTime() - now > 80000000L) return 0;
		int oldest = newest;
		for (int i = 1; i < count; i++) {
			int idx = (newest - i + pos.length) % pos.length;
			if (now - time[idx] > 110000000L) break;
			oldest = idx;
		}
		float dt = (now - time[oldest]) / 1e9f;
		if (dt < 0.005f) return 0;
		return (pos[newest] - pos[oldest]) / dt;
	}
}
