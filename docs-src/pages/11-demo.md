---
title: Demo & testing
slug: demo
nav: Demo & testing
---
# Demo app & testing

The repository contains a gallery app that shows every component: widgets, inputs, a 100 000-row list, an image grid, an XML screen, a bottom sheet, dialogs, a drawer, theme switching and a screen stack.

## Install the APK

Download `libgdx-compat-demo.apk` from the [latest release](https://github.com/star4droid/libgdx-compat/releases/latest) and open it on an Android phone (Android 5.0 or newer). It is a debug build, so Android will ask you to allow installing apps from that source.

## Run from source

```bash
git clone https://github.com/star4droid/libgdx-compat.git
cd libgdx-compat

# desktop gallery
./gradlew :demo:desktop:run
./gradlew :demo:desktop:run --args="--rtl"          # right-to-left mode
./gradlew :demo:desktop:run --args="--density 2"    # simulate a phone density

# Android APK (needs an Android SDK: create local.properties with sdk.dir=/path/to/sdk)
./gradlew :demo:android:assembleDebug
```

The gallery code is a good reference for real-world usage:

- [`GalleryApp.java`](https://github.com/star4droid/libgdx-compat/blob/main/demo/core/src/main/java/com/star4droid/compat/demo/GalleryApp.java): toolbar, tabs, pager, bottom navigation, drawer, FAB, navigator;
- [`Pages.java`](https://github.com/star4droid/libgdx-compat/blob/main/demo/core/src/main/java/com/star4droid/compat/demo/Pages.java): each page, list adapters, the bottom sheet content, XML click handlers;
- [`demo.xml`](https://github.com/star4droid/libgdx-compat/blob/main/demo/assets/layouts/demo.xml): the XML screen.

## Automated tests

`run-test.sh` starts the desktop gallery off-screen, drives it with scripted touches, key presses and typing, checks the results and saves screenshots to `shots/`. It needs `xvfb` on Linux.

```bash
./gradlew :demo:desktop:writeCp
./run-test.sh tour                     # every component, about 15 behaviour checks
./run-test.sh inputs                   # text editing, selection, menus, toggles
./run-test.sh rtl --rtl --out shots/rtl
./run-test.sh idle                     # frames rendered while idle
./run-test.sh tour --density 2.625 --size 1080x2340 --out shots/phone
```

Lines starting with `ASSERT` report checks, `PERF` lines report draw calls, frame time and allocation per frame. The scripts live in [`Scripts.java`](https://github.com/star4droid/libgdx-compat/blob/main/demo/desktop/src/main/java/com/star4droid/compat/demo/Scripts.java).

## Building the library

```bash
./gradlew :compat:jar          # compat/build/libs/compat-1.0.0.jar
./gradlew :compat:publishToMavenLocal
```

## Releasing a new version

Releases are made by the **Release** workflow, which only runs when started by hand:
**Actions → Release → Run workflow**, then enter the version (for example `1.0.1`), optional notes, and whether it is a pre-release.

The workflow builds the library jar, the sources jar and the demo APK, writes release notes (your text, the commits since the previous tag, download table, JitPack snippet), creates the tag and publishes the release with the files attached. JitPack picks the new tag up automatically.

The demo APK is signed with `demo/android/demo-release.jks`, a key committed to the repository on purpose, so each new APK installs over the previous one. It only signs the gallery app; never reuse it for a real app.

## Third-party assets

| File | License |
|---|---|
| IBM Plex Sans Arabic (Regular, Medium) | SIL Open Font License 1.1 |
| Material Icons font and code points | Apache License 2.0 |
