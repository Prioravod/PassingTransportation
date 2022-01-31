package ru.petrovpavel.passingtransportation.engine;

import android.app.Activity;
import android.location.Location;

import java.util.List;

import ru.petrovpavel.passingtransportation.data.Route;

public interface RouteEngine {

    List<Route> findOptimalRoutes(Activity activity, Location location);
}