---
title: Navigation
slug: navigation
nav: Navigation
---
# Navigation

## Toolbar

The top app bar: navigation icon, title and action icons.

```java
Toolbar toolbar = new Toolbar("Inbox");
toolbar.setNavigationIcon(Icons.MENU, () -> drawer.open());
toolbar.addAction(Icons.SEARCH, () -> search());
IconButton more = toolbar.addAction(Icons.MORE_VERT, null);
more.setOnClick(() -> new PopupMenu().add("Settings", () -> settings()).show(more));
```

- `setCentered(true)` centres the title.
- `setLifted(true)` tints the bar while content scrolls underneath it; connect it to a list with `list.setOnScroll((v, scroll, d) -> toolbar.setLifted(scroll > 0))`.
- `setColors(background, liftedBackground, contentColor)` overrides the colours.

## BottomNavigationView

```java
BottomNavigationView bottomNav = new BottomNavigationView();
bottomNav.addItem(Icons.HOME, "Home")
         .addItem(Icons.SEARCH, "Search")
         .addItem(Icons.PERSON, "Profile");
bottomNav.setSelectedIcon(0, Icons.HOME);        // optional filled icon for the active item
bottomNav.setBadge(1, 12);                        // -1 = none, 0 = dot, n = number
bottomNav.setOnItemSelected(index -> pager.setCurrentItem(index));
```

`setSelectedItem(index)` changes the selection without calling the listener (useful when a pager drives it).

## DrawerLayout and NavigationView

<img class="shot right" src="images/17_drawer.png" alt="Navigation drawer">

A drawer that slides in from the start edge. Swipe from the edge to open; tap the dimmed area, swipe back or press back to close.

```java
NavigationView nav = new NavigationView();
nav.addHeader(new TextView("My app", Typography.TITLE_LARGE));
nav.addItem(Icons.HOME, "Home", () -> showHome())
   .addItem(Icons.FAVORITE, "Favourites", () -> showFavourites())
   .addDivider()
   .addSectionTitle("Other")
   .addItem(Icons.SETTINGS, "Settings", () -> showSettings());
nav.setCheckedItem(0);

DrawerLayout drawer = new DrawerLayout();
drawer.setContent(mainScreen);
drawer.setDrawer(nav);
drawer.setFillParent(true);
stage.addActor(drawer);
```

`open()`, `close()`, `toggle()`, `isOpen()`, `setSwipeEnabled(false)`, `setDrawerListener(fraction -> ...)`.

<div class="clear"></div>

## Navigator: a stack of screens

`Navigator` keeps a back stack of screens with slide transitions. The back key pops the top screen.

```java
public class HomeScreen extends Navigator.Screen {
    @Override protected Actor onCreateView () {
        LinearLayout root = LinearLayout.vertical();
        root.setBackground(Compat.theme.surface);
        root.add(new Toolbar("Home")).matchWidth();
        root.add(new Button("Open details").onClick(() ->
                navigator.push(new DetailScreen(42))));
        return root;
    }
}

Navigator navigator = new Navigator();
navigator.setFillParent(true);
stage.addActor(navigator);
navigator.push(new HomeScreen());
```

| Method | Meaning |
|---|---|
| `push(screen)` | show a new screen on top |
| `pop()` | go back; returns `false` when only one screen is left |
| `replace(screen)` | swap the top screen |
| `popToRoot()` | back to the first screen |
| `current()`, `size()` | inspect the stack |

Lifecycle callbacks on `Screen`: `onShow()`, `onHide()`, `onDestroy()`, and `onBackPressed()` (return `true` to handle back yourself, e.g. to confirm leaving a form). Inside a screen the `navigator` field gives access to the stack.

## How the back key is routed

On every BACK / ESC press, the library walks the stage from the top-most actor down and asks each `BackHandler` in turn:

1. an open dialog, popup menu or modal bottom sheet closes;
2. an open drawer closes;
3. the `Navigator` pops a screen;
4. if nothing handled it, the app exits (Android only, unless `Compat.exitOnBack = false`).

Implement `BackHandler` on your own actor to join this chain.
