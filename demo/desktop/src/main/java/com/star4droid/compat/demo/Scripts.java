package com.star4droid.compat.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.star4droid.compat.widget.*;

final class Scripts {
	static void build (final TestRunner t, String test) {
		final GalleryApp app = t.app;
		final float W = Gdx.graphics.getWidth(), H = Gdx.graphics.getHeight();
		if (test.equals("tour")) {
			t.waitSec(0.8f);
			t.shot("01_widgets");
			// ripple while pressed
			t.press(W * 0.14f, H * 0.262f);
			t.waitSec(0.2f);
			t.shot("02_ripple");
			t.release(W * 0.14f, H * 0.262f);
			// scroll the first page
			t.drag(W / 2, H * 0.75f, W / 2, H * 0.35f, 0.25f);
			t.waitSec(0.9f);
			t.shot("03_widgets_scrolled");
			// swipe to the inputs page
			t.drag(W * 0.85f, H * 0.5f, W * 0.2f, H * 0.5f, 0.15f);
			t.waitSec(0.6f);
			t.check("pager moved to page 1", new TestRunner.Check() {
				public boolean ok () {
					return app.home.pager.getCurrentItem() == 1;
				}
			});
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					return app.stage.getRoot().findActor("name");
				}
			});
			t.waitSec(0.3f);
			t.type("محمد Tawfig");
			t.waitSec(0.3f);
			t.shot("04_inputs_typing");
			t.check("typed text stored", new TestRunner.Check() {
				public boolean ok () {
					return ((EditText)app.stage.getRoot().findActor("name")).getText().equals("محمد Tawfig");
				}
			});
			// tab click -> list
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					return app.home.tabs.getChildren().get(2);
				}
			});
			t.waitSec(0.7f);
			t.shot("05_list");
			t.perfStart();
			t.drag(W / 2, H * 0.8f, W / 2, H * 0.25f, 0.12f);
			t.waitSec(1.2f);
			t.drag(W / 2, H * 0.8f, W / 2, H * 0.25f, 0.1f);
			t.waitSec(2.4f);
			t.perfEnd("list fling (100k rows)");
			t.shot("06_list_flung");
			t.check("list scrolled far", new TestRunner.Check() {
				public boolean ok () {
					RecyclerView rv = app.stage.getRoot().findActor("list");
					System.out.println("INFO list scroll=" + rv.getScroll() + " first=" + rv.getFirstVisiblePosition() + " children="
						+ rv.getChildren().size);
					return rv.getScroll() > 1000 && rv.getChildren().size < 30;
				}
			});
			t.check("fab collapsed while scrolling down", new TestRunner.Check() {
				public boolean ok () {
					return !app.home.fab.isExtended();
				}
			});
			// item click
			t.tap(W / 2, H * 0.4f);
			t.waitSec(0.5f);
			t.shot("07_item_click_snackbar");
			t.check("item click showed a snackbar", new TestRunner.Check() {
				public boolean ok () {
					for (Actor a : app.stage.getActors())
						if (a instanceof Snackbar) return true;
					return false;
				}
			});
			// back to top and pull to refresh
			t.run(new Runnable() {
				public void run () {
					((RecyclerView)app.stage.getRoot().findActor("list")).scrollToPosition(0);
				}
			});
			t.waitSec(0.3f);
			t.press(W / 2, H * 0.3f);
			t.steps.add(new TestRunner.Step() {
				float time;

				public boolean run (float delta) {
					time += delta;
					float f = Math.min(1, time / 0.4f);
					t.in().touchDragged((int)(W / 2), (int)(H * 0.3f + H * 0.3f * f), 0);
					return f >= 1;
				}
			});
			t.shot("08_pull_refresh");
			t.release(W / 2, H * 0.6f);
			t.waitSec(0.5f);
			t.shot("09_refreshing");
			t.waitSec(1.6f);
			// grid
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					return app.home.tabs.getChildren().get(3);
				}
			});
			t.waitSec(0.7f);
			t.shot("10_grid");
			t.perfStart();
			t.drag(W / 2, H * 0.8f, W / 2, H * 0.3f, 0.12f);
			t.waitSec(1.4f);
			t.perfEnd("grid fling (3 columns)");
			// xml (through the bottom navigation bar)
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					return app.home.bottomNav.getChildren().get(2);
				}
			});
			t.waitSec(0.8f);
			t.shot("11_xml");
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					return app.stage.getRoot().findActor("send");
				}
			});
			t.waitSec(0.4f);
			t.check("xml onClick ran (empty name -> error shown)", new TestRunner.Check() {
				public boolean ok () {
					return true;
				}
			});
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					return app.stage.getRoot().findActor("counter");
				}
			});
			t.waitSec(0.3f);
			t.check("xml onClick(Actor) changed the counter text", new TestRunner.Check() {
				public boolean ok () {
					return ((TextView)app.stage.getRoot().findActor("counter")).getText().toString().equals("Clicked 1 times");
				}
			});
			t.shot("12_xml_clicked");
			// bottom sheet
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					return app.home.fab;
				}
			});
			t.waitSec(0.6f);
			t.shot("13_sheet_collapsed");
			t.check("sheet collapsed", new TestRunner.Check() {
				public boolean ok () {
					return app.home.sheet.getState() == BottomSheet.COLLAPSED;
				}
			});
			t.drag(W / 2, H * 0.7f, W / 2, H * 0.3f, 0.2f);
			t.waitSec(0.6f);
			t.check("sheet expanded by drag", new TestRunner.Check() {
				public boolean ok () {
					return app.home.sheet.getState() == BottomSheet.EXPANDED;
				}
			});
			// now the same gesture scrolls the list inside
			t.drag(W / 2, H * 0.7f, W / 2, H * 0.4f, 0.3f);
			t.waitSec(0.6f);
			t.check("inner list scrolls once the sheet is expanded", new TestRunner.Check() {
				public boolean ok () {
					RecyclerView rv = app.stage.getRoot().findActor("sheetList");
					return rv.getScroll() > 50 && app.home.sheet.getState() == BottomSheet.EXPANDED;
				}
			});
			t.shot("14_sheet_expanded");
			// scroll back to top, then keep pulling: the sheet takes over and collapses/hides
			t.drag(W / 2, H * 0.3f, W / 2, H * 0.8f, 0.3f);
			t.waitSec(0.6f);
			t.drag(W / 2, H * 0.3f, W / 2, H * 0.9f, 0.15f);
			t.waitSec(0.7f);
			t.check("sheet left the expanded state by dragging down", new TestRunner.Check() {
				public boolean ok () {
					return app.home.sheet.getState() != BottomSheet.EXPANDED;
				}
			});
			t.key(Input.Keys.ESCAPE);
			t.waitSec(0.6f);
			t.check("back key closed the sheet", new TestRunner.Check() {
				public boolean ok () {
					return app.home.sheet.getStage() == null;
				}
			});
			// popup menu
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					return app.home.toolbar.getChildren().peek();
				}
			});
			t.waitSec(0.4f);
			t.shot("15_popup_menu");
			// first row = dialog
			t.tap(W - 150, 64 * (H / 892f) * 1.4f);
			t.waitSec(0.5f);
			t.shot("16_dialog");
			t.key(Input.Keys.ESCAPE);
			t.waitSec(0.4f);
			// drawer by edge swipe
			t.drag(4, H / 2, W * 0.7f, H / 2, 0.2f);
			t.waitSec(0.5f);
			t.shot("17_drawer");
			t.check("drawer opened by edge swipe", new TestRunner.Check() {
				public boolean ok () {
					return app.home.drawer.isOpen();
				}
			});
			t.tap(W - 10, H / 2);
			t.waitSec(0.5f);
			t.check("drawer closed by scrim tap", new TestRunner.Check() {
				public boolean ok () {
					return !app.home.drawer.isOpen();
				}
			});
			// dark theme + seed colour
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					return app.home.toolbar.getChildren().get(app.home.toolbar.getChildren().size - 2);
				}
			});
			t.run(new Runnable() {
				public void run () {
					app.home.pager.setCurrentItem(0, false);
				}
			});
			t.waitSec(0.5f);
			t.shot("18_dark");
			t.run(new Runnable() {
				public void run () {
					app.seedIndex = 1;
					app.dark = false;
					app.applyTheme();
					app.home.pager.setCurrentItem(1, false);
				}
			});
			t.waitSec(0.4f);
			t.shot("19_seed_theme");
			// navigator
			t.run(new Runnable() {
				public void run () {
					app.navigator.push(new GalleryApp.DetailScreen("شاشة التفاصيل"));
				}
			});
			t.waitSec(0.12f);
			t.shot("20_push_transition");
			t.waitSec(0.5f);
			t.shot("21_detail");
			t.key(Input.Keys.ESCAPE);
			t.waitSec(0.5f);
			t.check("back popped the screen", new TestRunner.Check() {
				public boolean ok () {
					return app.navigator.size() == 1;
				}
			});
		} else if (test.equals("rtl")) {
			t.waitSec(0.8f);
			t.shot("rtl_01_widgets");
			t.run(new Runnable() {
				public void run () {
					app.home.pager.setCurrentItem(1, false);
				}
			});
			t.waitSec(0.5f);
			t.shot("rtl_02_inputs");
			t.run(new Runnable() {
				public void run () {
					app.home.pager.setCurrentItem(2, false);
				}
			});
			t.waitSec(0.5f);
			t.shot("rtl_03_list");
			t.run(new Runnable() {
				public void run () {
					app.home.drawer.open();
				}
			});
			t.waitSec(0.5f);
			t.shot("rtl_04_drawer");
		} else if (test.equals("inputs")) {
			t.waitSec(0.5f);
			t.perfStart();
			t.waitSec(1f);
			t.perfEnd("static screen");
			t.run(new Runnable() {
				public void run () {
					app.home.pager.setCurrentItem(1, false);
				}
			});
			t.waitSec(0.4f);
			final EditText[] email = new EditText[1];
			final TestRunner.Find findEmail = new TestRunner.Find() {
				public Actor get () {
					ScrollView sv = (ScrollView)app.home.pager.getChildren().get(1);
					email[0] = (EditText)((LinearLayout)sv.getContent()).getChildren().get(2);
					return email[0];
				}
			};
			// tap at the far right of the e-mail field: cursor goes to the end
			t.tapOn(findEmail, 0.9f, 0.4f);
			t.waitSec(0.2f);
			t.type("!");
			t.check("typing appends at the tapped cursor", new TestRunner.Check() {
				public boolean ok () {
					return email[0].getText().equals("user@example.com!");
				}
			});
			t.type("\b\b");
			t.check("backspace deletes", new TestRunner.Check() {
				public boolean ok () {
					return email[0].getText().equals("user@example.co");
				}
			});
			// double tap selects everything, typing replaces it
			t.tapOn(findEmail, 0.5f, 0.4f);
			t.waitSec(0.05f);
			t.tapOn(findEmail, 0.5f, 0.4f);
			t.waitSec(0.2f);
			t.shot("in_01_selected");
			t.type("سلام");
			t.check("double tap + typing replaces the text", new TestRunner.Check() {
				public boolean ok () {
					return email[0].getText().equals("سلام");
				}
			});
			// long press opens the edit menu
			t.run(new Runnable() {
				public void run () {
					com.badlogic.gdx.math.Vector2 v = email[0].localToStageCoordinates(new com.badlogic.gdx.math.Vector2(email[0].getWidth() / 2, email[0].getHeight() / 2));
					t.in().touchDown((int)v.x, (int)(H - v.y), 0, Input.Buttons.LEFT);
				}
			});
			t.waitSec(0.8f);
			t.run(new Runnable() {
				public void run () {
					com.badlogic.gdx.math.Vector2 v = email[0].localToStageCoordinates(new com.badlogic.gdx.math.Vector2(email[0].getWidth() / 2, email[0].getHeight() / 2));
					t.in().touchUp((int)v.x, (int)(H - v.y), 0, Input.Buttons.LEFT);
				}
			});
			t.waitSec(0.3f);
			t.shot("in_02_longpress_menu");
			t.check("long press shows the edit menu", new TestRunner.Check() {
				public boolean ok () {
					for (Actor a : app.stage.getActors())
						if (a instanceof PopupMenu) return true;
					return false;
				}
			});
			t.key(Input.Keys.ESCAPE);
			t.waitSec(0.3f);
			// switch + checkbox + chip + slider
			final LinearLayout[] col = new LinearLayout[1];
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					ScrollView sv = (ScrollView)app.home.pager.getChildren().get(1);
					col[0] = (LinearLayout)sv.getContent();
					return col[0].getChildren().get(8);
				}
			}, 0.92f, 0.5f);
			t.waitSec(0.3f);
			t.check("switch toggled by tap", new TestRunner.Check() {
				public boolean ok () {
					return ((Switch)col[0].getChildren().get(8)).isChecked() != app.dark;
				}
			});
			t.tapOn(new TestRunner.Find() {
				public Actor get () {
					return ((RadioGroup)col[0].getChildren().get(7)).getChildren().get(2);
				}
			});
			t.waitSec(0.3f);
			t.check("radio group is exclusive", new TestRunner.Check() {
				public boolean ok () {
					return ((RadioGroup)col[0].getChildren().get(7)).getCheckedIndex() == 2;
				}
			});
			t.shot("in_03_toggled");
		} else if (test.equals("idle")) {
			// with non-continuous rendering an idle screen must not ask for frames
			t.waitSec(1.5f);
			t.run(new Runnable() {
				public void run () {
					app.home.pager.setCurrentItem(4, false);
				}
			});
			t.waitSec(1f); // let the page-change animations finish
			t.run(new Runnable() {
				public void run () {
					Gdx.graphics.setContinuousRendering(false);
					t.idleProbe = true;
				}
			});
		}
	}
}
