package ru.petrovpavel.passingtransportation.data.memory;

import java.util.LinkedList;
import java.util.List;

import ru.petrovpavel.passingtransportation.data.Route;

public class RoutesHolder {

    private final List<Route> availableRoutes = new LinkedList<>();

    public List<Route> getAvailableRoutes() {
        return availableRoutes;
    }

    public void addAvailableRoute(Route newRoute) {
        availableRoutes.add(newRoute);
    }

    public void addAvailableRouteIfAbsent(Route newRoute) {
        if (!availableRoutes.contains(newRoute)) {
            availableRoutes.add(newRoute);
        }
    }

    public void removeAvailableRoute(Route newRoute) {
        availableRoutes.remove(newRoute);
    }

    public void flushAvailableRoutes() {
        availableRoutes.clear();
    }

    private static final RoutesHolder holder = new RoutesHolder();

    public static RoutesHolder getInstance() {
        return holder;
    }
}
