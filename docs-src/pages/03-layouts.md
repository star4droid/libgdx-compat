---
title: Layouts
slug: layouts
nav: Layouts
---
# Layouts

Layouts are `ViewGroup`s: Scene2D groups that position their children from per-child **layout params**, like Android. They never use a transform matrix, so nesting them costs nothing at draw time.

## Layout params

`add(child)` returns the child's `LayoutParams`, so options chain on the same line:

```java
row.add(title).weight(1).width(0).margins(dp(16), 0, dp(8), 0);
frame.add(fab).gravity(Gravity.BOTTOM | Gravity.END).margin(dp(16));
column.add(divider).matchWidth().height(dp(1));
```

| Method | Meaning |
|---|---|
| `width(px)`, `height(px)`, `size(w, h)` | fixed size in pixels |
| `matchWidth()`, `matchHeight()`, `match()` | `MATCH_PARENT` |
| `wrap()` | `WRAP_CONTENT` (the default) |
| `weight(w)` | share of the free space (`LinearLayout`) |
| `gravity(g)` | position inside the cell (`Gravity.CENTER`, `END`, `BOTTOM` …) |
| `margin(all)`, `margin(h, v)`, `margins(l, t, r, b)`, `marginTop(v)` … | outer spacing |

In RTL mode left/right margins and `START`/`END` gravities are mirrored automatically.

## LinearLayout

Arranges children in a row or a column.

```java
LinearLayout row = LinearLayout.horizontal()
        .spacing(dp(8))
        .gravity(Gravity.CENTER_VERTICAL);
row.add(new IconView(Icons.PERSON));
row.add(new TextView("Profile")).weight(1).width(0);   // takes the remaining width
row.add(new IconButton(Icons.CHEVRON_RIGHT));
```

- `weight` distributes free space; give the child `width(0)` (or `height(0)` in a column) so it gets only its share.
- A `wrap_content` text in a row shrinks to leave room for the children after it, so trailing icons never get pushed off screen.
- `setGravity()` packs the children when they do not fill the layout.

## FrameLayout

Stacks children; each one is placed with its own gravity. Use it to float a FAB over content:

```java
FrameLayout frame = new FrameLayout();
frame.add(content).match();
frame.add(new Fab(Icons.ADD)).gravity(Gravity.BOTTOM | Gravity.END).margin(dp(16));
```

## FlowLayout

A row that wraps onto new lines, for chip groups and tags:

```java
FlowLayout chips = new FlowLayout();
chips.setSpacing(dp(8), dp(8));
for (String tag : tags) chips.add(new Chip(tag).checkable(true));
```

## ScrollView

Scrolls one child vertically (default) or horizontally, with fling, a fading scroll bar, mouse-wheel support and culling of off-screen children:

```java
LinearLayout content = LinearLayout.vertical().padding(dp(16)).spacing(dp(12));
// ... add many children ...
ScrollView scroll = new ScrollView(content);
scroll.lp(content).matchWidth();

ScrollView carousel = new ScrollView(false);   // horizontal
```

Useful methods (shared by all scrolling components): `setScroll`, `scrollBy`, `smoothScrollTo`, `fling`, `canScroll(direction)`, `setOnScroll(listener)`, `setScrollbarEnabled`, and pull-to-refresh with `setOnRefresh` / `setRefreshing`.

## CardView

A surface for grouped content. Three styles:

```java
CardView card = new CardView(CardView.OUTLINED);   // ELEVATED (default), FILLED, OUTLINED
card.setPadding(dp(16));
card.add(new TextView("Card title", Typography.TITLE_MEDIUM));
card.setOnClick(() -> open());                      // ripple included
```

`CardView` is a `FrameLayout`; put a `LinearLayout` inside for several children.

## Styling any ViewGroup

Every layout can draw a background and react to taps:

```java
LinearLayout banner = LinearLayout.horizontal().padding(dp(16));
banner.setBackground(Compat.theme.primaryContainer);  // pass theme colours to follow theme changes
banner.setCornerRadius(dp(16));                      // or setCornerRadii(tl, tr, br, bl)
banner.setStroke(Compat.theme.outline, dp(1));
banner.setElevation(dp(3));                          // soft shadow
banner.setOnClick(() -> showDetails());              // ripple clipped to the rounded shape
banner.setOnLongClick(() -> showMenu());
banner.setClip(true);                                // clip children (costs a draw call)
```

## Finding views

Give views an id with `.id("name")` (or `android:id` in XML) and look them up like `findViewById`:

```java
EditText name = Views.find(root, "name");
```
