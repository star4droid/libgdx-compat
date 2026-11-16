package com.star4droid.compat.xml;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.star4droid.compat.Compat;
import com.star4droid.compat.Gravity;
import com.star4droid.compat.Icons;
import com.star4droid.compat.LayoutParams;
import com.star4droid.compat.Theme;
import com.star4droid.compat.Typography;
import com.star4droid.compat.widget.*;

/** Builds a view tree from an Android-style XML layout.
 *
 * <pre>
 * Actor root = new LayoutInflater().inflate(Gdx.files.internal("layouts/login.xml"), this);
 * EditText name = Views.find(root, "name");
 * </pre>
 *
 * Tags and attributes follow Android's names ({@code LinearLayout}, {@code TextView}, {@code layout_width},
 * {@code android:text} ...); the {@code android:} / {@code app:} prefixes are optional. See the README for the full list. */
public class LayoutInflater {
	/** Creates a view for a custom tag. Common attributes (id, layout_*, padding, background...) are applied afterwards. */
	public interface ViewFactory {
		Actor create (Attrs attrs, LayoutInflater inflater);
	}

	/** Supplies images for {@code src="@drawable/name"}. */
	public interface DrawableResolver {
		TextureRegion get (String name);
	}

	private static final ObjectMap<String, ViewFactory> CUSTOM = new ObjectMap<String, ViewFactory>();

	/** Registers a custom tag for all inflaters. */
	public static void register (String tag, ViewFactory factory) {
		CUSTOM.put(tag, factory);
	}

	private final ObjectMap<String, String> strings = new ObjectMap<String, String>();
	private final ObjectMap<String, Color> colors = new ObjectMap<String, Color>();
	private final ObjectMap<String, Float> dimens = new ObjectMap<String, Float>();
	private DrawableResolver drawables;
	private Object handler;
	private FileHandle baseDir;

	/** {@code @string/name} values. */
	public LayoutInflater putString (String name, String value) {
		strings.put(name, value);
		return this;
	}

	public LayoutInflater putStrings (ObjectMap<String, String> map) {
		strings.putAll(map);
		return this;
	}

	/** {@code @color/name} values (theme roles such as {@code @color/primary} work without registering). */
	public LayoutInflater putColor (String name, Color value) {
		colors.put(name, value);
		return this;
	}

	/** {@code @dimen/name} values in dp. */
	public LayoutInflater putDimen (String name, float dp) {
		dimens.put(name, dp);
		return this;
	}

	public LayoutInflater setDrawables (DrawableResolver r) {
		drawables = r;
		return this;
	}

	public Actor inflate (FileHandle file) {
		return inflate(file, null);
	}

	/** @param handler object whose methods are called by {@code onClick="methodName"} */
	public Actor inflate (FileHandle file, Object handler) {
		FileHandle oldBase = baseDir;
		Object oldHandler = this.handler;
		baseDir = file.parent();
		if (handler != null) this.handler = handler;
		try {
			return build(new XmlReader().parse(file), null, true);
		} finally {
			baseDir = oldBase;
			this.handler = oldHandler;
		}
	}

	public Actor inflate (String xml) {
		return inflate(xml, null);
	}

	public Actor inflate (String xml, Object handler) {
		Object oldHandler = this.handler;
		if (handler != null) this.handler = handler;
		try {
			return build(new XmlReader().parse(xml), null, true);
		} finally {
			this.handler = oldHandler;
		}
	}

	// ------------------------------------------------------------------ attribute access

	/** The attributes of one XML element with typed getters. */
	public final class Attrs {
		final XmlReader.Element element;
		final ObjectMap<String, String> map = new ObjectMap<String, String>();

		Attrs (XmlReader.Element e) {
			element = e;
			ObjectMap<String, String> raw = e.getAttributes();
			if (raw != null) for (ObjectMap.Entry<String, String> en : raw) {
				String k = en.key;
				if (k.startsWith("xmlns") || k.startsWith("tools:")) continue;
				int c = k.indexOf(':');
				map.put(c >= 0 ? k.substring(c + 1) : k, en.value);
			}
		}

		public boolean has (String name) {
			return map.containsKey(name);
		}

		public String raw (String name) {
			return map.get(name);
		}

		public String string (String name) {
			String v = map.get(name);
			return v == null ? null : resolveString(v);
		}

		public String string (String a, String b) {
			return has(a) ? string(a) : string(b);
		}

		public float dimen (String name, float def) {
			String v = map.get(name);
			return v == null ? def : parseDimen(v);
		}

		public Color color (String name) {
			String v = map.get(name);
			return v == null ? null : parseColor(v);
		}

		public boolean bool (String name, boolean def) {
			String v = map.get(name);
			return v == null ? def : v.trim().equalsIgnoreCase("true");
		}

		public int integer (String name, int def) {
			String v = map.get(name);
			return v == null ? def : (int)Float.parseFloat(v.trim());
		}

		public float number (String name, float def) {
			String v = map.get(name);
			return v == null ? def : Float.parseFloat(v.trim());
		}

		public char icon (String name) {
			String v = map.get(name);
			return v == null ? 0 : parseIcon(v);
		}

		/** First of the given attributes that names a known icon. */
		public char icon (String a, String b) {
			char c = icon(a);
			return c != 0 ? c : icon(b);
		}
	}

	String resolveString (String v) {
		if (v.startsWith("@string/")) {
			String s = strings.get(v.substring(8));
			return s != null ? s : v.substring(8);
		}
		if (v.indexOf('\\') >= 0) v = v.replace("\\n", "\n").replace("\\t", "\t").replace("\\'", "'").replace("\\\"", "\"");
		return v;
	}

	/** Parses "16dp", "14sp", "24px", "@dimen/x", "match_parent", "wrap_content" or a plain number (dp). */
	public float parseDimen (String v) {
		v = v.trim();
		if (v.equals("match_parent") || v.equals("fill_parent")) return LayoutParams.MATCH_PARENT;
		if (v.equals("wrap_content")) return LayoutParams.WRAP_CONTENT;
		if (v.startsWith("@dimen/")) {
			Float d = dimens.get(v.substring(7));
			if (d == null) throw new GdxRuntimeException("Unknown dimension: " + v);
			return Compat.dp(d);
		}
		if (v.endsWith("dip")) return Compat.dp(Float.parseFloat(v.substring(0, v.length() - 3)));
		if (v.endsWith("dp")) return Compat.dp(Float.parseFloat(v.substring(0, v.length() - 2)));
		if (v.endsWith("sp")) return Compat.sp(Float.parseFloat(v.substring(0, v.length() - 2)));
		if (v.endsWith("px")) return Float.parseFloat(v.substring(0, v.length() - 2));
		return Compat.dp(Float.parseFloat(v));
	}

	/** Parses "#RRGGBB", "#AARRGGBB", "@color/name", "?attr/colorPrimary", "?colorOnSurface" or a theme role name. */
	public Color parseColor (String v) {
		v = v.trim();
		if (v.startsWith("#")) return Theme.parseColor(v);
		String name = v;
		if (name.startsWith("@color/")) name = name.substring(7);
		else if (name.startsWith("@android:color/")) name = name.substring(15);
		else if (name.startsWith("?attr/")) name = name.substring(6);
		else if (name.startsWith("?android:attr/")) name = name.substring(14);
		else if (name.startsWith("?")) name = name.substring(1);
		if (name.equals("transparent")) return null;
		Color c = colors.get(name);
		if (c != null) return c;
		c = Compat.theme.color(name);
		if (c != null) return c;
		if (name.startsWith("color") && name.length() > 5) {
			c = Compat.theme.color(Character.toLowerCase(name.charAt(5)) + name.substring(6));
			if (c != null) return c;
		}
		if (name.equals("white")) return Color.WHITE;
		if (name.equals("black")) return Color.BLACK;
		throw new GdxRuntimeException("Unknown color: " + v);
	}

	char parseIcon (String v) {
		String n = v.trim();
		int slash = n.lastIndexOf('/');
		if (slash >= 0) n = n.substring(slash + 1);
		return Icons.get(n);
	}

	private static int parseStyle (String v, String[] names) {
		if (v == null) return -1;
		String s = v.toLowerCase();
		for (int i = 0; i < names.length; i++)
			if (s.contains(names[i])) return i;
		return -1;
	}

	// ------------------------------------------------------------------ building

	private Actor build (XmlReader.Element e, ViewGroup parent, boolean isRoot) {
		String tag = e.getName();
		int dot = tag.lastIndexOf('.');
		String simple = dot >= 0 ? tag.substring(dot + 1) : tag;
		Attrs a = new Attrs(e);

		if (simple.equals("include")) {
			String layout = a.raw("layout");
			if (layout == null) throw new GdxRuntimeException("<include> needs a layout attribute");
			if (layout.startsWith("@layout/")) layout = layout.substring(8) + ".xml";
			if (baseDir == null) throw new GdxRuntimeException("<include> only works when inflating from a file");
			Actor included = build(new XmlReader().parse(baseDir.child(layout)), parent, false);
			applyCommon(included, a, parent, false);
			return included;
		}

		Actor view = create(simple, tag, a);
		applyCommon(view, a, parent, isRoot);
		return view;
	}

	private void addChildren (ViewGroup group, Attrs a) {
		XmlReader.Element e = a.element;
		for (int i = 0; i < e.getChildCount(); i++) {
			XmlReader.Element c = e.getChild(i);
			Actor child = build(c, group, false);
			if (child.getParent() == null) {
				LayoutParams lp = pendingParams;
				pendingParams = null;
				if (lp != null) group.add(child, lp);
				else group.addActor(child);
			}
		}
	}

	private LayoutParams pendingParams;

	private static final String[] BUTTON_STYLES = {"filled", "tonal", "outlined", "text", "elevated"};

	private Actor create (String tag, String fullTag, final Attrs a) {
		ViewFactory custom = CUSTOM.get(tag);
		if (custom == null) custom = CUSTOM.get(fullTag);
		if (custom != null) {
			Actor v = custom.create(a, this);
			if (v instanceof ViewGroup) addChildren((ViewGroup)v, a);
			return v;
		}
		if (tag.equals("LinearLayout") || tag.equals("LinearLayoutCompat") || tag.equals("RadioGroup")) {
			boolean radio = tag.equals("RadioGroup");
			String o = a.raw("orientation");
			int orientation = o != null ? (o.equals("vertical") ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL)
				: (radio ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
			LinearLayout l = radio ? new RadioGroup(orientation) : new LinearLayout(orientation);
			if (a.has("gravity")) l.setGravity(Gravity.parse(a.raw("gravity")));
			if (a.has("spacing")) l.setSpacing(a.dimen("spacing", 0));
			addChildren(l, a);
			return l;
		}
		if (tag.equals("FrameLayout") || tag.equals("View") || tag.equals("RelativeLayout") || tag.equals("ConstraintLayout")
			|| tag.equals("CoordinatorLayout")) {
			FrameLayout f = new FrameLayout();
			addChildren(f, a);
			return f;
		}
		if (tag.equals("FlowLayout") || tag.equals("ChipGroup")) {
			FlowLayout f = new FlowLayout();
			if (a.has("spacing") || a.has("chipSpacing")) {
				float s = a.dimen(a.has("spacing") ? "spacing" : "chipSpacing", 0);
				f.setSpacing(s, s);
			}
			addChildren(f, a);
			return f;
		}
		if (tag.equals("ScrollView") || tag.equals("NestedScrollView") || tag.equals("HorizontalScrollView")) {
			ScrollView s = new ScrollView(!tag.equals("HorizontalScrollView"));
			addChildren(s, a);
			return s;
		}
		if (tag.equals("CardView") || tag.equals("MaterialCardView")) {
			int st = parseStyle(a.has("cardStyle") ? a.raw("cardStyle") : a.raw("style"), new String[] {"elevated", "filled", "outlined"});
			CardView c = new CardView(st < 0 ? CardView.ELEVATED : st);
			if (a.has("cardCornerRadius")) c.setCornerRadius(a.dimen("cardCornerRadius", 0));
			if (a.has("cardElevation")) c.setElevation(a.dimen("cardElevation", 0));
			if (a.has("cardBackgroundColor")) c.setBackground(a.color("cardBackgroundColor"));
			if (a.has("contentPadding")) c.setPadding(a.dimen("contentPadding", 0));
			addChildren(c, a);
			return c;
		}
		if (tag.equals("TextView") || tag.equals("MaterialTextView") || tag.equals("AppCompatTextView")) {
			int style = Typography.BODY_LARGE;
			String ta = a.has("textAppearance") ? a.raw("textAppearance") : a.raw("style");
			if (ta != null) {
				int d = Math.max(ta.lastIndexOf('.'), ta.lastIndexOf('/'));
				int p = Typography.parse(d >= 0 ? ta.substring(d + 1) : ta);
				if (p >= 0) style = p;
			}
			TextView t = new TextView(a.has("text") ? a.string("text") : "", style);
			if (a.has("textSize")) t.setTextSize(a.dimen("textSize", 0));
			if (a.has("textColor")) t.setTextColor(a.color("textColor"));
			if (a.has("textStyle")) t.setBold(a.raw("textStyle").contains("bold"));
			int g = a.has("gravity") ? Gravity.parse(a.raw("gravity")) : 0;
			String al = a.raw("textAlignment");
			if (al != null) {
				if (al.equals("center")) g |= Gravity.CENTER_HORIZONTAL;
				else if (al.equals("textEnd") || al.equals("viewEnd")) g |= Gravity.END;
			}
			if (g != 0) t.setGravity(g);
			if (a.has("maxLines")) t.setMaxLines(a.integer("maxLines", 0));
			if (a.has("lines")) t.setMaxLines(a.integer("lines", 0));
			if (a.bool("singleLine", false)) t.setSingleLine();
			if (a.has("maxWidth")) t.setMaxWidth(a.dimen("maxWidth", 0));
			float p = a.dimen("padding", 0);
			if (a.has("padding") || a.has("paddingLeft") || a.has("paddingTop") || a.has("paddingRight") || a.has("paddingBottom")
				|| a.has("paddingStart") || a.has("paddingEnd") || a.has("paddingHorizontal") || a.has("paddingVertical")) {
				float ph = a.dimen("paddingHorizontal", p), pv = a.dimen("paddingVertical", p);
				t.setPadding(a.dimen("paddingLeft", a.dimen("paddingStart", ph)), a.dimen("paddingTop", pv),
					a.dimen("paddingRight", a.dimen("paddingEnd", ph)), a.dimen("paddingBottom", pv));
			}
			return t;
		}
		if (tag.equals("Button") || tag.equals("MaterialButton") || tag.equals("AppCompatButton")) {
			int st = parseStyle(a.has("buttonStyle") ? a.raw("buttonStyle") : a.raw("style"), BUTTON_STYLES);
			Button b = new Button(a.has("text") ? a.string("text") : "", st < 0 ? Button.FILLED : st);
			char ic = a.icon("icon", "drawableStart");
			if (ic != 0) b.icon(ic);
			if (a.has("cornerRadius")) b.cornerRadius(a.dimen("cornerRadius", 0));
			if (a.has("backgroundTint") || a.has("textColor")) b.colors(a.color("backgroundTint"), a.color("textColor"));
			if (a.has("textSize")) b.textSize(a.dimen("textSize", 0));
			return b;
		}
		if (tag.equals("ImageButton") || tag.equals("IconButton") || tag.equals("AppCompatImageButton")) {
			IconButton b = new IconButton(a.icon("icon", "src"));
			int st = parseStyle(a.raw("style"), new String[] {"standard", "filled", "tonal", "outlined"});
			if (st > 0) b.style(st);
			if (a.has("tint")) b.iconColor(a.color("tint"));
			return b;
		}
		if (tag.equals("FloatingActionButton") || tag.equals("Fab") || tag.equals("ExtendedFloatingActionButton")) {
			Fab f = new Fab(a.icon("icon", "src"));
			if (a.has("text")) f.text(a.string("text"));
			String size = a.raw("fabSize");
			if (size != null) f.size(size.equals("mini") || size.equals("small") ? Fab.SMALL : size.equals("large") ? Fab.LARGE : Fab.NORMAL);
			if (a.has("backgroundTint") || a.has("tint")) f.colors(a.color("backgroundTint"), a.color("tint"));
			return f;
		}
		if (tag.equals("EditText") || tag.equals("TextInputEditText") || tag.equals("TextInputLayout")
			|| tag.equals("AppCompatEditText")) {
			EditText ed = new EditText(a.string("hint"));
			String st = a.has("boxStyle") ? a.raw("boxStyle") : a.raw("style");
			if (st != null && st.toLowerCase().contains("filled")) ed.setStyle(EditText.FILLED);
			if (a.has("text")) ed.setText(a.string("text"));
			String it = a.raw("inputType");
			if (it != null) {
				if (it.contains("Password")) ed.setPassword(true);
				else if (it.contains("number") || it.contains("phone")) ed.setNumeric();
				else if (it.contains("Email")) ed.setKeyboardType(com.badlogic.gdx.Input.OnscreenKeyboardType.Email);
			}
			if (a.bool("password", false)) ed.setPassword(true);
			if (a.has("maxLength")) ed.setMaxLength(a.integer("maxLength", 0));
			char si = a.icon("startIcon", "startIconDrawable");
			if (si != 0) ed.setLeadingIcon(si);
			char ei = a.icon("endIcon", "endIconDrawable");
			if (ei != 0) ed.setTrailingIcon(ei, null);
			if (a.has("helperText")) ed.setHelperText(a.string("helperText"));
			if (a.has("error")) ed.setError(a.string("error"));
			return ed;
		}
		if (tag.equals("CheckBox") || tag.equals("MaterialCheckBox") || tag.equals("AppCompatCheckBox")) {
			CheckBox c = new CheckBox(a.string("text"));
			c.setChecked(a.bool("checked", false), false);
			return c;
		}
		if (tag.equals("RadioButton") || tag.equals("MaterialRadioButton")) {
			RadioButton r = new RadioButton(a.string("text"));
			r.setChecked(a.bool("checked", false), false);
			return r;
		}
		if (tag.equals("Switch") || tag.equals("SwitchCompat") || tag.equals("SwitchMaterial") || tag.equals("MaterialSwitch")) {
			Switch s = new Switch(a.string("text"));
			s.setChecked(a.bool("checked", false), false);
			return s;
		}
		if (tag.equals("Slider") || tag.equals("SeekBar")) {
			float from = a.number(a.has("valueFrom") ? "valueFrom" : "min", 0);
			float to = a.number(a.has("valueTo") ? "valueTo" : "max", tag.equals("SeekBar") ? 100 : 1);
			Slider s = new Slider(from, to);
			if (a.has("stepSize")) s.setStep(a.number("stepSize", 0));
			s.setValue(a.number(a.has("value") ? "value" : "progress", from));
			return s;
		}
		if (tag.equals("Chip")) {
			Chip c = new Chip(a.has("text") ? a.string("text") : "");
			char ic = a.icon("chipIcon", "icon");
			if (ic != 0) c.icon(ic);
			if (a.bool("checkable", false) || a.has("checked")) c.checkable(true);
			if (a.bool("checked", false)) c.setSelected(true);
			return c;
		}
		if (tag.equals("ProgressBar") || tag.equals("LinearProgressIndicator") || tag.equals("CircularProgressIndicator")) {
			String st = a.raw("style");
			boolean circular = tag.startsWith("Circular")
				|| (tag.equals("ProgressBar") && (st == null || !st.toLowerCase().contains("horizontal")) && !a.has("progress")
					&& !"linear".equals(a.raw("type")))
				|| "circular".equals(a.raw("type"));
			ProgressBar p = new ProgressBar(circular ? ProgressBar.CIRCULAR : ProgressBar.LINEAR);
			if (a.has("progress") && !a.bool("indeterminate", false)) p.setProgress(a.number("progress", 0) / a.number("max", 100));
			return p;
		}
		if (tag.equals("ImageView") || tag.equals("AppCompatImageView") || tag.equals("ShapeableImageView")
			|| tag.equals("Icon") || tag.equals("IconView")) {
			String src = a.has("src") ? a.raw("src") : a.has("srcCompat") ? a.raw("srcCompat") : a.raw("icon");
			TextureRegion region = null;
			if (src != null && drawables != null && !tag.startsWith("Icon")) {
				int slash = src.lastIndexOf('/');
				region = drawables.get(slash >= 0 ? src.substring(slash + 1) : src);
			}
			if (region == null && src != null && parseIcon(src) != 0) {
				IconView iv = new IconView(parseIcon(src));
				if (a.has("tint")) iv.setIconColor(a.color("tint"));
				if (a.has("iconSize")) iv.setIconSize(a.dimen("iconSize", 0));
				else if (a.has("layout_width") && parseDimen(a.raw("layout_width")) > 0)
					iv.setIconSize(Math.min(parseDimen(a.raw("layout_width")), parseDimen(a.has("layout_height") ? a.raw("layout_height") : a.raw("layout_width"))));
				return iv;
			}
			ImageView iv = new ImageView(region);
			String sc = a.raw("scaleType");
			if (sc != null) iv.setScaleType(sc.equals("centerCrop") ? ImageView.CENTER_CROP
				: sc.equals("fitXY") ? ImageView.FIT_XY : sc.equals("center") ? ImageView.CENTER : ImageView.FIT_CENTER);
			if (a.has("cornerRadius")) iv.setCornerRadius(a.dimen("cornerRadius", 0));
			if (a.has("tint")) iv.setTint(a.color("tint"));
			if (a.has("background")) iv.setPlaceholder(a.color("background"));
			return iv;
		}
		if (tag.equals("Divider") || tag.equals("MaterialDivider")) {
			Divider d = new Divider("vertical".equals(a.raw("orientation")));
			if (a.has("dividerColor")) d.setLineColor(a.color("dividerColor"));
			return d;
		}
		if (tag.equals("Space")) return new Space();
		if (tag.equals("RecyclerView") || tag.equals("ListView") || tag.equals("GridView")) {
			RecyclerView rv = new RecyclerView(!"horizontal".equals(a.raw("orientation")));
			if (a.has("spanCount") || a.has("numColumns")) rv.setSpanCount(a.integer(a.has("spanCount") ? "spanCount" : "numColumns", 1));
			if (a.has("spacing")) rv.setSpacing(a.dimen("spacing", 0));
			if (a.bool("dividers", false)) rv.setDividers(true, 0);
			return rv;
		}
		if (tag.equals("ViewPager") || tag.equals("ViewPager2")) {
			ViewPager vp = new ViewPager();
			XmlReader.Element e = a.element;
			for (int i = 0; i < e.getChildCount(); i++)
				vp.addPage(build(e.getChild(i), null, false));
			return vp;
		}
		if (tag.equals("TabLayout")) {
			TabLayout tl = new TabLayout("scrollable".equals(a.raw("tabMode")) ? TabLayout.MODE_SCROLLABLE : TabLayout.MODE_FIXED);
			XmlReader.Element e = a.element;
			for (int i = 0; i < e.getChildCount(); i++) {
				Attrs ta = new Attrs(e.getChild(i));
				tl.addTab(ta.has("text") ? ta.string("text") : "", ta.icon("icon"));
			}
			return tl;
		}
		if (tag.equals("Toolbar") || tag.equals("MaterialToolbar") || tag.equals("TopAppBar")) {
			Toolbar tb = new Toolbar(a.has("title") ? a.string("title") : "");
			if (a.bool("titleCentered", false)) tb.setCentered(true);
			char nav = a.icon("navigationIcon");
			if (nav != 0) tb.setNavigationIcon(nav, click(a.raw("onNavigationClick"), tb));
			XmlReader.Element e = a.element;
			for (int i = 0; i < e.getChildCount(); i++) {
				Attrs ia = new Attrs(e.getChild(i));
				IconButton b = tb.addAction(ia.icon("icon"), null);
				if (ia.has("id")) b.setName(parseId(ia.raw("id")));
				if (ia.has("onClick")) b.setOnClick(click(ia.raw("onClick"), b));
			}
			return tb;
		}
		if (tag.equals("BottomNavigationView") || tag.equals("NavigationBarView")) {
			BottomNavigationView bn = new BottomNavigationView();
			XmlReader.Element e = a.element;
			for (int i = 0; i < e.getChildCount(); i++) {
				Attrs ia = new Attrs(e.getChild(i));
				bn.addItem(ia.icon("icon"), ia.has("title") ? ia.string("title") : ia.has("text") ? ia.string("text") : "");
			}
			return bn;
		}
		if (tag.equals("NavigationView")) {
			NavigationView nv = new NavigationView();
			XmlReader.Element e = a.element;
			for (int i = 0; i < e.getChildCount(); i++) {
				XmlReader.Element c = e.getChild(i);
				Attrs ia = new Attrs(c);
				if (c.getName().equals("item"))
					nv.addItem(ia.icon("icon"), ia.has("title") ? ia.string("title") : ia.string("text"), click(ia.raw("onClick"), nv));
				else if (c.getName().equals("divider")) nv.addDivider();
				else if (c.getName().equals("section")) nv.addSectionTitle(ia.has("title") ? ia.string("title") : ia.string("text"));
				else nv.addHeader(build(c, null, false));
			}
			return nv;
		}
		if (tag.equals("DrawerLayout")) {
			DrawerLayout dl = new DrawerLayout();
			XmlReader.Element e = a.element;
			if (e.getChildCount() > 0) dl.setContent(build(e.getChild(0), null, false));
			if (e.getChildCount() > 1) dl.setDrawer(build(e.getChild(1), null, false));
			return dl;
		}
		// a fully qualified class name of any Actor with a no-arg constructor
		if (fullTag.indexOf('.') > 0) {
			try {
				Object o = ClassReflection.newInstance(ClassReflection.forName(fullTag));
				if (o instanceof ViewGroup) addChildren((ViewGroup)o, a);
				return (Actor)o;
			} catch (Exception ex) {
				throw new GdxRuntimeException("Cannot create view " + fullTag, ex);
			}
		}
		throw new GdxRuntimeException("Unknown layout tag <" + fullTag + ">. Register it with LayoutInflater.register().");
	}

	private static String parseId (String v) {
		int slash = v.lastIndexOf('/');
		return slash >= 0 ? v.substring(slash + 1) : v;
	}

	private Runnable click (final String method, final Actor view) {
		if (method == null) return null;
		final Object target = handler;
		if (target == null) throw new GdxRuntimeException("onClick=\"" + method + "\" needs a handler: inflate(file, handler)");
		Method found = null;
		boolean withArg = false;
		for (Class<?> c = target.getClass(); c != null && found == null; c = c.getSuperclass()) {
			for (Method m : ClassReflection.getDeclaredMethods(c)) {
				if (!m.getName().equals(method)) continue;
				Class[] p = m.getParameterTypes();
				if (p.length == 0 || (p.length == 1 && ClassReflection.isAssignableFrom(p[0], view.getClass()))) {
					found = m;
					withArg = p.length == 1;
					break;
				}
			}
		}
		if (found == null)
			throw new GdxRuntimeException("No method " + method + "() or " + method + "(Actor) in " + target.getClass().getName());
		final Method m = found;
		final boolean arg = withArg;
		m.setAccessible(true);
		return new Runnable() {
			@Override
			public void run () {
				try {
					if (arg) m.invoke(target, view);
					else m.invoke(target);
				} catch (Exception ex) {
					throw new GdxRuntimeException("onClick handler " + method + " failed", ex);
				}
			}
		};
	}

	private void applyCommon (Actor v, Attrs a, ViewGroup parent, boolean isRoot) {
		if (a.has("id")) v.setName(parseId(a.raw("id")));
		String vis = a.raw("visibility");
		if (vis != null && !vis.equals("visible")) v.setVisible(false);
		if (a.has("alpha")) v.getColor().a = a.number("alpha", 1);

		if (v instanceof ViewGroup) {
			ViewGroup g = (ViewGroup)v;
			float p = a.dimen("padding", -1);
			if (p >= 0 || a.has("paddingLeft") || a.has("paddingTop") || a.has("paddingRight") || a.has("paddingBottom")
				|| a.has("paddingStart") || a.has("paddingEnd") || a.has("paddingHorizontal") || a.has("paddingVertical")) {
				float base = Math.max(0, p), ph = a.dimen("paddingHorizontal", base), pv = a.dimen("paddingVertical", base);
				g.setPadding(a.dimen("paddingLeft", a.dimen("paddingStart", ph)), a.dimen("paddingTop", pv),
					a.dimen("paddingRight", a.dimen("paddingEnd", ph)), a.dimen("paddingBottom", pv));
			}
			if (a.has("background")) g.setBackground(a.color("background"));
			if (a.has("cornerRadius")) g.setCornerRadius(a.dimen("cornerRadius", 0));
			if (a.has("elevation")) g.setElevation(a.dimen("elevation", 0));
			if (a.has("strokeColor")) g.setStroke(a.color("strokeColor"), a.dimen("strokeWidth", Compat.dp(1)));
			if (a.bool("clipChildren", false)) g.setClip(true);
			if (a.has("onClick") && a.raw("onClick").length() > 0) g.setOnClick(click(a.raw("onClick"), v));
			if (a.bool("clickable", false) && !a.has("onClick")) g.setOnClick(new Runnable() {
				@Override
				public void run () {
				}
			});
			if (!a.bool("enabled", true)) g.setEnabled(false);
		} else if (v instanceof View) {
			View view = (View)v;
			if (a.has("onClick")) view.setOnClick(click(a.raw("onClick"), v));
			if (!a.bool("enabled", true)) view.setEnabled(false);
		}
		if ("false".equals(a.raw("touchable"))) v.setTouchable(Touchable.disabled);

		boolean hasLp = a.has("layout_width") || a.has("layout_height") || a.has("layout_weight") || a.has("layout_gravity")
			|| a.has("layout_margin") || a.has("layout_marginTop") || a.has("layout_marginBottom") || a.has("layout_marginLeft")
			|| a.has("layout_marginRight") || a.has("layout_marginStart") || a.has("layout_marginEnd")
			|| a.has("layout_marginHorizontal") || a.has("layout_marginVertical");
		if (isRoot) {
			if (v instanceof WidgetGroup && "match_parent".equals(a.raw("layout_width")) && "match_parent".equals(a.raw("layout_height")))
				((WidgetGroup)v).setFillParent(true);
			return;
		}
		if (!hasLp) return;
		LayoutParams lp = parent != null && v.getParent() == parent ? parent.lp(v) : new LayoutParams();
		if (a.has("layout_width")) lp.width = parseDimen(a.raw("layout_width"));
		if (a.has("layout_height")) lp.height = parseDimen(a.raw("layout_height"));
		lp.weight = a.number("layout_weight", lp.weight);
		if (a.has("layout_gravity")) lp.gravity = Gravity.parse(a.raw("layout_gravity"));
		float m = a.dimen("layout_margin", 0), mh = a.dimen("layout_marginHorizontal", m), mv = a.dimen("layout_marginVertical", m);
		lp.marginLeft = a.dimen("layout_marginLeft", a.dimen("layout_marginStart", mh));
		lp.marginRight = a.dimen("layout_marginRight", a.dimen("layout_marginEnd", mh));
		lp.marginTop = a.dimen("layout_marginTop", mv);
		lp.marginBottom = a.dimen("layout_marginBottom", mv);
		if (parent != null && v.getParent() != parent) pendingParams = lp;
	}
}
