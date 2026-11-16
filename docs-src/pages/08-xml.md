---
title: XML layouts
slug: xml
nav: XML layouts
---
# XML layouts

`LayoutInflater` builds a view tree from an Android-style XML file. Tag and attribute names are the ones you already know from Android; the `android:` and `app:` prefixes are optional.

<img class="shot right" src="images/11_xml.png" alt="A screen inflated from XML">

```xml
<!-- assets/layouts/login.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:text="@string/title"
        android:textAppearance="headlineSmall" />

    <EditText
        android:id="@+id/name"
        android:layout_width="match_parent"
        android:layout_marginTop="16dp"
        android:hint="Name"
        app:startIcon="person" />

    <Button
        android:layout_gravity="end"
        android:layout_marginTop="16dp"
        android:onClick="onSend"
        android:text="Send"
        app:icon="send" />
</LinearLayout>
```

<div class="clear"></div>

```java
public class LoginScreen {
    Actor root;

    Actor build () {
        root = new LayoutInflater()
                .putString("title", "Sign in")
                .inflate(Gdx.files.internal("layouts/login.xml"), this);   // this = onClick handler
        return root;
    }

    public void onSend () {                // or onSend(Actor view)
        EditText name = Views.find(root, "name");
        if (name.getText().isEmpty()) name.setError("Required");
    }
}
```

A root element with `match_parent` for both width and height fills its parent (`setFillParent(true)`). You can also inflate from a string: `inflater.inflate("<Button android:text='OK'/>")`.

## Supported tags

Fully qualified Material / AndroidX names (for example `com.google.android.material.card.MaterialCardView`) are accepted too; only the last part is used.

| Tag | Creates | Specific attributes |
|---|---|---|
| `LinearLayout`, `LinearLayoutCompat` | `LinearLayout` | `orientation`, `gravity`, `spacing` |
| `FrameLayout`, `RelativeLayout`*, `ConstraintLayout`*, `CoordinatorLayout`* | `FrameLayout` | |
| `FlowLayout`, `ChipGroup` | `FlowLayout` | `spacing`, `chipSpacing` |
| `ScrollView`, `NestedScrollView`, `HorizontalScrollView` | `ScrollView` | |
| `CardView`, `MaterialCardView` | `CardView` | `cardStyle` (elevated / filled / outlined), `cardCornerRadius`, `cardElevation`, `cardBackgroundColor`, `contentPadding` |
| `TextView`, `MaterialTextView` | `TextView` | `text`, `textAppearance`, `textSize`, `textColor`, `textStyle`, `gravity`, `textAlignment`, `maxLines`, `lines`, `singleLine`, `maxWidth` |
| `Button`, `MaterialButton` | `Button` | `text`, `style` / `buttonStyle` (filled, tonal, outlined, text, elevated), `icon`, `cornerRadius`, `backgroundTint`, `textColor`, `textSize` |
| `ImageButton`, `IconButton` | `IconButton` | `icon` / `src`, `style`, `tint` |
| `FloatingActionButton`, `ExtendedFloatingActionButton`, `Fab` | `Fab` | `icon` / `src`, `text`, `fabSize` (mini, normal, large), `backgroundTint`, `tint` |
| `EditText`, `TextInputEditText`, `TextInputLayout` | `EditText` | `hint`, `text`, `inputType`, `password`, `maxLength`, `startIcon`, `endIcon`, `helperText`, `error`, `style` / `boxStyle` (filled) |
| `CheckBox`, `RadioButton` | same | `text`, `checked` |
| `RadioGroup` | `RadioGroup` | `orientation` |
| `Switch`, `SwitchCompat`, `MaterialSwitch` | `Switch` | `text`, `checked` |
| `Slider`, `SeekBar` | `Slider` | `valueFrom`/`valueTo`/`value`/`stepSize`, or `max`/`progress` |
| `Chip` | `Chip` | `text`, `chipIcon`, `checkable`, `checked` |
| `ProgressBar`, `LinearProgressIndicator`, `CircularProgressIndicator` | `ProgressBar` | `progress`, `max`, `indeterminate`, `style` (horizontal) |
| `ImageView`, `ShapeableImageView`, `Icon`, `IconView` | `ImageView` or `IconView` | `src`, `scaleType`, `cornerRadius`, `tint`, `iconSize` |
| `Divider`, `MaterialDivider` | `Divider` | `orientation`, `dividerColor` |
| `Space` | `Space` | |
| `RecyclerView`, `ListView`, `GridView` | `RecyclerView` | `orientation`, `spanCount` / `numColumns`, `spacing`, `dividers` |
| `ViewPager`, `ViewPager2` | `ViewPager` | children become pages |
| `TabLayout` | `TabLayout` | `tabMode` (fixed / scrollable); children `<TabItem text icon />` |
| `Toolbar`, `MaterialToolbar` | `Toolbar` | `title`, `titleCentered`, `navigationIcon`, `onNavigationClick`; children `<item icon id onClick />` |
| `BottomNavigationView` | `BottomNavigationView` | children `<item icon title />` |
| `NavigationView` | `NavigationView` | children `<item icon title onClick />`, `<divider />`, `<section title />`, any view as header |
| `DrawerLayout` | `DrawerLayout` | first child = content, second = drawer |
| `include` | the included layout | `layout="@layout/name"` (file `name.xml` next to the current one) |

\* These layouts are mapped to `FrameLayout`: their children are stacked and positioned with `layout_gravity` and margins. Relative and constraint rules are not supported.

A tag with a dot that is not a known name is treated as a class name and instantiated with its no-argument constructor, so your own `Actor` classes work out of the box.

## Common attributes

| Attribute | Notes |
|---|---|
| `id` | `@+id/name` or `name`; look it up with `Views.find(root, "name")` |
| `layout_width`, `layout_height` | `match_parent`, `wrap_content`, or a dimension |
| `layout_weight`, `layout_gravity` | |
| `layout_margin`, `layout_marginHorizontal`, `layout_marginVertical`, `layout_marginStart/End/Left/Right/Top/Bottom` | |
| `padding`, `paddingHorizontal`, `paddingVertical`, `paddingStart/End/Left/Right/Top/Bottom` | layouts and `TextView` |
| `background` | a colour (layouts) |
| `cornerRadius`, `elevation`, `strokeColor`, `strokeWidth`, `clipChildren` | layouts |
| `onClick` | method name on the handler object; adds a ripple to layouts |
| `clickable` | gives a layout a ripple without a handler |
| `visibility` | anything but `visible` hides the view |
| `enabled`, `alpha` | |

## Values

| Kind | Accepted forms |
|---|---|
| Dimensions | `16dp`, `16dip`, `14sp`, `24px`, a bare number (dp), `@dimen/name` |
| Colours | `#RGB`, `#RRGGBB`, `#AARRGGBB`, `@color/primary`, `?attr/colorPrimary`, `?colorOnSurface`, `@android:color/white`, `transparent` |
| Text | plain text, `@string/name`, escapes `\n` `\t` `\'` |
| Icons | `add`, `ic_add`, `@drawable/ic_add`, or a hex code point |
| Text appearance | `titleLarge`, `bodyMedium`, `@style/TextAppearance.Material3.HeadlineSmall` … |

Theme colour roles (`primary`, `onSurfaceVariant`, `surfaceContainerHigh` …) resolve to the live theme colours, so inflated views follow theme changes. Register your own values:

```java
LayoutInflater inflater = new LayoutInflater()
        .putString("app_name", "My App")
        .putColor("brand", Color.valueOf("FF6D00"))
        .putDimen("gutter", 20)                           // in dp
        .setDrawables(name -> atlas.findRegion(name));    // for <ImageView android:src="@drawable/x">
```

## onClick handlers

`android:onClick="name"` calls a public method on the object passed to `inflate(file, handler)`. The method may take no argument or one argument whose type matches the view. If the method does not exist, inflating throws an exception naming it, so typos show up immediately.

## Custom tags

```java
LayoutInflater.register("RatingBar", (attrs, inflater) ->
        new RatingBar(attrs.integer("stars", 5), attrs.color("color")));
```

`attrs` offers `string`, `dimen`, `color`, `bool`, `integer`, `number`, `icon` and `raw`. Common attributes (`id`, `layout_*`, padding, background, `onClick` …) are applied after your factory returns. If your view is a `ViewGroup`, its XML children are added automatically.
