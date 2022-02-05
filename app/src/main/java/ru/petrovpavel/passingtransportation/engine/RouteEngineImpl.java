package ru.petrovpavel.passingtransportation.engine;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.mapbox.geojson.Point;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ru.petrovpavel.passingtransportation.data.MapPoint;
import ru.petrovpavel.passingtransportation.data.Route;
import ru.petrovpavel.passingtransportation.data.memory.RoutesHolder;
import ru.petrovpavel.passingtransportation.data.memory.VehicleHolder;

public class RouteEngineImpl implements RouteEngine {

    public static boolean temp = true;

    public Route findOptimalRoute(Activity activity, Location location, Point destination) {
        VehicleHolder vehicleHolder = VehicleHolder.getInstance();
        RoutesHolder routesHolder = RoutesHolder.getInstance();

        int availableCapacity = vehicleHolder.getAvailableCapacity();
        int delay = vehicleHolder.getDelay();
        List<Route> availableRoutes = routesHolder.getAvailableRoutes();

        Log.d("RouteEngineImpl", LocalDateTime.now().toString() +
                " availableCapacity = [" + availableCapacity +
                "] availableDelay = [" + delay +
                "] AvailableRoutes = [" + routesHolder.getAvailableRoutes() + "]");
        return calculate(routesHolder.getAvailableRoutes());
    }

    @Override
    public void buildNewRoute(Route optimalRoute, Location location) {

    }

    @Override
    public void rejectRoute(Route rejectedRoute) {

    }

    public Route calculate(List<Route> availableRoutes) {
        if (temp) {
            temp = false;
            return availableRoutes.get(0);
        } else {
            return null;
        }
    }

    private static Point[] convertToPointsArray(Route possibleRoute, Location originLocation, Point destination) {
        Point waypointOrigin = convertToPoint(possibleRoute.getOrigin());
        Point waypointDestination = convertToPoint(possibleRoute.getDestination());
        Point origin = Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());

        return new Point[]{origin, waypointOrigin, waypointDestination, destination};
    }

    private static Point convertToPoint(MapPoint point) {
        return Optional.ofNullable(point)
                .map(mapPoint -> Point.fromLngLat(mapPoint.getLongitude(),mapPoint.getLatitude()))
                .orElse(null);
    }

    /**TODO:
     * перенести подгрузку маршрутов, +
     * вынести в поля класса список подгруженных маршрутов и обновлять в листенере при изменении/обновлении +
     * добавить в активити заполнение ограничений и добавить в этот класс импортирование этих значений +
     * следовательно инициализировать класс в классе фрагменте +
     * при вызове findRoutes() проверять валидность ограничениям +
     *
     * на вкладке инициализации
     * 1 параметр - флаг вкл/выкл поиск маршрутов +
     * 2 параметр - объём текущей загрузки
     * 3 параметр - полная грузоподъёмность +
     * 4 параметр - максимально возможная задержка по времени (+ 10 мин, + 20 мин, + 30 мин ...) +
     *
     * оптимизация: проверить на удалённость каждого маршрута и запомнить насколько они не соответствуют требованиям
     * (пример: если маршрут увеличивается на 20 минут из 10 возможных, то проверить его через 5 минут)
     *
     * приоритизировать маршруты -> LinkedHashSet
     *
     * при применении маршрута : this.origin = currentLocation
     */

}
