package ru.petrovpavel.passingtransportation.engine;

import android.app.Activity;
import android.location.Location;

import java.util.List;

import ru.petrovpavel.passingtransportation.data.Route;

public class RouteEngineProxy implements RouteEngine {

    private byte tickCounter;
    private static final byte TICK_MAX = 15;
    private RouteEngine engine;

    public RouteEngineProxy() {
        this.engine = new RouteEngineImpl();
        this.tickCounter = 0;
    }

    public List<Route> findOptimalRoutes(Activity activity, List<Route> availableRoutes, Location location) {
        if (tickCounter == TICK_MAX) {
            tickCounter = 0;
            return engine.findOptimalRoutes(activity, availableRoutes, location);
        } else {
            tickCounter++;
        }
        return null;
    }
}
