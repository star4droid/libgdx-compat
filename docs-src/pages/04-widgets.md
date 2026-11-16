---
title: Widgets
slug: widgets
nav: Widgets
---
# Widgets

Leaf widgets draw themselves directly (no inner actors), which keeps the scene graph small. All of them take `setOnClick` / `setOnLongClick`, `setEnabled`, and an id via `.id("name")`.

## TextView

Wrapped text with maximum lines and ellipsis, alignment and automatic Arabic shaping.

```java
TextView title = new TextView("Settings", Typography.TITLE_LARGE);
TextView body = new TextView(longText, Typography.BODY_MEDIUM)
        .setTextColor(Compat.theme.onSurfaceVariant)
        .setMaxLines(2)
        .setGravity(Gravity.CENTER_HORIZONTAL);
```

| Method | Notes |
|---|---|
| `setText(text)` | re-measures only when needed |
| `setTextAppearance(style)` | one of the `Typography` constants |
| `setTextSize(px)`, `setBold(b)`, `setTextColor(c)` | |
| `setMaxLines(n)`, `setSingleLine()` | extra text ends with … |
| `setGravity(g)` | without a horizontal flag the text follows its own direction |
| `setPadding(...)`, `setMaxWidth(px)` | |

## Buttons

<img class="shot right" src="images/01_widgets.png" alt="Buttons, FABs and progress indicators">

```java
new Button("Filled");
new Button("Tonal", Button.TONAL).icon(Icons.ADD);
new Button("Outlined", Button.OUTLINED);
new Button("Text", Button.TEXT);
new Button("Elevated", Button.ELEVATED).icon(Icons.FAVORITE);

Button save = new Button("Save")
        .onClick(() -> save())
        .colors(myBackground, myTextColor)   // null keeps the style's colour
        .cornerRadius(dp(8))                 // default: fully rounded
        .height(dp(48));
save.setEnabled(false);
```

### IconButton

```java
new IconButton(Icons.SETTINGS, () -> openSettings());
new IconButton(Icons.FAVORITE).style(IconButton.FILLED);     // STANDARD, FILLED, TONAL, OUTLINED
new IconButton(Icons.SHARE).iconColor(Compat.theme.primary);
```

### Floating action button

```java
Fab fab = new Fab(Icons.ADD).onClick(() -> create());
new Fab(Icons.EDIT).size(Fab.SMALL);                   // SMALL, NORMAL, LARGE
Fab extended = new Fab(Icons.SEND, "Compose");         // extended FAB with a label

extended.shrink();   // animate to icon only (e.g. while a list scrolls down)
extended.extend();   // show the label again
fab.hide();          // scale out
fab.show();          // scale in
```

<div class="clear"></div>

## EditText

<img class="shot right" src="images/04_inputs_typing.png" alt="Text fields">

A Material text field with a floating label, outlined or filled.

```java
EditText name = new EditText("Name").setLeadingIcon(Icons.PERSON);

EditText email = new EditText("Email")
        .setStyle(EditText.FILLED)
        .setKeyboardType(Input.OnscreenKeyboardType.Email);

EditText password = new EditText("Password")
        .setPassword(true)                       // hides text + adds a show/hide toggle
        .setHelperText("At least 8 characters");

EditText amount = new EditText("Amount").setNumeric().setMaxLength(8);
amount.setError("Invalid number");               // red state; setError(null) clears it

name.setOnTextChanged((view, text) -> validate(text));
name.setOnSubmit(() -> next());                  // Enter / Done
```

Behaviour you get for free:

- tap to place the cursor, drag to select, double-tap to select all;
- long press shows a Cut / Copy / Paste / Select all menu (rename it through `EditText.MENU_LABELS`);
- keyboard shortcuts on desktop (Ctrl+A/C/X/V, arrows, Home/End);
- tapping outside the field hides the on-screen keyboard;
- Arabic input with correct shaping.

Other options: `setTrailingIcon(icon, onClick)`, `setFilter(c -> ...)`, `setContainerColor(c)` (when an outlined field sits on a colour other than `surface`), `requestFocus()`, `clearFocus()`, `selectAll()`, `copy()`, `cut()`, `paste()`.

<div class="clear"></div>

## Selection controls

```java
CheckBox remember = new CheckBox("Remember me", true);
Switch notifications = new Switch("Notifications");
notifications.setOnCheckedChange((button, checked) -> enable(checked));

RadioGroup size = new RadioGroup(LinearLayout.HORIZONTAL);
size.add(new RadioButton("Small"));
size.add(new RadioButton("Medium"));
size.add(new RadioButton("Large"));
size.check(1);
size.setOnCheckedChange((group, index) -> setSize(index));
```

`setChecked(value)` animates; `setChecked(value, false)` does not. A `Switch` with a label lays out like a settings row: label at the start, switch at the end.

## Slider

```java
Slider volume = new Slider(0, 100).setStep(5).setValue(40);
volume.setOnChange((slider, value, fromUser) -> setVolume(value));
```

A slider keeps horizontal drags to itself, so it works inside a `ViewPager`.

## Chip

```java
new Chip("Help").icon(Icons.HELP_OUTLINE).onClick(() -> help());     // action chip
Chip filter = new Chip("Games").checkable(true);                        // filter chip
filter.onSelected((chip, selected) -> refresh());
```

## Progress indicators

```java
ProgressBar loading = new ProgressBar();                  // linear, indeterminate
ProgressBar upload = new ProgressBar().setProgress(0.4f);  // determinate (animates to the value)
ProgressBar spinner = ProgressBar.circular();
spinner.setIndicatorSize(dp(48), dp(4));
```

Indeterminate indicators only request frames while they are visible.

## Images and icons

```java
ImageView photo = new ImageView(texture)
        .setScaleType(ImageView.CENTER_CROP)     // FIT_CENTER, CENTER_CROP, FIT_XY, CENTER
        .setCornerRadius(dp(12));                // anti-aliased, no stencil needed
ImageView avatar = new ImageView(region).setCornerRadius(dp(20));   // radius = half size → circle

IconView icon = new IconView(Icons.STAR, Compat.theme.primary).setIconSize(dp(32));
```

### Icons

The Material Icons font is bundled (about 2 200 icons). Common icons are constants; any other icon is available by name:

```java
char add = Icons.ADD;
char cart = Icons.get("shopping_cart");   // also accepts "ic_shopping_cart" or a hex code like "e8cc"
```

Browse names at [fonts.google.com/icons](https://fonts.google.com/icons?icon.set=Material+Icons).

### Small images in the shared atlas

Images packed into the library's atlas are drawn in the same batch as everything else, so a list full of avatars still costs a single draw call:

```java
TextureRegion avatar = Compat.gfx().pack(pixmap);   // the pixmap is not disposed for you
```

Keep these images small (a page is 1024 × 1024 by default); large photos are better as normal textures.

## Divider and Space

```java
column.add(new Divider()).matchWidth();
row.add(new Divider(true)).height(dp(24));      // vertical
row.add(new Space()).weight(1).width(0);         // pushes the next children to the end
```
