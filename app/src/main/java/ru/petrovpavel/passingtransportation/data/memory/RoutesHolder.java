package ru.petrovpavel.passingtransportation.data.memory;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import ru.petrovpavel.passingtransportation.data.Route;
import ru.petrovpavel.passingtransportation.data.RouteStatus;
import ru.petrovpavel.passingtransportation.data.RouteWrapper;

public class RoutesHolder {

    private final List<RouteWrapper> availableRoutes = new LinkedList<>();

    public List<RouteWrapper> getAvailableRoutes() {
        return availableRoutes.stream()
                .filter(routeWrapper -> RouteStatus.NEW.equals(routeWrapper.getStatus()))
                .collect(Collectors.toList());
    }

    public RouteWrapper getCurrentRoute() {
        return availableRoutes.stream()
                .filter(routeWrapper -> RouteStatus.CONFIRMED.equals(routeWrapper.getStatus()))
                .findFirst()
                .orElse(null);
    }

    public void finishTrip() {
        availableRoutes.stream()
                .filter(routeWrapper -> RouteStatus.CONFIRMED.equals(routeWrapper.getStatus()))
                .findFirst()
                .ifPresent(routeWrapper -> routeWrapper.setStatus(RouteStatus.PASSED));
    }

    public void addAvailableRoute(Route newRoute) {
        availableRoutes.add(new RouteWrapper(newRoute, RouteStatus.NEW));
    }

    public void removeAvailableRoute(Route deletedRoute) {
        updateStatus(deletedRoute, RouteStatus.DELETED);
    }

    public void flushAvailableRoutes() {
        availableRoutes.clear();
    }

    public void updateStatus(Route route, RouteStatus status) {
        availableRoutes.stream()
                .filter(routeWrapper ->
                        routeWrapper.getRoute() != null
                        && route != null
                        && route.equals(routeWrapper.getRoute()))
                .findFirst()
                .ifPresent(routeWrapper -> routeWrapper.setStatus(status));
    }

    private static final RoutesHolder holder = new RoutesHolder();

    public static RoutesHolder getInstance() {
        return holder;
    }
}
