package com.star4droid.compat.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.IntMap;
import com.star4droid.compat.Compat;

/** A virtualised list / grid: only the visible rows exist as actors and they are recycled while scrolling, so a list of
 * 100 000 items costs the same as a list of 10. Vertical list, vertical grid ({@link #setSpanCount}) or horizontal list. */
public class RecyclerView extends ScrollContainer {
	public static class ViewHolder {
		public final Actor itemView;
		int position = -1, viewType;

		public ViewHolder (Actor itemView) {
			this.itemView = itemView;
		}

		public int getPosition () {
			return position;
		}
	}

	public abstract static class Adapter<VH extends ViewHolder> {
		RecyclerView view;

		public abstract int getItemCount ();

		public int getItemViewType (int position) {
			return 0;
		}

		public abstract VH onCreateViewHolder (int viewType);

		public abstract void onBindViewHolder (VH holder, int position);

		/** Optional: exact size of an item along the scroll axis (pixels) for the given cross size. Return -1 (default) to
		 * use the measured size of the item's view type. */
		public float getItemSize (int position, float crossSize) {
			return -1;
		}

		public void notifyDataSetChanged () {
			if (view != null) view.dataChanged(true);
		}

		public void notifyItemChanged (int position) {
			if (view != null) view.itemChanged(position);
		}

		/** Items were appended / removed; visible items that still exist keep their views. */
		public void notifyItemRangeChanged () {
			if (view != null) view.dataChanged(false);
		}
	}

	public interface OnItemClickListener {
		void onItemClick (int position);
	}

	/** Adapter for the common case: a list of items, one kind of row. */
	public static class ListAdapter<T, V extends Actor> extends Adapter<ViewHolder> {
		public interface Factory<V> {
			V create ();
		}

		public interface Binder<T, V> {
			void bind (V view, T item, int position);
		}

		public final Array<T> items;
		private final Factory<V> factory;
		private final Binder<T, V> binder;

		public ListAdapter (Array<T> items, Factory<V> factory, Binder<T, V> binder) {
			this.items = items;
			this.factory = factory;
			this.binder = binder;
		}

		@Override
		public int getItemCount () {
			return items.size;
		}

		@Override
		public ViewHolder onCreateViewHolder (int viewType) {
			return new ViewHolder(factory.create());
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onBindViewHolder (ViewHolder holder, int position) {
			binder.bind((V)holder.itemView, items.get(position), position);
		}
	}

	private Adapter<ViewHolder> adapter;
	private int span = 1;
	private float gap;
	private float[] rowStart = new float[32], rowLen = new float[32];
	private int rowCount;
	private float total, itemCross, lastCross = -1;
	private final IntMap<ViewHolder> active = new IntMap<ViewHolder>();
	private final IntMap<Array<ViewHolder>> pool = new IntMap<Array<ViewHolder>>();
	private final IntFloatMap typeSize = new IntFloatMap();
	private boolean dataDirty = true, rebindAll, inLayout;
	private int pendingPosition = -1;
	private OnItemClickListener itemClick;
	private boolean dividers;
	private Color dividerColor;
	private float dividerInset;
	private Runnable onEndReached;
	private boolean endNotified;
	private int firstRow, lastRow = -1;

	public RecyclerView () {
		super(true);
	}

	/** @param vertical false for a horizontal list (carousel) */
	public RecyclerView (boolean vertical) {
		super(vertical);
	}

	@SuppressWarnings("unchecked")
	public void setAdapter (Adapter<? extends ViewHolder> a) {
		if (adapter != null) adapter.view = null;
		// drop every view of the old adapter
		for (IntMap.Entry<ViewHolder> e : active)
			e.value.itemView.remove();
		for (IntMap.Entry<Array<ViewHolder>> e : pool)
			for (ViewHolder h : e.value)
				h.itemView.remove();
		active.clear();
		pool.clear();
		typeSize.clear(0);
		adapter = (Adapter<ViewHolder>)a;
		if (a != null) a.view = this;
		dataChanged(true);
	}

	public Adapter<? extends ViewHolder> getAdapter () {
		return adapter;
	}

	/** Number of columns (vertical lists only); 1 = plain list. */
	public void setSpanCount (int columns) {
		span = Math.max(1, columns);
		typeSize.clear(0);
		dataChanged(true);
	}

	/** Space between items, in pixels. */
	public void setSpacing (float px) {
		gap = px;
		dataChanged(false);
	}

	/** Call before {@link #setAdapter}. Rows that are compat views get a ripple. */
	public void setOnItemClick (OnItemClickListener l) {
		itemClick = l;
	}

	/** Thin lines between rows. */
	public void setDividers (boolean value, float insetPx) {
		dividers = value;
		dividerInset = insetPx;
	}

	public void setDividerColor (Color c) {
		dividerColor = c;
	}

	/** Called (once per data change) when the last rows come into view; use it to load more. */
	public void setOnEndReached (Runnable r) {
		onEndReached = r;
	}

	public void scrollToPosition (int position) {
		pendingPosition = position;
		stopScroll();
		invalidate();
		Compat.requestRender();
	}

	public void smoothScrollToPosition (int position) {
		validate();
		if (rowCount == 0) return;
		int row = Math.min(rowCount - 1, Math.max(0, position / span));
		smoothScrollTo(rowStart[row] - mainPadStart());
	}

	public int getFirstVisiblePosition () {
		return firstRow * span;
	}

	public int getLastVisiblePosition () {
		return adapter == null ? -1 : Math.min(adapter.getItemCount() - 1, (lastRow + 1) * span - 1);
	}

	void dataChanged (boolean rebind) {
		dataDirty = true;
		if (rebind) rebindAll = true;
		endNotified = false;
		super.invalidate();
		Compat.requestRender();
	}

	void itemChanged (int position) {
		ViewHolder h = active.get(position);
		if (h != null) adapter.onBindViewHolder(h, position);
		dataDirty = true;
		super.invalidate();
		Compat.requestRender();
	}

	// the list's own size never depends on its rows, so row changes must not re-layout the whole screen
	@Override
	public void invalidateHierarchy () {
		invalidate();
	}

	@Override
	public void invalidate () {
		if (inLayout) return;
		super.invalidate();
	}

	@Override
	public float getPrefWidth () {
		return Compat.dp(200);
	}

	@Override
	public float getPrefHeight () {
		return Compat.dp(200);
	}

	private float mainPadStart () {
		return vertical ? padTop : (Compat.isRtl() ? padRight : padLeft);
	}

	private float mainPadEnd () {
		return vertical ? padBottom : (Compat.isRtl() ? padLeft : padRight);
	}

	private ViewHolder obtain (int type) {
		Array<ViewHolder> list = pool.get(type);
		ViewHolder h;
		if (list != null && list.size > 0) {
			h = list.pop();
		} else {
			h = adapter.onCreateViewHolder(type);
			h.viewType = type;
			final ViewHolder holder = h;
			if (itemClick != null) {
				Runnable r = new Runnable() {
					@Override
					public void run () {
						if (holder.position >= 0 && itemClick != null) itemClick.onItemClick(holder.position);
					}
				};
				if (h.itemView instanceof ViewGroup) ((ViewGroup)h.itemView).setOnClick(r);
				else if (h.itemView instanceof View) ((View)h.itemView).setOnClick(r);
			}
			addActor(h.itemView);
		}
		h.itemView.setVisible(true);
		return h;
	}

	private void recycle (ViewHolder h) {
		h.position = -1;
		h.itemView.setVisible(false);
		Array<ViewHolder> list = pool.get(h.viewType);
		if (list == null) pool.put(h.viewType, list = new Array<ViewHolder>());
		list.add(h);
	}

	private float sizeOfType (int type, int position) {
		float s = typeSize.get(type, -1);
		if (s < 0) {
			ViewHolder h = obtain(type);
			adapter.onBindViewHolder(h, position);
			s = vertical ? Measure.prefHeight(h.itemView, itemCross) : Measure.prefWidth(h.itemView);
			s = Math.round(s);
			recycle(h);
			typeSize.put(type, s);
		}
		return s;
	}

	private void rebuild () {
		dataDirty = false;
		int n = adapter == null ? 0 : adapter.getItemCount();
		if (rebindAll) {
			for (IntMap.Entry<ViewHolder> e : active)
				recycle(e.value);
			active.clear();
			rebindAll = false;
		} else {
			IntMap.Entries<ViewHolder> it = active.entries();
			while (it.hasNext()) {
				IntMap.Entry<ViewHolder> e = it.next();
				if (e.key >= n) {
					recycle(e.value);
					it.remove();
				}
			}
		}
		rowCount = (n + span - 1) / span;
		if (rowStart.length < rowCount + 1) {
			rowStart = new float[rowCount + rowCount / 4 + 16];
			rowLen = new float[rowStart.length];
		}
		float pos = mainPadStart();
		for (int r = 0; r < rowCount; r++) {
			int p = r * span;
			float s = adapter.getItemSize(p, itemCross);
			if (s < 0) s = sizeOfType(adapter.getItemViewType(p), p);
			rowStart[r] = pos;
			rowLen[r] = s;
			pos += s + gap;
		}
		total = rowCount == 0 ? 0 : pos - gap + mainPadEnd();
	}

	@Override
	protected float computeContentLength () {
		inLayout = true;
		float cross = vertical ? getWidth() - padLeft - padRight : getHeight() - padTop - padBottom;
		if (cross != lastCross) {
			lastCross = cross;
			typeSize.clear(0);
			dataDirty = true;
		}
		itemCross = Math.max(0, (cross - gap * (span - 1)) / span);
		if (dataDirty) rebuild();
		if (pendingPosition >= 0 && rowCount > 0) {
			int row = Math.min(rowCount - 1, pendingPosition / span);
			float old = scroll;
			scroll = Math.min(Math.max(0, rowStart[row] - mainPadStart()), Math.max(0, total - viewportLength()));
			pendingPosition = -1;
			notifyScrolled(old);
		}
		return total;
	}

	private int findRow (float offset) {
		int lo = 0, hi = rowCount - 1;
		while (lo < hi) {
			int mid = (lo + hi + 1) >>> 1;
			if (rowStart[mid] <= offset) lo = mid;
			else hi = mid - 1;
		}
		return lo;
	}

	@Override
	protected void layoutContent () {
		try {
			if (adapter == null || rowCount == 0) {
				firstRow = 0;
				lastRow = -1;
				return;
			}
			float view = viewportLength();
			int first = findRow(scroll), last = first;
			while (last + 1 < rowCount && rowStart[last + 1] < scroll + view)
				last++;
			firstRow = first;
			lastRow = last;
			int n = adapter.getItemCount();
			int from = first * span, to = Math.min(n, (last + 1) * span);
			IntMap.Entries<ViewHolder> it = active.entries();
			while (it.hasNext()) {
				IntMap.Entry<ViewHolder> e = it.next();
				if (e.key < from || e.key >= to) {
					recycle(e.value);
					it.remove();
				}
			}
			boolean rtl = Compat.isRtl();
			float w = getWidth(), h = getHeight();
			for (int p = from; p < to; p++) {
				ViewHolder holder = active.get(p);
				if (holder == null) {
					holder = obtain(adapter.getItemViewType(p));
					holder.position = p;
					adapter.onBindViewHolder(holder, p);
					active.put(p, holder);
				}
				int row = p / span, col = p % span;
				float start = rowStart[row] - scroll, len = rowLen[row];
				if (vertical) {
					float x = padLeft + col * (itemCross + gap);
					if (rtl) x = w - x - itemCross;
					place(holder.itemView, x, h - start - len, itemCross, len);
				} else {
					float x = rtl ? w - start - len : start;
					place(holder.itemView, x, padBottom, len, itemCross);
				}
			}
			if (onEndReached != null && !endNotified && last >= rowCount - 2) {
				endNotified = true;
				Gdx.app.postRunnable(onEndReached);
			}
		} finally {
			inLayout = false;
		}
	}

	@Override
	protected void drawOverContent (Batch batch, float alpha) {
		if (!dividers || !vertical || span != 1) return;
		Color c = dividerColor != null ? dividerColor : theme().outlineVariant;
		float th = Math.max(1, Compat.dp(1));
		for (int r = firstRow; r <= lastRow && r < rowCount - 1; r++) {
			float y = getY() + getHeight() - (rowStart[r] + rowLen[r] - scroll) - gap / 2;
			gfx().rect(batch, getX() + padLeft + dividerInset, Math.round(y), getWidth() - padLeft - padRight - dividerInset, th, c,
				alpha);
		}
	}
}
