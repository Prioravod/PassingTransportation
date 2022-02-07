package ru.petrovpavel.passingtransportation.engine;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.mapbox.geojson.Point;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;

import ru.petrovpavel.passingtransportation.data.Route;

public interface RouteEngine {

    Route findOptimalRoute(Activity activity, Location location, Point destination);

    ArrayList<Position> getEstimatedWaypointPositions(Route possibleRoute, Location originLocation, Point destination);

    ArrayList<Point> getEstimatedWaypoints(Route possibleRoute, Location originLocation, Point destination);
}