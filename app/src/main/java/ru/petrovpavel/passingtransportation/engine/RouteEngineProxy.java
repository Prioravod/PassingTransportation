package ru.petrovpavel.passingtransportation.engine;

import android.app.Activity;
import android.location.Location;

import com.mapbox.geojson.Point;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;

import ru.petrovpavel.passingtransportation.data.Route;

public class RouteEngineProxy implements RouteEngine {

    private byte tickCounter;
    private static final byte TICK_MAX = 15;
    private final RouteEngine engine;

    public RouteEngineProxy(Activity activity) {
        this.engine = new RouteEngineImpl(activity);
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
    public ArrayList<Position> getEstimatedWaypointPositions(Route possibleRoute, Location originLocation, Point destination) {
        return engine.getEstimatedWaypointPositions(possibleRoute, originLocation, destination);
    }

    @Override
    public ArrayList<Point> getEstimatedWaypoints(Route possibleRoute, Location originLocation, Point destination) {
        return engine.getEstimatedWaypoints(possibleRoute, originLocation, destination);
    }
}
