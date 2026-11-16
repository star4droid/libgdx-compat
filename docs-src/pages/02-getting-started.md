---
title: Getting started
slug: getting-started
nav: Getting started
---
# Getting started

## Requirements

- libGDX 1.12.1 or newer (the library is compiled against 1.12.1 and tested on 1.14.2).
- The `gdx-freetype` extension, used to rasterise text on demand.
- Java 8 language level. Backends: Android and LWJGL3 desktop. GWT/HTML is not supported because FreeType is not available there.

## Installation

### Option 1: JitPack

Add the JitPack repository and the dependency to your **core** module:

```groovy
// settings.gradle or the root build.gradle
repositories {
    maven { url 'https://jitpack.io' }
}

// core/build.gradle
dependencies {
    api 'com.github.star4droid:libgdx-compat:1.0.0'
    api "com.badlogicgames.gdx:gdx-freetype:$gdxVersion"
}
```

The version is the name of a release tag in the GitHub repository.

### Option 2: the jar file

Download `libgdx-compat-1.0.0.jar` from the [releases page](https://github.com/star4droid/libgdx-compat/releases) (it is also in the `dist/` folder of the repository), put it in `core/libs/` and add:

```groovy
dependencies {
    api files('libs/libgdx-compat-1.0.0.jar')
    api "com.badlogicgames.gdx:gdx-freetype:$gdxVersion"
}
```

### Option 3: as a module

Copy the `compat` folder into your project, add `include 'compat'` to `settings.gradle`, and depend on it with `api project(':compat')`.

### FreeType natives

Whichever option you use, every platform needs the FreeType native libraries, exactly as for any libGDX project using fonts:

```groovy
// desktop/build.gradle
implementation "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop"

// android/build.gradle (inside the natives configuration)
natives "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi-v7a"
natives "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-arm64-v8a"
natives "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86_64"
```

The demo's [desktop](https://github.com/star4droid/libgdx-compat/blob/main/demo/desktop/build.gradle) and [android](https://github.com/star4droid/libgdx-compat/blob/main/demo/android/build.gradle) build files are complete working examples.

<div class="note"><strong>R8 / ProGuard:</strong> the rules the library needs are shipped inside the jar (<code>META-INF/proguard/libgdx-compat.pro</code>) and are applied automatically when <code>minifyEnabled</code> is on.</div>

## Your first screen

```java
public class MyGame extends ApplicationAdapter {
    private Stage stage;

    @Override
    public void create () {
        Compat.init();                        // once, before creating any widget
        stage = Compat.createStage();         // ScreenViewport + back-key handling
        Gdx.input.setInputProcessor(stage);

        LinearLayout root = LinearLayout.vertical()
                .padding(Compat.dp(16))
                .spacing(Compat.dp(12));
        root.setFillParent(true);
        root.setBackground(Compat.theme.surface);

        root.add(new TextView("Welcome", Typography.HEADLINE_SMALL));
        root.add(new EditText("Your name").setLeadingIcon(Icons.PERSON)).matchWidth();
        root.add(new Button("Continue").onClick(() -> Gdx.app.log("ui", "clicked")));

        stage.addActor(root);
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
}
```

## Units: dp and sp

The library works in real screen pixels with a `ScreenViewport`. Convert every size with:

| Method | Use for |
|---|---|
| `Compat.dp(16)` | sizes, margins, paddings (rounded to whole pixels) |
| `Compat.dpf(0.5f)` | the same without rounding |
| `Compat.sp(14)` | text sizes (also multiplied by `fontScale`) |

On Android and iOS the density comes from the device; on desktop it is 1. You can force it:

```java
Compat.Config config = new Compat.Config();
config.density = 2f;
Compat.init(config);
```

<div class="note">If your game already uses a viewport with a fixed virtual size (for example 360 × 640), set <code>config.density = stage.getWidth() / 360f</code> so that 1 dp maps to one unit of your design width.</div>

## Configuration

All fields of `Compat.Config` are optional:

| Field | Default | Meaning |
|---|---|---|
| `density` | `0` (automatic) | pixels per dp |
| `fontScale` | `1` | extra multiplier for text sizes |
| `regularFont`, `mediumFont` | IBM Plex Sans Arabic | any `.ttf`/`.otf` file |
| `iconFont` | Material Icons | icon font used by `Icons` |
| `atlasPageSize` | `1024` | size of the shared atlas page |
| `rtl` | `false` | mirror all layouts for right-to-left languages |
| `dark` | `false` | start with the dark theme |
| `catchBackKey` | `true` | catch Android BACK so overlays can handle it |

## Saving battery

Components ask for frames only while they animate, so a UI-heavy app can stop drawing when nothing changes:

```java
Gdx.graphics.setContinuousRendering(false);
```

Call `Compat.requestRender()` yourself when your own code changes something on screen outside an input event.

## The back key

`Compat.createStage()` (or `Compat.attach(stage)` for an existing stage) routes Android BACK and desktop ESC to the top-most open component: a dialog, a bottom sheet, the drawer, then the `Navigator`. When nothing consumes it the app exits on Android; set `Compat.exitOnBack = false` to keep it running.
