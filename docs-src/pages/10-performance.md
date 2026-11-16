---
title: Performance
slug: performance
nav: Performance
---
# Performance

libgdx-compat is meant to sit next to a game, so it tries hard to stay out of the frame budget.

## How it stays light

- **One atlas for everything.** Rounded corners, outlines, shadows, text glyphs and icons live in a single texture page, so the batch rarely flushes. A whole screen usually renders in 1–4 draw calls.
- **No shader, no stencil.** Rounded shapes are built from small pre-rendered corner tiles; ripples are scaled copies of the widget's own shape, so they are clipped without scissors.
- **Flat scene graph.** Leaf widgets draw themselves instead of nesting labels and images, and layouts never use transform matrices.
- **Recycling.** `RecyclerView` creates the visible rows only, reuses them while scrolling, and a row change never re-lays out the rest of the screen.
- **Cached text.** Text is measured, wrapped and shaped once, then drawn from a `BitmapFontCache`.
- **Lazy glyphs.** Only the characters and sizes you use are rasterised.
- **Frames on demand.** Components request rendering only while they animate, so `setContinuousRendering(false)` works out of the box.

## Measurements

From the automated tests of the gallery app (desktop, LWJGL3; window 412 × 892 at density 1, and 1080 × 2340 at density 2.625):

| Scenario | Draw calls | Allocation per frame |
|---|---|---|
| Static screen | 3 | about 60 bytes |
| Fling through a 100 000-row list | 3 | about 0.6 KB (text of rows coming into view) |
| Fling through a 3-column image grid | 4–5 | about 0.5–1 KB |
| 3 s idle with continuous rendering off | 0 frames rendered | – |

These numbers come from a desktop GPU; frame times on phones depend on the device, but the draw-call count and allocation rate are the same.

## Tips

- Pack small repeated images (avatars, custom icons) with `Compat.gfx().pack(pixmap)` so they share the atlas.
- Every scrolling container, and every `ViewGroup` with `setClip(true)`, adds one or two draw calls for its clip region. Avoid clipping when content already fits.
- Give `RecyclerView` rows an exact size through `Adapter.getItemSize` when you know it; it skips measuring.
- Build heavy `ViewPager` pages lazily with `addPage(index -> ...)`.
- Reuse `Color` objects in rows (as the demo's `ContactRow` does) instead of allocating new ones in `bind`.
- The atlas page is 1024 × 1024 RGBA (4 MB of GPU memory); a new page is added automatically if it fills up. Raise or lower it with `Config.atlasPageSize`.

## Known limitations

- `EditText` is single-line. In text that mixes Arabic and Latin the cursor position is approximate.
- Arabic diacritics are positioned approximately (libGDX has no access to the font's GPOS table).
- No animations when items are inserted into or removed from a `RecyclerView`.
- Colour emoji are not supported.
- GWT / HTML5 is not supported because it has no FreeType.
