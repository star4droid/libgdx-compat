---
title: Overview
slug: index
nav: Overview
---
<div class="hero">
<div class="hero-text">
<h1>libgdx-compat</h1>
<p class="lead">AppCompat-style Material 3 UI components for libGDX. Bottom sheets, FABs, lists, grids, view pagers, dialogs and drawers that behave like their Android counterparts, plus Android-style XML layouts.</p>
<p class="hero-actions"><a class="btn primary" href="getting-started.html">Get started</a> <a class="btn" href="https://github.com/star4droid/libgdx-compat">View on GitHub</a></p>
</div>
<div class="hero-shots">
<img src="images/01_widgets.png" alt="Widgets page of the gallery app">
<img src="images/14_sheet_expanded.png" alt="Bottom sheet over a list">
</div>
</div>

## Why

Scene2D gives you the building blocks, but making a bottom sheet that follows the finger, a recycled list of 100 000 rows, or a ViewPager with a tab indicator takes a lot of code. libgdx-compat packages those components with the touch behaviour, animations and sizing rules people expect from Android apps.

- **Built on Scene2D.** Every component is a normal `Actor`, so it drops into any existing `Stage`.
- **One module, plain Java 8.** Android, desktop (LWJGL3) and Linux. Tested with libGDX 1.12.1 and 1.14.2.
- **No skin, no images, no custom shader.** Shapes, shadows and ripples are drawn from code.
- **Fast.** A typical screen renders in 1–4 draw calls; lists only create the rows that are visible.
- **Arabic and RTL.** Letter shaping, bidirectional text and fully mirrored layouts.
- **XML layouts** with the same tag and attribute names as Android.

## What's inside

| Area | Components |
|---|---|
| [Layouts](layouts.html) | `LinearLayout`, `FrameLayout`, `FlowLayout`, `ScrollView`, `CardView` |
| [Widgets](widgets.html) | `TextView`, `Button`, `IconButton`, `Fab`, `EditText`, `CheckBox`, `RadioButton`, `Switch`, `Slider`, `Chip`, `ProgressBar`, `ImageView`, `IconView` |
| [Lists & pages](lists.html) | `RecyclerView` (list and grid), pull-to-refresh, `ViewPager`, `TabLayout` |
| [Overlays](overlays.html) | `BottomSheet`, `AlertDialog`, `PopupMenu`, `Snackbar` |
| [Navigation](navigation.html) | `Toolbar`, `BottomNavigationView`, `DrawerLayout`, `NavigationView`, `Navigator` |
| [XML layouts](xml.html) | `LayoutInflater` with Android tag names, `onClick`, `@string`, `@color`, `<include>` |
| [Theming](theming.html) | Material 3 colour roles, seed colours, dark mode, type scale, Arabic / RTL |

## A first look

```java
LinearLayout root = LinearLayout.vertical().padding(Compat.dp(16)).spacing(Compat.dp(12));
root.setFillParent(true);
root.setBackground(Compat.theme.surface);

root.add(new TextView("Hello", Typography.HEADLINE_SMALL));
root.add(new EditText("Name").setLeadingIcon(Icons.PERSON)).matchWidth();
root.add(new Button("Save").icon(Icons.SAVE).onClick(() ->
        Snackbar.make(stage, "Saved", Snackbar.LENGTH_SHORT).show()));

stage.addActor(root);
```

The same screen as XML:

```xml
<LinearLayout android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="vertical" android:padding="16dp" android:background="@color/surface">
    <TextView android:text="Hello" android:textAppearance="headlineSmall" />
    <EditText android:layout_width="match_parent" android:hint="Name" app:startIcon="person" />
    <Button android:text="Save" app:icon="save" android:onClick="onSave" />
</LinearLayout>
```

## Gallery

<div class="gallery">
<figure><img src="images/04_inputs_typing.png" alt=""><figcaption>Text fields and selection controls</figcaption></figure>
<figure><img src="images/05_list.png" alt=""><figcaption>RecyclerView with 100 000 rows</figcaption></figure>
<figure><img src="images/10_grid.png" alt=""><figcaption>Grid with images from the shared atlas</figcaption></figure>
<figure><img src="images/16_dialog.png" alt=""><figcaption>AlertDialog</figcaption></figure>
<figure><img src="images/17_drawer.png" alt=""><figcaption>Navigation drawer</figcaption></figure>
<figure><img src="images/18_dark.png" alt=""><figcaption>Dark theme</figcaption></figure>
<figure><img src="images/11_xml.png" alt=""><figcaption>Screen inflated from XML</figcaption></figure>
<figure><img src="images/rtl_04_drawer.png" alt=""><figcaption>Right-to-left mode</figcaption></figure>
</div>

Try it yourself with the gallery app: see [Demo & testing](demo.html).
