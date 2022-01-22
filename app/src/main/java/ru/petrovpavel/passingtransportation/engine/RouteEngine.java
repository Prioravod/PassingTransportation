package ru.petrovpavel.passingtransportation.engine;

import android.app.Activity;
import android.location.Location;

import java.util.List;

import ru.petrovpavel.passingtransportation.data.Route;

public interface RouteEngine {

    void findRoutes(Activity activity, List<Route> availableRoutes, Location location);
}