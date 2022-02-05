package ru.petrovpavel.passingtransportation.engine;

import android.app.Activity;
import android.location.Location;

import com.mapbox.geojson.Point;

import ru.petrovpavel.passingtransportation.data.Route;

public class RouteEngineProxy implements RouteEngine {

    private byte tickCounter;
    private static final byte TICK_MAX = 15;
    private final RouteEngine engine;

    public RouteEngineProxy() {
        this.engine = new RouteEngineImpl();
        this.tickCounter = 0;
    }

    public Route findOptimalRoute(Activity activity, Location location, Point destination) {
        if (tickCounter == TICK_MAX) {
            tickCounter = 0;
            return engine.findOptimalRoute(activity, location, destination);
        } else {
            tickCounter++;
        }
        return null;
    }

    @Override
    public void buildNewRoute(Route optimalRoute, Location location) {

    }

    @Override
    public void rejectRoute(Route rejectedRoute) {

    }
}
