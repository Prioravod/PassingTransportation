package ru.petrovpavel.passingtransportation.engine;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.mapbox.geojson.Point;

import ru.petrovpavel.passingtransportation.data.Route;

public interface RouteEngine {

    Route findOptimalRoute(Activity activity, Location location, Point destination);

    //TODO:change signature
    void buildNewRoute(Route optimalRoute, Location location);

    void rejectRoute(Route rejectedRoute);
}