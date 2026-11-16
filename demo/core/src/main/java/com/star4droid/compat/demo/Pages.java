package com.star4droid.compat.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Gravity;
import com.star4droid.compat.Icons;
import com.star4droid.compat.Typography;
import com.star4droid.compat.widget.*;
import com.star4droid.compat.xml.LayoutInflater;

/** The pages of the gallery. */
final class Pages {
	static float dp (float v) {
		return Compat.dp(v);
	}

	static TextView section (String text) {
		TextView t = new TextView(text, Typography.TITLE_SMALL);
		t.setTextColor(Compat.theme.primary);
		return t;
	}

	// ------------------------------------------------------------------ 1: widgets

	static Actor widgets (final GalleryApp app) {
		LinearLayout root = LinearLayout.vertical().padding(dp(16)).spacing(dp(12));
		root.add(new TextView("مرحباً! Hello", Typography.HEADLINE_SMALL));
		root.add(new TextView("مكتبة libgdx-compat تعطيك عناصر Material بنفس سهولة أندرويد: أزرار، قوائم، صفحات، BottomSheet والمزيد.",
			Typography.BODY_MEDIUM).setTextColor(Compat.theme.onSurfaceVariant)).matchWidth();

		root.add(section("Buttons")).marginTop(dp(8));
		FlowLayout buttons = new FlowLayout();
		buttons.add(new Button("Filled"));
		buttons.add(new Button("Tonal", Button.TONAL).icon(Icons.ADD));
		buttons.add(new Button("Outlined", Button.OUTLINED));
		buttons.add(new Button("Text", Button.TEXT));
		buttons.add(new Button("زر مرتفع", Button.ELEVATED).icon(Icons.FAVORITE));
		Button disabled = new Button("Disabled");
		disabled.setEnabled(false);
		buttons.add(disabled);
		root.add(buttons).matchWidth();

		root.add(section("FAB / Icon buttons")).marginTop(dp(8));
		LinearLayout fabs = LinearLayout.horizontal().spacing(dp(12)).gravity(Gravity.CENTER_VERTICAL);
		fabs.add(new Fab(Icons.ADD).size(Fab.SMALL));
		fabs.add(new Fab(Icons.EDIT));
		fabs.add(new IconButton(Icons.SETTINGS));
		fabs.add(new IconButton(Icons.FAVORITE).style(IconButton.FILLED));
		fabs.add(new IconButton(Icons.BOOKMARK).style(IconButton.TONAL));
		fabs.add(new IconButton(Icons.SHARE).style(IconButton.OUTLINED));
		root.add(fabs);

		root.add(section("Progress")).marginTop(dp(8));
		final ProgressBar bar = new ProgressBar();
		bar.setProgress(0.6f);
		root.add(bar).matchWidth();
		root.add(new ProgressBar()).matchWidth();
		LinearLayout spinners = LinearLayout.horizontal().spacing(dp(16)).gravity(Gravity.CENTER_VERTICAL);
		spinners.add(ProgressBar.circular());
		final ProgressBar ring = ProgressBar.circular();
		ring.setProgress(0.6f);
		spinners.add(ring);
		Slider slider = new Slider();
		slider.setValue(0.6f);
		slider.setOnChange(new Slider.OnChangeListener() {
			public void onValueChanged (Slider s, float value, boolean fromUser) {
				bar.setProgress(value);
				ring.setProgress(value);
			}
		});
		spinners.add(slider).weight(1).width(0);
		root.add(spinners).matchWidth();

		root.add(section("Cards")).marginTop(dp(8));
		CardView card = new CardView();
		LinearLayout cl = LinearLayout.vertical().spacing(dp(4)).padding(dp(16));
		cl.add(new TextView("بطاقة مرتفعة", Typography.TITLE_MEDIUM));
		cl.add(new TextView("Supporting text that is long enough to wrap onto a second line inside the card.", Typography.BODY_MEDIUM)
			.setTextColor(Compat.theme.onSurfaceVariant)).matchWidth();
		LinearLayout actions = LinearLayout.horizontal().spacing(dp(8)).gravity(Gravity.END);
		actions.add(new Button("إلغاء", Button.TEXT));
		actions.add(new Button("موافق", Button.FILLED));
		cl.add(actions).matchWidth().marginTop(dp(8));
		card.add(cl).matchWidth();
		root.add(card).matchWidth();

		LinearLayout twoCards = LinearLayout.horizontal().spacing(dp(12));
		CardView filled = new CardView(CardView.FILLED);
		filled.setPadding(dp(16));
		filled.add(new TextView("Filled", Typography.TITLE_MEDIUM));
		filled.setOnClick(new Runnable() {
			public void run () {
			}
		});
		CardView outlined = new CardView(CardView.OUTLINED);
		outlined.setPadding(dp(16));
		outlined.add(new TextView("بإطار", Typography.TITLE_MEDIUM));
		outlined.setOnClick(new Runnable() {
			public void run () {
			}
		});
		twoCards.add(filled).weight(1).width(0).height(dp(72));
		twoCards.add(outlined).weight(1).width(0).height(dp(72));
		root.add(twoCards).matchWidth();
		root.add(new Space(0, dp(72)));

		ScrollView scroll = new ScrollView(root);
		scroll.lp(root).matchWidth();
		return scroll;
	}

	// ------------------------------------------------------------------ 2: inputs

	static Actor inputs (final GalleryApp app) {
		LinearLayout root = LinearLayout.vertical().padding(dp(16)).spacing(dp(12));
		root.add(section("Text fields"));
		root.add(new EditText("الاسم").setLeadingIcon(Icons.PERSON).id("name")).matchWidth();
		root.add(new EditText("Email").setStyle(EditText.FILLED).setLeadingIcon(Icons.MAIL).setText("user@example.com")).matchWidth();
		root.add(new EditText("كلمة المرور").setPassword(true).setText("secret12").setHelperText("8 أحرف على الأقل")).matchWidth();
		root.add(new EditText("Amount").setNumeric().setText("12x").setError("رقم غير صالح")).matchWidth();

		root.add(section("Selection")).marginTop(dp(8));
		LinearLayout checks = LinearLayout.horizontal().spacing(dp(8)).gravity(Gravity.CENTER_VERTICAL);
		checks.add(new CheckBox("Check", true));
		checks.add(new CheckBox("خيار"));
		root.add(checks);
		RadioGroup group = new RadioGroup(LinearLayout.HORIZONTAL);
		group.setSpacing(dp(8));
		group.add(new RadioButton("صغير"));
		group.add(new RadioButton("Medium"));
		group.add(new RadioButton("كبير"));
		group.check(1);
		root.add(group);
		root.add(new Switch("الوضع الليلي", app.dark)).matchWidth();
		root.add(new Switch("Notifications", true)).matchWidth();

		root.add(section("Chips")).marginTop(dp(8));
		FlowLayout chips = new FlowLayout();
		String[] names = {"الكل", "Games", "تطبيقات", "Music", "أفلام", "Books", "رياضة"};
		for (int i = 0; i < names.length; i++) {
			Chip c = new Chip(names[i]).checkable(true);
			if (i == 0) c.setSelected(true);
			chips.add(c);
		}
		chips.add(new Chip("Help").icon(Icons.HELP_OUTLINE));
		root.add(chips).matchWidth();
		root.add(new Space(0, dp(72)));
		ScrollView scroll = new ScrollView(root);
		scroll.lp(root).matchWidth();
		return scroll;
	}

	// ------------------------------------------------------------------ 3: list

	static final String[] NAMES = {"أحمد محمد", "Sara Ali", "خالد عبد الله", "Mona Hassan", "يوسف إبراهيم", "Omar Khalid", "فاطمة الزهراء",
		"Layla Nour", "محمد توفيق", "Zain Malik"};
	static final String[] MESSAGES = {"السلام عليكم، كيف حالك اليوم؟", "See you at 5:30 PM", "تم إرسال الملف بنجاح ✓", "Thanks a lot!",
		"هل وصلت الرحلة رقم 245؟", "Let's ship version 2.0", "موعدنا غداً إن شاء الله"};
	static final int[] HUES = {0xD32F2F, 0x7B1FA2, 0x1976D2, 0x00796B, 0xF57C00, 0x5D4037, 0x455A64};

	/** One row of the list: built once, re-bound while scrolling. */
	static class ContactRow extends LinearLayout {
		final FrameLayout avatar = new FrameLayout();
		final TextView letter = new TextView("", Typography.TITLE_MEDIUM);
		final TextView name = new TextView("", Typography.BODY_LARGE);
		final TextView message = new TextView("", Typography.BODY_MEDIUM);
		final TextView time = new TextView("", Typography.LABEL_SMALL);
		final Color color = new Color();

		ContactRow () {
			super(HORIZONTAL);
			setGravity(Gravity.CENTER_VERTICAL);
			setPadding(dp(16), dp(8), dp(16), dp(8));
			avatar.setBackground(color);
			avatar.setCornerRadius(dp(20));
			letter.setTextColor(Color.WHITE);
			avatar.add(letter).gravity(Gravity.CENTER);
			add(avatar).size(dp(40), dp(40));
			LinearLayout texts = LinearLayout.vertical();
			name.setSingleLine();
			message.setSingleLine().setTextColor(Compat.theme.onSurfaceVariant);
			texts.add(name).matchWidth();
			texts.add(message).matchWidth();
			add(texts).weight(1).width(0).margins(dp(16), 0, dp(8), 0);
			time.setTextColor(Compat.theme.onSurfaceVariant);
			add(time);
		}

		void bind (int position) {
			String n = NAMES[position % NAMES.length];
			name.setText(n + "  #" + (position + 1));
			message.setText(MESSAGES[position % MESSAGES.length]);
			letter.setText(n.substring(0, 1));
			int h = HUES[position % HUES.length];
			color.set(((h >> 16) & 255) / 255f, ((h >> 8) & 255) / 255f, (h & 255) / 255f, 1);
			time.setText((position % 12 + 1) + ":" + (position * 7 % 60 < 10 ? "0" : "") + (position * 7 % 60));
		}
	}

	static Actor list (final GalleryApp app, final GalleryApp.HomeScreen home) {
		final RecyclerView rv = new RecyclerView();
		rv.setName("list");
		rv.setDividers(true, dp(72));
		rv.setPadding(0, dp(4), 0, dp(88));
		rv.setOnItemClick(new RecyclerView.OnItemClickListener() {
			public void onItemClick (int position) {
				Snackbar.make(app.stage, "Item " + (position + 1) + " — " + NAMES[position % NAMES.length], Snackbar.LENGTH_SHORT)
					.setBottomOffset(dp(80)).show();
			}
		});
		rv.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
			public int getItemCount () {
				return 100000;
			}

			public RecyclerView.ViewHolder onCreateViewHolder (int viewType) {
				return new RecyclerView.ViewHolder(new ContactRow());
			}

			public void onBindViewHolder (RecyclerView.ViewHolder holder, int position) {
				((ContactRow)holder.itemView).bind(position);
			}
		});
		rv.setOnRefresh(new Runnable() {
			public void run () {
				Timer.schedule(new Timer.Task() {
					public void run () {
						rv.setRefreshing(false);
						Compat.requestRender();
					}
				}, 1.5f);
			}
		});
		// the extended FAB collapses while the list scrolls down, like in Gmail
		rv.setOnScroll(new ScrollContainer.OnScrollListener() {
			public void onScrolled (ScrollContainer view, float scroll, float delta) {
				if (delta > 2 && home.fab.isExtended()) home.fab.shrink();
				else if (delta < -2 && !home.fab.isExtended()) home.fab.extend();
				home.toolbar.setLifted(scroll > 0);
			}
		});
		return rv;
	}

	// ------------------------------------------------------------------ 4: grid

	/** A generated picture (so the demo needs no image files), packed into the shared atlas: drawing it costs no extra
	 * draw call. Real photos would normally be separate textures. */
	static TextureRegion makePhoto () {
		int s = 96;
		Pixmap pm = new Pixmap(s, s, Pixmap.Format.RGBA8888);
		for (int y = 0; y < s; y++)
			for (int x = 0; x < s; x++) {
				float fx = x / (float)s, fy = y / (float)s;
				float wave = 0.5f + 0.5f * (float)Math.sin(fx * 9 + fy * 5);
				pm.drawPixel(x, y, Color.rgba8888(0.25f + 0.6f * fx, 0.3f + 0.4f * wave, 0.85f - 0.5f * fy, 1));
			}
		TextureRegion region = Compat.gfx().pack(pm);
		pm.dispose();
		return region;
	}

	static class Tile extends CardView {
		final ImageView image;
		final TextView title = new TextView("", Typography.TITLE_SMALL);
		final TextView sub = new TextView("", Typography.BODY_SMALL);
		final Color tint = new Color();

		Tile (TextureRegion photo) {
			super(CardView.FILLED);
			image = new ImageView(photo);
			LinearLayout col = LinearLayout.vertical();
			image.setScaleType(ImageView.CENTER_CROP).setCornerRadius(dp(12)).setTint(tint);
			col.add(image).matchWidth().height(dp(84));
			title.setSingleLine();
			col.add(title).matchWidth().margins(dp(12), dp(8), dp(12), 0);
			sub.setSingleLine().setTextColor(Compat.theme.onSurfaceVariant);
			col.add(sub).matchWidth().margins(dp(12), 0, dp(12), dp(10));
			add(col).matchWidth();
		}

		void bind (int position) {
			title.setText(position % 2 == 0 ? "عنصر " + (position + 1) : "Item " + (position + 1));
			sub.setText((position * 37 % 90 + 10) + " MB");
			int h = HUES[position % HUES.length];
			tint.set(0.6f + 0.4f * ((h >> 16) & 255) / 255f, 0.6f + 0.4f * ((h >> 8) & 255) / 255f, 0.6f + 0.4f * (h & 255) / 255f, 1);
		}
	}

	static Actor grid (final GalleryApp app) {
		final TextureRegion photo = makePhoto();
		RecyclerView rv = new RecyclerView();
		rv.setName("grid");
		rv.setSpanCount(3);
		rv.setSpacing(dp(8));
		rv.setPadding(dp(12), dp(12), dp(12), dp(88));
		Array<Integer> items = new Array<Integer>();
		for (int i = 0; i < 3000; i++)
			items.add(i);
		rv.setOnItemClick(new RecyclerView.OnItemClickListener() {
			public void onItemClick (int position) {
			}
		});
		rv.setAdapter(new RecyclerView.ListAdapter<Integer, Tile>(items, new RecyclerView.ListAdapter.Factory<Tile>() {
			public Tile create () {
				return new Tile(photo);
			}
		}, new RecyclerView.ListAdapter.Binder<Integer, Tile>() {
			public void bind (Tile view, Integer item, int position) {
				view.bind(item);
			}
		}));
		return rv;
	}

	// ------------------------------------------------------------------ 5: XML

	/** Receives the onClick="..." calls of layouts/demo.xml. */
	public static class XmlHandler {
		Actor root;
		GalleryApp app;
		int clicks;

		public void onSendClicked () {
			EditText name = Views.find(root, "name");
			CheckBox agree = Views.find(root, "agree");
			if (name.getText().trim().length() == 0) {
				name.setError("الاسم مطلوب");
				return;
			}
			name.setError(null);
			Snackbar.make(app.stage, "مرحباً " + name.getText() + (agree.isChecked() ? " ✓" : ""), Snackbar.LENGTH_LONG)
				.setBottomOffset(dp(80)).show();
		}

		public void onBannerClicked (Actor view) {
			TextView counter = Views.find(root, "counter");
			counter.setText("Clicked " + (++clicks) + " times");
		}
	}

	static Actor xml (GalleryApp app) {
		XmlHandler handler = new XmlHandler();
		handler.app = app;
		Actor root = new LayoutInflater().putString("title", "نموذج من XML").inflate(Gdx.files.internal("layouts/demo.xml"), handler);
		handler.root = root;
		return root;
	}

	// ------------------------------------------------------------------ bottom sheet

	static Actor sheetContent (final GalleryApp app, final Runnable close) {
		LinearLayout col = LinearLayout.vertical();
		TextView title = new TextView("مشاركة عبر", Typography.TITLE_LARGE);
		col.add(title).matchWidth().margins(dp(24), 0, dp(24), dp(8));
		RecyclerView rv = new RecyclerView();
		rv.setName("sheetList");
		final String[] apps = {"رسالة", "Bluetooth", "البريد", "Drive", "نسخ الرابط", "WhatsApp", "طباعة", "Telegram", "QR code", "حفظ كملف",
			"Notes", "تقويم", "Maps", "ترجمة", "Cloud", "سجل", "Bookmarks", "تنزيل", "Wi-Fi", "حاسوب"};
		final char[] icons = {Icons.CHAT, Icons.BLUETOOTH, Icons.MAIL, Icons.CLOUD, Icons.CONTENT_COPY, Icons.SEND, Icons.PRINT, Icons.SEND,
			Icons.QR_CODE_SCANNER, Icons.SAVE, Icons.EDIT, Icons.EVENT, Icons.MAP, Icons.TRANSLATE, Icons.CLOUD_UPLOAD, Icons.HISTORY,
			Icons.BOOKMARK, Icons.DOWNLOAD, Icons.WIFI, Icons.COMPUTER};
		rv.setOnItemClick(new RecyclerView.OnItemClickListener() {
			public void onItemClick (int position) {
				close.run();
			}
		});
		rv.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
			public int getItemCount () {
				return apps.length;
			}

			public RecyclerView.ViewHolder onCreateViewHolder (int viewType) {
				LinearLayout row = LinearLayout.horizontal().gravity(Gravity.CENTER_VERTICAL);
				row.setPadding(dp(24), 0, dp(24), 0);
				row.add(new IconView(Icons.SHARE).id("icon"));
				row.add(new TextView("", Typography.BODY_LARGE).id("text")).weight(1).width(0).marginLeft(dp(16));
				return new RecyclerView.ViewHolder(row);
			}

			public void onBindViewHolder (RecyclerView.ViewHolder holder, int position) {
				((IconView)Views.find(holder.itemView, "icon")).setIcon(icons[position]);
				((TextView)Views.find(holder.itemView, "text")).setText(apps[position]);
			}

			public float getItemSize (int position, float crossSize) {
				return dp(56);
			}
		});
		col.add(rv).matchWidth().weight(1).height(0);
		return col;
	}
}
