/*
 * Copyright (c) 2021 Petrov Pavel.
 */

package ru.petrovpavel.passingtransportation;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.mapbox.mapboxsdk.Mapbox;

public class MotoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Mapbox.getInstance(this, BuildConfig.MAPBOX_TOKEN);
        Stetho.initializeWithDefaults(this);

    }
}