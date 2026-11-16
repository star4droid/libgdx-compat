# libgdx-compat calls PixmapPacker.pack reflectively to stay compatible with several libGDX versions.
-keep class com.badlogic.gdx.graphics.g2d.PixmapPacker { *; }
-keep class com.badlogic.gdx.graphics.g2d.PixmapPacker$* { *; }
# onClick="method" handlers referenced from XML layouts are found by name.
-keepclassmembers class * { public void *(com.badlogic.gdx.scenes.scene2d.Actor); }
