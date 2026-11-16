package com.star4droid.compat.demo;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = false; // keep the system bars so adjustResize works with the keyboard
		config.useAccelerometer = false;
		config.useCompass = false;
		config.numSamples = 0; // shapes and text are already anti-aliased; MSAA would only cost fill rate
		initialize(new GalleryApp(), config);
	}
}
