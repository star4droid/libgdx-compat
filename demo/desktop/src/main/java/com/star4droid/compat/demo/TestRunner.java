package com.star4droid.compat.demo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.Array;

import java.nio.ByteBuffer;

/** Drives the gallery with scripted touches and saves screenshots; used to test the library without a device. */
public class TestRunner implements ApplicationListener {
	interface Step {
		/** @return true when finished */
		boolean run (float delta);
	}

	final GalleryApp app;
	final String test, out;
	final Array<Step> steps = new Array<Step>();
	int index;
	GLProfiler profiler;
	int lastDrawCalls, lastBinds;
	boolean idleProbe;
	int idleFrames;
	long idleStart;
	float avgFrameMs;

	TestRunner (GalleryApp app, String test, String out) {
		this.app = app;
		this.test = test;
		this.out = out;
	}

	InputProcessor in () {
		return Gdx.input.getInputProcessor();
	}

	void waitSec (final float seconds) {
		steps.add(new Step() {
			float t;

			public boolean run (float delta) {
				t += delta;
				return t >= seconds;
			}
		});
	}

	void run (final Runnable r) {
		steps.add(new Step() {
			public boolean run (float delta) {
				r.run();
				return true;
			}
		});
	}

	void tap (final float x, final float y) {
		run(new Runnable() {
			public void run () {
				in().touchDown((int)x, (int)y, 0, Input.Buttons.LEFT);
			}
		});
		waitSec(0.06f);
		run(new Runnable() {
			public void run () {
				in().touchUp((int)x, (int)y, 0, Input.Buttons.LEFT);
			}
		});
	}

	void press (final float x, final float y) {
		run(new Runnable() {
			public void run () {
				in().touchDown((int)x, (int)y, 0, Input.Buttons.LEFT);
			}
		});
	}

	void release (final float x, final float y) {
		run(new Runnable() {
			public void run () {
				in().touchUp((int)x, (int)y, 0, Input.Buttons.LEFT);
			}
		});
	}

	/** Drag from a to b in the given time, then release (a fast drag becomes a fling). */
	void drag (final float x1, final float y1, final float x2, final float y2, final float seconds) {
		press(x1, y1);
		steps.add(new Step() {
			float t;

			public boolean run (float delta) {
				t += delta;
				float f = Math.min(1, t / seconds);
				in().touchDragged((int)(x1 + (x2 - x1) * f), (int)(y1 + (y2 - y1) * f), 0);
				return f >= 1;
			}
		});
		release(x2, y2);
	}

	interface Find {
		com.badlogic.gdx.scenes.scene2d.Actor get ();
	}

	final float[] tapPos = new float[2];

	/** Taps the centre of an actor that is looked up when the step runs. */
	void tapOn (final Find f) {
		tapOn(f, 0.5f, 0.5f);
	}

	void tapOn (final Find f, final float fx, final float fy) {
		run(new Runnable() {
			public void run () {
				com.badlogic.gdx.scenes.scene2d.Actor a = f.get();
				com.badlogic.gdx.math.Vector2 v = a.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(a.getWidth() * fx, a.getHeight() * fy));
				tapPos[0] = v.x;
				tapPos[1] = Gdx.graphics.getHeight() - v.y;
				in().touchDown((int)tapPos[0], (int)tapPos[1], 0, Input.Buttons.LEFT);
			}
		});
		waitSec(0.06f);
		run(new Runnable() {
			public void run () {
				in().touchUp((int)tapPos[0], (int)tapPos[1], 0, Input.Buttons.LEFT);
			}
		});
	}

	// ---- performance sampling
	boolean sampling;
	int sFrames, sMaxDraw, sIdleFrames;
	double sMs, sAlloc;
	long allocBefore, allocOverhead;

	long allocated () {
		try {
			return ((com.sun.management.ThreadMXBean)java.lang.management.ManagementFactory.getThreadMXBean())
				.getThreadAllocatedBytes(Thread.currentThread().getId());
		} catch (Throwable e) {
			return 0;
		}
	}

	void perfStart () {
		run(new Runnable() {
			public void run () {
				long b0 = allocated(), b1 = allocated();
				allocOverhead = b1 - b0; // the measurement itself allocates
				sampling = true;
				sFrames = sMaxDraw = 0;
				sMs = sAlloc = 0;
			}
		});
	}

	void perfEnd (final String label) {
		run(new Runnable() {
			public void run () {
				sampling = false;
				System.out.println(String.format("PERF %-28s frames=%d avgFrameMs=%.2f maxDrawCalls=%d allocPerFrame=%.0f bytes", label,
					sFrames, sMs / Math.max(1, sFrames), sMaxDraw, sAlloc / Math.max(1, sFrames)));
			}
		});
	}

	void check (final String what, final Check c) {
		run(new Runnable() {
			public void run () {
				boolean ok;
				String detail = "";
				try {
					ok = c.ok();
				} catch (Throwable e) {
					ok = false;
					detail = " (" + e + ")";
				}
				System.out.println("ASSERT " + (ok ? "ok   " : "FAIL ") + what + detail);
			}
		});
	}

	interface Check {
		boolean ok ();
	}

	void type (final String text) {
		run(new Runnable() {
			public void run () {
				for (int i = 0; i < text.length(); i++)
					in().keyTyped(text.charAt(i));
			}
		});
	}

	void key (final int keycode) {
		run(new Runnable() {
			public void run () {
				in().keyDown(keycode);
				in().keyUp(keycode);
			}
		});
	}

	void shot (final String name) {
		run(new Runnable() {
			public void run () {
				int w = Gdx.graphics.getBackBufferWidth(), h = Gdx.graphics.getBackBufferHeight();
				Pixmap pm = Pixmap.createFromFrameBuffer(0, 0, w, h);
				// flip vertically and make opaque
				ByteBuffer px = pm.getPixels();
				byte[] row = new byte[w * 4], row2 = new byte[w * 4];
				for (int y = 0; y < h / 2; y++) {
					px.position(y * w * 4);
					px.get(row);
					px.position((h - 1 - y) * w * 4);
					px.get(row2);
					px.position(y * w * 4);
					px.put(row2);
					px.position((h - 1 - y) * w * 4);
					px.put(row);
				}
				for (int i = 3; i < w * h * 4; i += 4)
					px.put(i, (byte)255);
				px.position(0);
				PixmapIO.writePNG(Gdx.files.absolute(new java.io.File(out, name + ".png").getAbsolutePath()), pm);
				pm.dispose();
				System.out.println("SHOT " + name + "  drawCalls=" + lastDrawCalls + " textureBinds=" + lastBinds + " frameMs="
					+ String.format("%.2f", avgFrameMs) + " javaHeapMB=" + (Gdx.app.getJavaHeap() / 1024 / 1024));
			}
		});
	}

	@Override
	public void create () {
		new java.io.File(out).mkdirs();
		app.create();
		Gdx.graphics.setContinuousRendering(true);
		profiler = new GLProfiler(Gdx.graphics);
		profiler.enable();
		Scripts.build(this, test);
	}

	@Override
	public void resize (int width, int height) {
		app.resize(width, height);
	}

	@Override
	public void render () {
		profiler.reset();
		long a0 = sampling ? allocated() : 0;
		long t0 = System.nanoTime();
		app.render();
		float cpuMs = (System.nanoTime() - t0) / 1e6f;
		Gdx.gl.glFinish();
		float ms = (System.nanoTime() - t0) / 1e6f;
		if (sampling) {
			sFrames++;
			sMs += cpuMs;
			sAlloc += Math.max(0, allocated() - a0 - allocOverhead);
			sMaxDraw = Math.max(sMaxDraw, profiler.getDrawCalls());
		}
		avgFrameMs = avgFrameMs == 0 ? ms : avgFrameMs * 0.9f + ms * 0.1f;
		lastDrawCalls = profiler.getDrawCalls();
		lastBinds = profiler.getTextureBindings();
		float delta = Gdx.graphics.getDeltaTime();
		while (index < steps.size) {
			if (!steps.get(index).run(delta)) break;
			index++;
			delta = 0;
			// one step per frame unless it was instantaneous and the next one is a wait
			break;
		}
		if (idleProbe) {
			// frames arriving here were requested by the UI itself
			if (idleStart == 0) {
				idleStart = System.currentTimeMillis();
				new Thread() {
					public void run () {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException ignored) {
						}
						System.out.println("PERF idle 3s with continuous rendering off: frames rendered=" + idleFrames);
						Gdx.app.exit();
						Gdx.graphics.requestRendering();
					}
				}.start();
			} else idleFrames++;
			return;
		}
		if (index >= steps.size) Gdx.app.exit();
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
		app.dispose();
	}
}
