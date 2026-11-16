package com.star4droid.compat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectIntMap;

import java.io.BufferedReader;

/** Material Icons code points. Constants cover the common icons; {@link #get(String)} resolves any of the ~2200 icon names
 * (e.g. "shopping_cart") from the bundled table. */
public final class Icons {
	public static final char ADD = '\ue145';
	public static final char REMOVE = '\ue15b';
	public static final char CLOSE = '\ue5cd';
	public static final char CHECK = '\ue5ca';
	public static final char MENU = '\ue5d2';
	public static final char MORE_VERT = '\ue5d4';
	public static final char MORE_HORIZ = '\ue5d3';
	public static final char ARROW_BACK = '\ue5c4';
	public static final char ARROW_FORWARD = '\ue5c8';
	public static final char ARROW_UPWARD = '\ue5d8';
	public static final char ARROW_DOWNWARD = '\ue5db';
	public static final char ARROW_DROP_DOWN = '\ue5c5';
	public static final char ARROW_DROP_UP = '\ue5c7';
	public static final char CHEVRON_LEFT = '\ue5cb';
	public static final char CHEVRON_RIGHT = '\ue5cc';
	public static final char EXPAND_MORE = '\ue5cf';
	public static final char EXPAND_LESS = '\ue5ce';
	public static final char SEARCH = '\ue8b6';
	public static final char SETTINGS = '\ue8b8';
	public static final char HOME = '\ue88a';
	public static final char FAVORITE = '\ue87d';
	public static final char FAVORITE_BORDER = '\ue87e';
	public static final char STAR = '\ue838';
	public static final char STAR_BORDER = '\ue83a';
	public static final char PERSON = '\ue7fd';
	public static final char PEOPLE = '\ue7fb';
	public static final char ACCOUNT_CIRCLE = '\ue853';
	public static final char NOTIFICATIONS = '\ue7f4';
	public static final char NOTIFICATIONS_NONE = '\ue7f5';
	public static final char MAIL = '\ue158';
	public static final char CALL = '\ue0b0';
	public static final char CHAT = '\ue0b7';
	public static final char SEND = '\ue163';
	public static final char SHARE = '\ue80d';
	public static final char EDIT = '\ue3c9';
	public static final char DELETE = '\ue872';
	public static final char DELETE_OUTLINE = '\ue92e';
	public static final char SAVE = '\ue161';
	public static final char DONE = '\ue876';
	public static final char DONE_ALL = '\ue877';
	public static final char INFO = '\ue88e';
	public static final char INFO_OUTLINE = '\ue88f';
	public static final char WARNING = '\ue002';
	public static final char ERROR = '\ue000';
	public static final char ERROR_OUTLINE = '\ue001';
	public static final char HELP = '\ue887';
	public static final char HELP_OUTLINE = '\ue8fd';
	public static final char VISIBILITY = '\ue8f4';
	public static final char VISIBILITY_OFF = '\ue8f5';
	public static final char LOCK = '\ue897';
	public static final char LOCK_OPEN = '\ue898';
	public static final char REFRESH = '\ue5d5';
	public static final char SYNC = '\ue627';
	public static final char DOWNLOAD = '\uf090';
	public static final char UPLOAD = '\uf09b';
	public static final char FILE_DOWNLOAD = '\ue2c4';
	public static final char FILE_UPLOAD = '\ue2c6';
	public static final char FOLDER = '\ue2c7';
	public static final char FOLDER_OPEN = '\ue2c8';
	public static final char INSERT_DRIVE_FILE = '\ue24d';
	public static final char IMAGE = '\ue3f4';
	public static final char PHOTO_CAMERA = '\ue412';
	public static final char CAMERA_ALT = '\ue3b0';
	public static final char VIDEOCAM = '\ue04b';
	public static final char MIC = '\ue029';
	public static final char VOLUME_UP = '\ue050';
	public static final char VOLUME_OFF = '\ue04f';
	public static final char PLAY_ARROW = '\ue037';
	public static final char PAUSE = '\ue034';
	public static final char STOP = '\ue047';
	public static final char SKIP_NEXT = '\ue044';
	public static final char SKIP_PREVIOUS = '\ue045';
	public static final char FAST_FORWARD = '\ue01f';
	public static final char FAST_REWIND = '\ue020';
	public static final char SHOPPING_CART = '\ue8cc';
	public static final char SHOPPING_BAG = '\uf1cc';
	public static final char PAYMENT = '\ue8a1';
	public static final char LOCATION_ON = '\ue0c8';
	public static final char PLACE = '\ue55f';
	public static final char MAP = '\ue55b';
	public static final char DIRECTIONS_BUS = '\ue530';
	public static final char DIRECTIONS_CAR = '\ue531';
	public static final char LOCAL_SHIPPING = '\ue558';
	public static final char SCHEDULE = '\ue8b5';
	public static final char EVENT = '\ue878';
	public static final char CALENDAR_TODAY = '\ue935';
	public static final char ALARM = '\ue855';
	public static final char ACCESS_TIME = '\ue192';
	public static final char LANGUAGE = '\ue894';
	public static final char TRANSLATE = '\ue8e2';
	public static final char LIGHT_MODE = '\ue518';
	public static final char DARK_MODE = '\ue51c';
	public static final char BRIGHTNESS_6 = '\ue3ab';
	public static final char PALETTE = '\ue40a';
	public static final char FILTER_LIST = '\ue152';
	public static final char SORT = '\ue164';
	public static final char TUNE = '\ue429';
	public static final char ATTACH_FILE = '\ue226';
	public static final char LINK = '\ue157';
	public static final char CONTENT_COPY = '\ue14d';
	public static final char CONTENT_PASTE = '\ue14f';
	public static final char CONTENT_CUT = '\ue14e';
	public static final char UNDO = '\ue166';
	public static final char REDO = '\ue15a';
	public static final char ZOOM_IN = '\ue8ff';
	public static final char ZOOM_OUT = '\ue900';
	public static final char FULLSCREEN = '\ue5d0';
	public static final char FULLSCREEN_EXIT = '\ue5d1';
	public static final char LIST = '\ue896';
	public static final char GRID_VIEW = '\ue9b0';
	public static final char VIEW_LIST = '\ue8ef';
	public static final char VIEW_MODULE = '\ue8f0';
	public static final char DASHBOARD = '\ue871';
	public static final char APPS = '\ue5c3';
	public static final char BOOKMARK = '\ue866';
	public static final char BOOKMARK_BORDER = '\ue867';
	public static final char THUMB_UP = '\ue8dc';
	public static final char THUMB_DOWN = '\ue8db';
	public static final char LOGOUT = '\ue9ba';
	public static final char LOGIN = '\uea77';
	public static final char CLOUD = '\ue2bd';
	public static final char CLOUD_UPLOAD = '\ue2c3';
	public static final char CLOUD_DOWNLOAD = '\ue2c0';
	public static final char WIFI = '\ue63e';
	public static final char BLUETOOTH = '\ue1a7';
	public static final char BATTERY_FULL = '\ue1a4';
	public static final char PHONE_ANDROID = '\ue324';
	public static final char COMPUTER = '\ue30a';
	public static final char PRINT = '\ue8ad';
	public static final char QR_CODE_SCANNER = '\uf206';
	public static final char KEY = '\ue73c';
	public static final char BUILD = '\ue869';
	public static final char BOLT = '\uea0b';
	public static final char CODE = '\ue86f';
	public static final char BUG_REPORT = '\ue868';
	public static final char EXTENSION = '\ue87b';
	public static final char SCHOOL = '\ue80c';
	public static final char WORK = '\ue8f9';
	public static final char ATTACH_MONEY = '\ue227';
	public static final char SPORTS_ESPORTS = '\uea28';
	public static final char CANCEL = '\ue5c9';
	public static final char CHECK_CIRCLE = '\ue86c';
	public static final char RADIO_BUTTON_CHECKED = '\ue837';
	public static final char RADIO_BUTTON_UNCHECKED = '\ue836';
	public static final char CHECK_BOX = '\ue834';
	public static final char CHECK_BOX_OUTLINE_BLANK = '\ue835';
	public static final char ADD_CIRCLE = '\ue147';
	public static final char REMOVE_CIRCLE = '\ue15c';
	public static final char CLEAR = '\ue14c';
	public static final char BACKSPACE = '\ue14a';
	public static final char KEYBOARD_ARROW_DOWN = '\ue313';
	public static final char KEYBOARD_ARROW_UP = '\ue316';
	public static final char KEYBOARD_ARROW_LEFT = '\ue314';
	public static final char KEYBOARD_ARROW_RIGHT = '\ue315';
	public static final char DRAG_HANDLE = '\ue25d';
	public static final char OPEN_IN_NEW = '\ue89e';
	public static final char HISTORY = '\ue889';
	public static final char LABEL = '\ue892';
	public static final char CATEGORY = '\ue574';
	public static final char LAYERS = '\ue53b';

	private static ObjectIntMap<String> names;

	private Icons () {
	}

	/** @return the icon's character, or 0 when the name is unknown. Also accepts a hex code such as "e145". */
	public static char get (String name) {
		if (name == null || name.length() == 0) return 0;
		if (names == null) load();
		String key = name.toLowerCase().replace('-', '_');
		if (key.startsWith("ic_")) key = key.substring(3);
		int v = names.get(key, 0);
		if (v == 0 && key.length() == 4) {
			try {
				v = Integer.parseInt(key, 16);
			} catch (NumberFormatException ignored) {
			}
		}
		return (char)v;
	}

	private static void load () {
		names = new ObjectIntMap<String>(2400);
		try {
			BufferedReader r = Gdx.files.classpath("compat/fonts/icons.codepoints").reader(4096, "UTF-8");
			String line;
			while ((line = r.readLine()) != null) {
				int sp = line.indexOf(' ');
				if (sp > 0) names.put(line.substring(0, sp), Integer.parseInt(line.substring(sp + 1).trim(), 16));
			}
			r.close();
		} catch (Exception e) {
			Gdx.app.error("Compat", "Could not read the icon table", e);
		}
	}
}
