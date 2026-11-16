package com.star4droid.compat.graphics;

import com.badlogic.gdx.math.Interpolation;

/** A tiny float animator: {@code animateTo()} then call {@code update(delta)} every frame while it returns true. */
public final class Anim {
	public float value;
	private float from, to, time, duration;
	private boolean running;
	public Interpolation interpolation = Interpolation.pow3Out;

	public Anim () {
	}

	public Anim (float value) {
		this.value = to = value;
	}

	/** Jumps to a value without animating. */
	public void set (float v) {
		value = to = v;
		running = false;
	}

	public void animateTo (float target, float seconds) {
		if (seconds <= 0) {
			set(target);
			return;
		}
		if (running && target == to) return;
		if (!running && target == value) {
			to = target;
			return;
		}
		from = value;
		to = target;
		time = 0;
		duration = seconds;
		running = true;
	}

	/** @return true while the value is still changing (ask for another frame). */
	public boolean update (float delta) {
		if (!running) return false;
		time += delta;
		if (time >= duration) {
			value = to;
			running = false;
			return true;
		}
		value = from + (to - from) * interpolation.apply(time / duration);
		return true;
	}

	public boolean isRunning () {
		return running;
	}

	public float target () {
		return to;
	}
}
