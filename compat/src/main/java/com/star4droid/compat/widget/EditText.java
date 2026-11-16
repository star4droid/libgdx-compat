package com.star4droid.compat.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Icons;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.graphics.Anim;
import com.star4droid.compat.graphics.Fonts;
import com.star4droid.compat.graphics.Gfx;
import com.star4droid.compat.graphics.TextLayout;
import com.star4droid.compat.graphics.TextShaper;

/** Material text field (outlined or filled) with a floating label, icons, helper / error text, password mode, selection,
 * clipboard and Arabic input. Uses the system keyboard on mobile. */
public class EditText extends View {
	public static final int OUTLINED = 0, FILLED = 1;
	/** Labels of the long-press menu; replace them to localise. */
	public static String[] MENU_LABELS = {"Cut", "Copy", "Paste", "Select all"};

	public interface OnTextChangedListener {
		void onTextChanged (EditText view, String text);
	}

	public interface CharFilter {
		boolean accept (char c);
	}

	private final StringBuilder text = new StringBuilder();
	private final TextLayout layout = new TextLayout(Typography.sizePx(Typography.BODY_LARGE), false);
	private TextLayout labelBig, labelSmall, helperLayout;
	private CharSequence hint, helper, error;
	private int style = OUTLINED;
	private char leadingIcon, trailingIcon;
	private Runnable onTrailingClick;
	private boolean password, showPassword, focused;
	private int cursor, anchor, maxLength = Integer.MAX_VALUE;
	private float[] prefix = new float[16];
	private boolean prefixDirty = true, rtl;
	private float scrollX, blink;
	private final Anim labelAnim = new Anim(0f);
	private CharFilter filter;
	private OnTextChangedListener listener;
	private Runnable onSubmit;
	private Input.OnscreenKeyboardType keyboardType = Input.OnscreenKeyboardType.Default;
	private Color containerColor;
	private InputListener outsideListener;
	private long lastTap;

	public EditText () {
		this(null);
	}

	/** @param hint the floating label */
	public EditText (CharSequence hint) {
		setHint(hint);
		addListener(new InputListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (!isEnabled() || pointer != 0) return false;
				requestFocus();
				if (trailing() != 0 && overTrailing(x)) return true;
				int i = indexAt(x);
				long now = System.currentTimeMillis();
				if (now - lastTap < 300 && i == cursor) selectAll();
				else cursor = anchor = i;
				lastTap = now;
				blink = 0;
				return true;
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				if (trailing() != 0 && overTrailing(x)) return;
				cursor = indexAt(x);
				ensureCursorVisible();
				Compat.requestRender();
			}

			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				if (event.isTouchFocusCancel()) return;
				if (trailing() != 0 && overTrailing(x)) {
					if (password) {
						showPassword = !showPassword;
						textChangedInternal(false);
					} else if (onTrailingClick != null) onTrailingClick.run();
				}
			}

			@Override
			public boolean keyTyped (InputEvent event, char c) {
				if (!focused) return false;
				if (c == '\b') {
					if (!deleteSelection() && cursor > 0) {
						text.deleteCharAt(cursor - 1);
						cursor = anchor = cursor - 1;
						textChangedInternal(true);
					}
				} else if (c == 127) {
					if (!deleteSelection() && cursor < text.length()) {
						text.deleteCharAt(cursor);
						textChangedInternal(true);
					}
				} else if (c == '\r' || c == '\n') {
					if (onSubmit != null) onSubmit.run();
					clearFocus();
				} else if (c >= 32 && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
					&& !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.SYM)) {
					insert(String.valueOf(c));
				}
				return true;
			}

			@Override
			public boolean keyDown (InputEvent event, int keycode) {
				if (!focused) return false;
				boolean ctrl = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)
					|| Gdx.input.isKeyPressed(Input.Keys.SYM);
				boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
				if (ctrl) {
					if (keycode == Input.Keys.A) selectAll();
					else if (keycode == Input.Keys.C) copy();
					else if (keycode == Input.Keys.X) cut();
					else if (keycode == Input.Keys.V) paste();
					else return false;
					return true;
				}
				int step = 0;
				if (keycode == Input.Keys.LEFT) step = rtl ? 1 : -1;
				else if (keycode == Input.Keys.RIGHT) step = rtl ? -1 : 1;
				else if (keycode == Input.Keys.HOME) step = -text.length();
				else if (keycode == Input.Keys.END) step = text.length();
				else return false;
				cursor = Math.max(0, Math.min(text.length(), cursor + step));
				if (!shift) anchor = cursor;
				blink = 0;
				ensureCursorVisible();
				Compat.requestRender();
				return true;
			}
		});
		addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged (FocusEvent event, Actor actor, boolean gained) {
				if (actor == EditText.this) setFocused(gained);
			}
		});
		setOnLongClick(new Runnable() {
			@Override
			public void run () {
				showMenu();
			}
		});
	}

	// ------------------------------------------------------------ options

	public EditText setHint (CharSequence h) {
		hint = h;
		if (h == null) labelBig = labelSmall = null;
		else {
			if (labelBig == null) {
				labelBig = new TextLayout(Typography.sizePx(Typography.BODY_LARGE), false);
				labelSmall = new TextLayout(Typography.sizePx(Typography.BODY_SMALL), false);
				labelBig.setMaxLines(1);
				labelSmall.setMaxLines(1);
			}
			labelBig.setText(h);
			labelSmall.setText(h);
		}
		return this;
	}

	public EditText setStyle (int s) {
		style = s;
		return this;
	}

	public EditText setText (CharSequence t) {
		text.setLength(0);
		if (t != null) text.append(t);
		cursor = anchor = text.length();
		textChangedInternal(true);
		labelAnim.set(shouldFloat() ? 1 : 0);
		return this;
	}

	public String getText () {
		return text.toString();
	}

	public EditText setLeadingIcon (char icon) {
		leadingIcon = icon;
		return this;
	}

	public EditText setTrailingIcon (char icon, Runnable onClick) {
		trailingIcon = icon;
		onTrailingClick = onClick;
		return this;
	}

	/** Hides the characters and adds a show / hide toggle. */
	public EditText setPassword (boolean value) {
		password = value;
		if (value) keyboardType = Input.OnscreenKeyboardType.Password;
		textChangedInternal(false);
		return this;
	}

	/** Small text under the field. */
	public EditText setHelperText (CharSequence t) {
		helper = t;
		updateHelper();
		return this;
	}

	/** Puts the field in the error state (null clears it). */
	public EditText setError (CharSequence t) {
		error = t;
		updateHelper();
		Compat.requestRender();
		return this;
	}

	public EditText setMaxLength (int n) {
		maxLength = n;
		return this;
	}

	public EditText setFilter (CharFilter f) {
		filter = f;
		return this;
	}

	/** Digits only, with the numeric keyboard. */
	public EditText setNumeric () {
		keyboardType = Input.OnscreenKeyboardType.NumberPad;
		filter = new CharFilter() {
			@Override
			public boolean accept (char c) {
				return (c >= '0' && c <= '9') || c == '.' || c == '-' || (c >= 0x660 && c <= 0x669);
			}
		};
		return this;
	}

	public EditText setKeyboardType (Input.OnscreenKeyboardType type) {
		keyboardType = type;
		return this;
	}

	public EditText setOnTextChanged (OnTextChangedListener l) {
		listener = l;
		return this;
	}

	/** Called when Enter / Done is pressed. */
	public EditText setOnSubmit (Runnable r) {
		onSubmit = r;
		return this;
	}

	/** Colour behind an outlined field's label (set it when the field is not on the theme's surface colour). */
	public EditText setContainerColor (Color c) {
		containerColor = c;
		return this;
	}

	// ------------------------------------------------------------ focus

	public void requestFocus () {
		Stage stage = getStage();
		if (stage == null) return;
		if (stage.getKeyboardFocus() != this) stage.setKeyboardFocus(this);
		Gdx.input.setOnscreenKeyboardVisible(true, keyboardType);
	}

	public void clearFocus () {
		Stage stage = getStage();
		if (stage != null && stage.getKeyboardFocus() == this) stage.setKeyboardFocus(null);
		Gdx.input.setOnscreenKeyboardVisible(false);
	}

	public boolean hasFocus () {
		return focused;
	}

	private void setFocused (boolean value) {
		if (focused == value) return;
		focused = value;
		blink = 0;
		labelAnim.animateTo(shouldFloat() ? 1 : 0, 0.15f);
		final Stage stage = getStage();
		if (stage != null) {
			if (value) {
				if (outsideListener == null) outsideListener = new InputListener() {
					@Override
					public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
						Actor target = event.getTarget();
						if (target != EditText.this && !(target instanceof EditText)) {
							Stage s = getStage();
							if (s != null && s.getKeyboardFocus() == EditText.this) s.setKeyboardFocus(null);
							Gdx.input.setOnscreenKeyboardVisible(false);
						}
						return false;
					}
				};
				stage.getRoot().addCaptureListener(outsideListener);
			} else if (outsideListener != null) {
				stage.getRoot().removeCaptureListener(outsideListener);
			}
		}
		if (!value) anchor = cursor;
		Compat.requestRender();
	}

	private boolean shouldFloat () {
		return focused || text.length() > 0;
	}

	// ------------------------------------------------------------ editing

	private boolean hasSelection () {
		return cursor != anchor;
	}

	public void selectAll () {
		anchor = 0;
		cursor = text.length();
		Compat.requestRender();
	}

	private boolean deleteSelection () {
		if (!hasSelection()) return false;
		int a = Math.min(cursor, anchor), b = Math.max(cursor, anchor);
		text.delete(a, b);
		cursor = anchor = a;
		textChangedInternal(true);
		return true;
	}

	private void insert (String s) {
		StringBuilder ok = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < 32 || (filter != null && !filter.accept(c))) continue;
			ok.append(c);
		}
		if (ok.length() == 0) return;
		if (hasSelection()) {
			int a = Math.min(cursor, anchor), b = Math.max(cursor, anchor);
			text.delete(a, b);
			cursor = anchor = a;
		}
		int room = maxLength - text.length();
		if (room <= 0) return;
		if (ok.length() > room) ok.setLength(room);
		text.insert(cursor, ok);
		cursor = anchor = cursor + ok.length();
		textChangedInternal(true);
	}

	public void copy () {
		if (!hasSelection() || password) return;
		Gdx.app.getClipboard().setContents(text.substring(Math.min(cursor, anchor), Math.max(cursor, anchor)));
	}

	public void cut () {
		copy();
		if (!password) deleteSelection();
	}

	public void paste () {
		String c = Gdx.app.getClipboard().getContents();
		if (c != null) insert(c.replace('\n', ' ').replace("\r", ""));
	}

	private void showMenu () {
		if (getStage() == null) return;
		requestFocus();
		PopupMenu menu = new PopupMenu();
		if (hasSelection() && !password) {
			menu.add(MENU_LABELS[0], new Runnable() {
				@Override
				public void run () {
					cut();
				}
			});
			menu.add(MENU_LABELS[1], new Runnable() {
				@Override
				public void run () {
					copy();
				}
			});
		}
		menu.add(MENU_LABELS[2], new Runnable() {
			@Override
			public void run () {
				paste();
			}
		});
		if (text.length() > 0) menu.add(MENU_LABELS[3], new Runnable() {
			@Override
			public void run () {
				selectAll();
			}
		});
		menu.show(this);
	}

	private String display () {
		if (password && !showPassword) {
			StringBuilder sb = new StringBuilder(text.length());
			for (int i = 0; i < text.length(); i++)
				sb.append('•');
			return sb.toString();
		}
		return text.toString();
	}

	private void textChangedInternal (boolean notify) {
		layout.setText(display());
		prefixDirty = true;
		blink = 0;
		if (cursor > text.length()) cursor = text.length();
		if (anchor > text.length()) anchor = text.length();
		labelAnim.animateTo(shouldFloat() ? 1 : 0, getStage() != null ? 0.15f : 0);
		ensureCursorVisible();
		Compat.requestRender();
		if (notify && listener != null) listener.onTextChanged(this, text.toString());
	}

	private void updateHelper () {
		CharSequence t = error != null ? error : helper;
		boolean had = helperLayout != null;
		if (t == null) helperLayout = null;
		else {
			if (helperLayout == null) helperLayout = new TextLayout(Typography.sizePx(Typography.BODY_SMALL), false);
			helperLayout.setText(t);
			helperLayout.setMaxLines(1);
		}
		if (had != (helperLayout != null)) invalidateHierarchy();
	}

	// ------------------------------------------------------------ geometry

	private char trailing () {
		if (password) return showPassword ? Icons.VISIBILITY_OFF : Icons.VISIBILITY;
		return trailingIcon;
	}

	private boolean overTrailing (float x) {
		return Compat.isRtl() ? x < Compat.dp(48) : x > getWidth() - Compat.dp(48);
	}

	private float helperHeight () {
		return helperLayout != null ? Compat.dp(20) : 0;
	}

	/** Left edge of the text area in local coordinates. */
	private float textLeft () {
		boolean flip = Compat.isRtl();
		char startIcon = flip ? trailing() : leadingIcon;
		return startIcon != 0 ? Compat.dp(52) : Compat.dp(16);
	}

	private float textWidth () {
		boolean flip = Compat.isRtl();
		char endIcon = flip ? leadingIcon : trailing();
		return Math.max(1, getWidth() - textLeft() - (endIcon != 0 ? Compat.dp(52) : Compat.dp(16)));
	}

	private void computePrefix () {
		if (!prefixDirty) return;
		prefixDirty = false;
		String d = display();
		int n = d.length();
		if (prefix.length < n + 1) prefix = new float[n + 16];
		BitmapFont f = layout.font();
		boolean any = TextShaper.hasRtl(d);
		rtl = any && TextShaper.isRtlParagraph(d);
		prefix[0] = 0;
		if (!any) {
			for (int i = 1; i <= n; i++)
				prefix[i] = prefix[i - 1] + Fonts.width(f, d, i - 1, i);
		} else {
			for (int i = 1; i <= n; i++) {
				String s = TextShaper.shape(d.substring(0, i));
				prefix[i] = Fonts.width(f, s, 0, s.length());
			}
		}
	}

	/** x of a character boundary relative to the text area's left edge. */
	private float caretX (int index) {
		computePrefix();
		float p = prefix[Math.min(index, text.length())];
		return rtl ? textWidth() - p + scrollX : p - scrollX;
	}

	private int indexAt (float localX) {
		computePrefix();
		float x = localX - textLeft();
		float d = rtl ? textWidth() + scrollX - x : x + scrollX;
		int best = 0, n = text.length();
		float bestDist = Float.MAX_VALUE;
		for (int i = 0; i <= n; i++) {
			float dist = Math.abs(prefix[i] - d);
			if (dist < bestDist) {
				bestDist = dist;
				best = i;
			}
		}
		return best;
	}

	private void ensureCursorVisible () {
		if (getWidth() <= 0) return;
		computePrefix();
		float w = textWidth(), total = prefix[text.length()], p = prefix[Math.min(cursor, text.length())];
		float max = Math.max(0, total - w + Compat.dp(2));
		if (p - scrollX > w - Compat.dp(2)) scrollX = p - w + Compat.dp(2);
		if (p - scrollX < 0) scrollX = p;
		if (scrollX > max) scrollX = max;
		if (scrollX < 0) scrollX = 0;
	}

	@Override
	protected void sizeChanged () {
		super.sizeChanged();
		ensureCursorVisible();
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		if (labelAnim.update(delta)) Compat.requestRender();
		if (focused) {
			blink += delta;
			Compat.requestRender();
		}
	}

	@Override
	public float getPrefWidth () {
		return Compat.dp(220);
	}

	@Override
	public float getPrefHeight () {
		return Compat.dp(56) + helperHeight();
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		validate();
		Theme t = theme();
		Gfx g = gfx();
		float a = parentAlpha * getColor().a * (isEnabled() ? 1f : 0.38f);
		float hh = helperHeight();
		float x = getX(), y = getY() + hh, w = getWidth(), h = getHeight() - hh;
		boolean err = error != null;
		Color accent = err ? t.error : t.primary;
		float r = Compat.dp(t.cornerExtraSmall), lf = labelAnim.value;
		boolean uiRtl = Compat.isRtl();

		if (style == FILLED) {
			g.roundRect(batch, x, y, w, h, r, r, 0, 0, t.surfaceContainerHighest, a);
			float lw = focused || err ? Compat.dp(2) : Compat.dp(1);
			g.rect(batch, x, y, w, lw, focused || err ? accent : t.onSurfaceVariant, a);
		} else {
			g.stroke(batch, x, y, w, h, r, focused || err ? Compat.dp(2) : Compat.dp(1), focused || err ? accent : t.outline, a);
		}

		float tl = textLeft(), tw = textWidth();
		float is = Compat.dp(24);
		if (leadingIcon != 0)
			Compat.fonts().drawIcon(batch, leadingIcon, uiRtl ? x + w - Compat.dp(12) - is : x + Compat.dp(12), y + (h - is) / 2,
				is, 1f, t.onSurfaceVariant, a);
		char tr = trailing();
		if (tr != 0) Compat.fonts().drawIcon(batch, tr, uiRtl ? x + Compat.dp(12) : x + w - Compat.dp(12) - is,
			y + (h - is) / 2, is, 1f, err ? t.error : t.onSurfaceVariant, a);

		// floating label
		if (labelBig != null) {
			TextLayout l = lf < 0.5f ? labelBig : labelSmall;
			l.wrap(tw);
			boolean labelRtl = l.isRtl() || uiRtl;
			float restTop = y + (h + labelBig.lineHeight()) / 2;
			float floatTop = style == FILLED ? y + h - Compat.dp(7) : y + h + labelSmall.lineHeight() / 2;
			float top = restTop + (floatTop - restTop) * lf;
			float lx = x + tl;
			if (style == OUTLINED && lf > 0) {
				float nw = (labelSmall.prefWidth() + Compat.dp(8)) * lf;
				float nx = labelRtl ? x + tl + tw - labelSmall.prefWidth() - Compat.dp(4) + (labelSmall.prefWidth() + Compat.dp(8) - nw)
					: x + tl - Compat.dp(4);
				g.rect(batch, nx, y + h - Compat.dp(3), nw, Compat.dp(4), containerColor != null ? containerColor : t.surface, a);
			}
			l.setAlign(labelRtl ? TextLayout.ALIGN_RIGHT : TextLayout.ALIGN_LEFT);
			l.draw(batch, lx, top, tw, lf > 0.5f && (focused || err) ? accent : (err ? t.error : t.onSurfaceVariant), a);
		}

		// text, selection and cursor
		float textTop = (style == FILLED && labelBig != null ? y + h / 2 - Compat.dp(8) + layout.lineHeight() / 2 + Compat.dp(1)
			: y + (h + layout.lineHeight()) / 2);
		if (style == FILLED && labelBig != null) textTop = y + Compat.dp(8) + layout.lineHeight();
		computePrefix();
		float total = prefix[text.length()];
		boolean clipText = total > tw;
		if (clipText) {
			batch.flush();
			if (!clipBegin(x + tl, y, tw, h)) clipText = false;
		}
		float lineH = layout.lineHeight(), caretBottom = textTop - lineH + Compat.dp(1);
		if (hasSelection() && focused) {
			float x1 = caretX(Math.min(cursor, anchor)), x2 = caretX(Math.max(cursor, anchor));
			g.rect(batch, x + tl + Math.min(x1, x2), caretBottom, Math.abs(x2 - x1), lineH, t.primary, a * 0.3f);
		}
		if (text.length() > 0) {
			layout.wrap(100000);
			float pw = layout.prefWidth();
			layout.setAlign(TextLayout.ALIGN_LEFT);
			float dx = rtl ? x + tl + tw + scrollX - pw + 1 : x + tl - scrollX;
			layout.draw(batch, dx, textTop, pw + 2, t.onSurface, a);
		}
		if (focused && (blink % 1f) < 0.5f) {
			float cx = caretX(cursor);
			g.rect(batch, Math.round(x + tl + cx) - (rtl ? Compat.dp(2) : 0), caretBottom, Math.max(1, Compat.dp(2)), lineH - Compat.dp(2),
				accent, a);
		}
		if (clipText) {
			batch.flush();
			clipEnd();
		}

		if (helperLayout != null) {
			helperLayout.wrap(Math.max(1, w - Compat.dp(32)));
			helperLayout.setAlign(uiRtl || helperLayout.isRtl() ? TextLayout.ALIGN_RIGHT : TextLayout.ALIGN_LEFT);
			helperLayout.draw(batch, x + Compat.dp(16), getY() + hh - Compat.dp(3), Math.max(1, w - Compat.dp(32)),
				err ? t.error : t.onSurfaceVariant, a);
		}
	}
}
