package com.star4droid.compat.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Gravity;
import com.star4droid.compat.Icons;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.widget.*;

/** The gallery: every component of libgdx-compat on one stage. */
public class GalleryApp extends ApplicationAdapter {
	public Stage stage;
	public float density = 0;
	public boolean rtl, dark;
	public Navigator navigator;
	public HomeScreen home;
	int seedIndex;
	static final Color[] SEEDS = {null, new Color(0x00897BFF), new Color(0xE65100FF), new Color(0x1565C0FF)};

	@Override
	public void create () {
		Compat.Config c = new Compat.Config();
		c.density = density;
		c.rtl = rtl;
		c.dark = dark;
		Compat.init(c);
		// draw only when something changes: the library asks for frames while it animates
		Gdx.graphics.setContinuousRendering(false);
		stage = Compat.createStage();
		Gdx.input.setInputProcessor(stage);
		navigator = new Navigator();
		navigator.setFillParent(true);
		stage.addActor(navigator);
		navigator.push(home = new HomeScreen(this));
	}

	void applyTheme () {
		Color seed = SEEDS[seedIndex];
		Compat.theme.set(seed == null ? (dark ? Theme.dark() : Theme.light()) : Theme.fromSeed(seed, dark));
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void render () {
		ScreenUtils.clear(Compat.theme.background);
		stage.act();
		stage.draw();
	}

	@Override
	public void dispose () {
		stage.dispose();
		Compat.dispose();
	}

	// ------------------------------------------------------------------ screens

	public static class HomeScreen extends Navigator.Screen {
		final GalleryApp app;
		public DrawerLayout drawer;
		public ViewPager pager;
		public TabLayout tabs;
		public Toolbar toolbar;
		public Fab fab;
		public BottomNavigationView bottomNav;

		HomeScreen (GalleryApp app) {
			this.app = app;
		}

		@Override
		protected Actor onCreateView () {
			final Stage stage = app.stage;
			drawer = new DrawerLayout();

			LinearLayout column = LinearLayout.vertical();
			column.setBackground(Compat.theme.surface);

			toolbar = new Toolbar("libgdx-compat");
			toolbar.setNavigationIcon(Icons.MENU, new Runnable() {
				public void run () {
					drawer.open();
				}
			});
			final IconButton[] themeButton = new IconButton[1];
			themeButton[0] = toolbar.addAction(Icons.DARK_MODE, new Runnable() {
				public void run () {
					app.dark = !app.dark;
					app.applyTheme();
					themeButton[0].icon(app.dark ? Icons.LIGHT_MODE : Icons.DARK_MODE);
				}
			});
			final IconButton[] more = new IconButton[1];
			more[0] = toolbar.addAction(Icons.MORE_VERT, new Runnable() {
				public void run () {
					new PopupMenu().add("حوار / Dialog", Icons.CHAT, new Runnable() {
						public void run () {
							showDialog();
						}
					}).add("Snackbar", Icons.INFO_OUTLINE, new Runnable() {
						public void run () {
							Snackbar.make(stage, "تم حفظ التغييرات", Snackbar.LENGTH_LONG).setAction("تراجع", null)
								.setBottomOffset(Compat.dp(80)).show();
						}
					}).add("لون جديد / Seed colour", Icons.PALETTE, new Runnable() {
						public void run () {
							app.seedIndex = (app.seedIndex + 1) % SEEDS.length;
							app.applyTheme();
						}
					}).addDivider().add("شاشة جديدة / Push screen", Icons.OPEN_IN_NEW, new Runnable() {
						public void run () {
							app.navigator.push(new DetailScreen("شاشة التفاصيل"));
						}
					}).show(more[0]);
				}
			});
			column.add(toolbar).matchWidth();

			tabs = new TabLayout(TabLayout.MODE_SCROLLABLE);
			tabs.addTab("العناصر");
			tabs.addTab("Inputs");
			tabs.addTab("قائمة");
			tabs.addTab("Grid");
			tabs.addTab("XML");
			column.add(tabs).matchWidth();

			pager = new ViewPager();
			pager.addPage(Pages.widgets(app));
			pager.addPage(new ViewPager.PageFactory() {
				public Actor create (int index) {
					return Pages.inputs(app);
				}
			});
			pager.addPage(new ViewPager.PageFactory() {
				public Actor create (int index) {
					return Pages.list(app, HomeScreen.this);
				}
			});
			pager.addPage(new ViewPager.PageFactory() {
				public Actor create (int index) {
					return Pages.grid(app);
				}
			});
			pager.addPage(new ViewPager.PageFactory() {
				public Actor create (int index) {
					return Pages.xml(app);
				}
			});
			tabs.setupWithViewPager(pager);
			pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				public void onPageScrolled (float position) {
				}

				public void onPageSelected (int index) {
					toolbar.setLifted(false);
					fab.extend();
					bottomNav.setSelectedItem(index >= 4 ? 2 : index >= 2 ? 1 : 0);
				}
			});
			column.add(pager).matchWidth().weight(1).height(0);

			bottomNav = new BottomNavigationView();
			bottomNav.addItem(Icons.HOME, "الرئيسية").addItem(Icons.LIST, "Lists").addItem(Icons.CODE, "XML");
			bottomNav.setBadge(1, 12);
			bottomNav.setOnItemSelected(new BottomNavigationView.OnItemSelectedListener() {
				public void onItemSelected (int index) {
					pager.setCurrentItem(index == 0 ? 0 : index == 1 ? 2 : 4);
				}
			});
			column.add(bottomNav).matchWidth();

			// the FAB floats above the content
			FrameLayout frame = new FrameLayout();
			frame.add(column).match();
			fab = new Fab(Icons.ADD, "جديد").onClick(new Runnable() {
				public void run () {
					showSheet();
				}
			});
			frame.add(fab).gravity(Gravity.BOTTOM | Gravity.END).margins(Compat.dp(16), 0, Compat.dp(16), Compat.dp(96));
			drawer.setContent(frame);

			NavigationView nav = new NavigationView();
			LinearLayout header = LinearLayout.vertical().padding(Compat.dp(16), Compat.dp(20));
			header.add(new TextView("libgdx-compat", Typography.TITLE_LARGE));
			header.add(new TextView("واجهات Material فوق libGDX", Typography.BODY_MEDIUM).setTextColor(Compat.theme.onSurfaceVariant));
			nav.addHeader(header);
			nav.addItem(Icons.HOME, "الرئيسية", close()).addItem(Icons.FAVORITE, "المفضلة", close())
				.addItem(Icons.DOWNLOAD, "Downloads", close()).addDivider().addSectionTitle("أخرى")
				.addItem(Icons.SETTINGS, "الإعدادات", close()).addItem(Icons.INFO, "About", close());
			nav.setCheckedItem(0);
			drawer.setDrawer(nav);
			return drawer;
		}

		Runnable close () {
			return new Runnable() {
				public void run () {
					drawer.close();
				}
			};
		}

		void showDialog () {
			new AlertDialog().setIcon(Icons.DELETE).setTitle("حذف العنصر؟")
				.setMessage("سيتم حذف هذا العنصر نهائياً ولا يمكن التراجع عن هذه العملية.").setNegativeButton("إلغاء", null)
				.setPositiveButton("حذف", new Runnable() {
					public void run () {
						Snackbar.make(app.stage, "تم الحذف", Snackbar.LENGTH_SHORT).setBottomOffset(Compat.dp(80)).show();
					}
				}).show(app.stage);
		}

		public BottomSheet sheet;

		void showSheet () {
			sheet = new BottomSheet(Pages.sheetContent(app, new Runnable() {
				public void run () {
					sheet.dismiss();
				}
			}));
			sheet.setFullHeight(true);
			sheet.show(app.stage);
		}
	}

	public static class DetailScreen extends Navigator.Screen {
		final String title;

		DetailScreen (String title) {
			this.title = title;
		}

		@Override
		protected Actor onCreateView () {
			LinearLayout root = LinearLayout.vertical();
			root.setBackground(Compat.theme.surface);
			Toolbar tb = new Toolbar(title);
			tb.setNavigationIcon(Compat.isRtl() ? Icons.ARROW_FORWARD : Icons.ARROW_BACK, new Runnable() {
				public void run () {
					navigator.pop();
				}
			});
			root.add(tb).matchWidth();
			LinearLayout body = LinearLayout.vertical().padding(Compat.dp(24)).spacing(Compat.dp(16)).gravity(Gravity.CENTER);
			body.add(new IconView(Icons.LAYERS, Compat.theme.primary).setIconSize(Compat.dp(72)));
			body.add(new TextView("Navigator", Typography.HEADLINE_SMALL));
			body.add(new TextView("هذه شاشة ثانية فوق مكدّس التنقل. زر الرجوع (أو ESC) يعيدك للشاشة السابقة مع حركة انتقال.",
				Typography.BODY_LARGE).setGravity(Gravity.CENTER_HORIZONTAL).setTextColor(Compat.theme.onSurfaceVariant)).matchWidth();
			body.add(new Button("شاشة أخرى", Button.TONAL).onClick(new Runnable() {
				public void run () {
					navigator.push(new DetailScreen(title + " +"));
				}
			}));
			root.add(body).matchWidth().weight(1).height(0);
			return root;
		}
	}
}
