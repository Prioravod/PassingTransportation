/*
 * Copyright (c) 2021 Petrov Pavel.
 */

package ru.petrovpavel.passingtransportation;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.mapbox.mapboxsdk.MapboxAccountManager;

public class MotoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MapboxAccountManager.start(this, getString(R.string.PUBLIC_TOKEN));
        Stetho.initializeWithDefaults(this);

    }
}