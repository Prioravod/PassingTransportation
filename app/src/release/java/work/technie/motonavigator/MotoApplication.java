package ru.petrovpavel.passingtransportation;

import android.app.Application;

import com.mapbox.mapboxsdk.MapboxAccountManager;

public class MotoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MapboxAccountManager.start(this, getString(R.string.mapbox_token));

    }
}