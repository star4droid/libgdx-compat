package com.star4droid.compat.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

/** A LinearLayout whose {@link RadioButton} children are mutually exclusive. */
public class RadioGroup extends LinearLayout {
	public interface OnCheckedChangeListener {
		/** @param index position among the radio buttons, or -1 */
		void onCheckedChanged (RadioGroup group, int index);
	}

	private final Array<RadioButton> buttons = new Array<RadioButton>();
	private OnCheckedChangeListener listener;
	private boolean updating;

	public RadioGroup () {
		super(VERTICAL);
	}

	public RadioGroup (int orientation) {
		super(orientation);
	}

	public void setOnCheckedChange (OnCheckedChangeListener l) {
		listener = l;
	}

	@Override
	protected void childrenChanged () {
		super.childrenChanged();
		buttons.clear();
		for (Actor a : getChildren()) {
			if (a instanceof RadioButton) {
				final RadioButton rb = (RadioButton)a;
				buttons.add(rb);
				rb.setOnCheckedChange(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged (CompoundButton button, boolean checked) {
						if (!checked || updating) return;
						updating = true;
						for (RadioButton other : buttons)
							if (other != rb) other.setChecked(false);
						updating = false;
						if (listener != null) listener.onCheckedChanged(RadioGroup.this, buttons.indexOf(rb, true));
					}
				});
			}
		}
	}

	public int getCheckedIndex () {
		for (int i = 0; i < buttons.size; i++)
			if (buttons.get(i).isChecked()) return i;
		return -1;
	}

	public void check (int index) {
		if (index >= 0 && index < buttons.size) buttons.get(index).setChecked(true);
	}
}
