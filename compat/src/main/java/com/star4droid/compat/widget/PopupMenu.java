package com.star4droid.compat.widget;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Gravity;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;

/** A drop-down menu anchored to a view (overflow menus, spinners, context menus). */
public class PopupMenu extends Overlay {
	private static final Vector2 TMP = new Vector2();

	private final LinearLayout list = new LinearLayout(LinearLayout.VERTICAL);
	private final Array<LinearLayout> rows = new Array<LinearLayout>();
	private float anchorX, anchorY, anchorW, anchorH;
	private boolean hasAnchor;

	public PopupMenu () {
		scrimAlpha = 0f;
		showSeconds = 0.14f;
		hideSeconds = 0.1f;
		Theme t = theme();
		list.setBackground(t.surfaceContainer);
		list.setCornerRadius(Compat.dp(t.cornerExtraSmall));
		list.setElevation(Compat.dp(4));
		list.setPadding(0, Compat.dp(8), 0, Compat.dp(8));
		list.setTouchable(Touchable.enabled);
		addActor(list);
	}

	public PopupMenu add (CharSequence text, Runnable onClick) {
		return add(text, (char)0, onClick);
	}

	public PopupMenu add (CharSequence text, char icon, final Runnable onClick) {
		Theme t = theme();
		LinearLayout row = LinearLayout.horizontal().gravity(Gravity.CENTER_VERTICAL);
		row.setPadding(Compat.dp(12), 0, Compat.dp(16), 0);
		if (icon != 0) row.add(new IconView(icon, t.onSurfaceVariant)).marginRight(Compat.dp(12));
		row.add(new TextView(text, Typography.BODY_LARGE).setSingleLine());
		row.setOnClick(new Runnable() {
			@Override
			public void run () {
				dismiss();
				if (onClick != null) onClick.run();
			}
		});
		rows.add(row);
		list.add(row).matchWidth().height(Compat.dp(48));
		return this;
	}

	public PopupMenu addDivider () {
		list.add(new Divider()).matchWidth().margin(0, Compat.dp(8));
		return this;
	}

	/** Shows the menu next to an actor (below it when there is room, aligned to its end edge). */
	public void show (Actor anchor) {
		Stage stage = anchor.getStage();
		if (stage == null) return;
		anchor.localToStageCoordinates(TMP.set(0, 0));
		anchorX = TMP.x;
		anchorY = TMP.y;
		anchorW = anchor.getWidth();
		anchorH = anchor.getHeight();
		hasAnchor = true;
		show(stage);
	}

	/** Shows the menu with its top-start corner at a stage position. */
	public void showAt (Stage stage, float x, float y) {
		anchorX = x;
		anchorY = y;
		anchorW = anchorH = 0;
		hasAnchor = true;
		show(stage);
	}

	@Override
	protected void onShow () {
		list.setTransform(true);
	}

	@Override
	protected void onHide () {
		list.setTransform(true);
	}

	@Override
	public void act (float delta) {
		super.act(delta);
		float v = shown.value;
		if (shown.isRunning() || dismissing) {
			float s = 0.85f + 0.15f * Interpolation.pow2Out.apply(v);
			list.setScale(s);
			list.getColor().a = Math.min(1f, v * 1.5f);
		} else if (list.isTransform()) {
			list.setScale(1f);
			list.getColor().a = 1f;
			list.setTransform(false);
		}
	}

	@Override
	public void layout () {
		float w = getWidth(), h = getHeight(), m = Compat.dp(8);
		float lw = MathUtils.clamp(list.getPrefWidth(), Compat.dp(112), Math.min(Compat.dp(280), w - 2 * m));
		float lh = Math.min(list.measureHeight(lw), h - 2 * m);
		float x, y;
		boolean alignRight;
		if (!hasAnchor) {
			x = (w - lw) / 2;
			y = (h - lh) / 2;
			alignRight = false;
		} else {
			// start edges aligned by default; switch to the other edge when the menu would leave the screen
			float left = anchorX, right = anchorX + anchorW - lw;
			x = Compat.isRtl() ? right : left;
			if (x + lw > w - m) x = right;
			if (x < m) x = left;
			alignRight = x == right && right != left;
			y = anchorY - lh;
			if (y < m) y = Math.min(h - m - lh, anchorY + anchorH);
		}
		x = MathUtils.clamp(x, m, Math.max(m, w - m - lw));
		y = MathUtils.clamp(y, m, Math.max(m, h - m - lh));
		place(list, x, y, lw, lh);
		list.setOrigin(alignRight ? lw : 0, lh);
	}
}
