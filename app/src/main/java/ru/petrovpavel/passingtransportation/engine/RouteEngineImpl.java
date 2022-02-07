package ru.petrovpavel.passingtransportation.engine;

import android.app.Activity;
import android.content.res.Resources;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.util.CollectionUtils;
import com.mapbox.geojson.Point;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.petrovpavel.passingtransportation.BuildConfig;
import ru.petrovpavel.passingtransportation.R;
import ru.petrovpavel.passingtransportation.data.MapPoint;
import ru.petrovpavel.passingtransportation.data.Route;
import ru.petrovpavel.passingtransportation.data.RouteWrapper;
import ru.petrovpavel.passingtransportation.data.memory.RoutesHolder;
import ru.petrovpavel.passingtransportation.data.memory.VehicleHolder;

@RequiredArgsConstructor
public class RouteEngineImpl implements RouteEngine {

    public static boolean temp = true;
    public static final String TAG = "RouteEngineImpl";
    private final Activity activity;

    public Route findOptimalRoute(Activity activity, Location location, Point destination) {
        VehicleHolder vehicleHolder = VehicleHolder.getInstance();
        RoutesHolder routesHolder = RoutesHolder.getInstance();

        int availableCapacity = vehicleHolder.getAvailableCapacity();
        int delay = vehicleHolder.getDelay();
        List<RouteWrapper> availableRoutes = routesHolder.getAvailableRoutes();

        if (CollectionUtils.isEmpty(availableRoutes)) {
            return null;
        }

        Log.d(TAG, LocalDateTime.now().toString() +
                " availableCapacity = [" + availableCapacity +
                "] availableDelay = [" + delay +
                "] availableRoutes = [" + availableRoutes + "]");

        return calculate(availableRoutes);
    }

    public Route calculate(List<RouteWrapper> availableRoutes) {
        if (temp) {
            temp = false;
            return availableRoutes.get(0).getRoute();
        } else {
            return null;
        }
    }

    public void api(ArrayList<Position> waypoints) {
        try {
            MapboxDirections client = new MapboxDirections.Builder()
                    .setCoordinates(waypoints)
                    .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                    .setSteps(false)
                    .setAccessToken(BuildConfig.MAPBOX_TOKEN)
                    .build();
            client.enqueueCall(new Callback<DirectionsResponse>() {

                @Override
                public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                    DirectionsRoute currentRoute = response.body().getRoutes().get(0);
                    final double duration = currentRoute.getDuration();

                }

                @Override
                public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    Toast.makeText(activity, Resources.getSystem().getString(R.string.error) + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ServicesException exception) {
            Toast.makeText(activity, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
        }

    }

    //for finding route
    @Override
    public ArrayList<Position> getEstimatedWaypointPositions(Route possibleRoute, Location originLocation, Point destination) {
        Position waypointOrigin = convertToPosition(possibleRoute.getOrigin());
        Position waypointDestination = convertToPosition(possibleRoute.getDestination());
        Position originPosition = convertToPosition(originLocation);
        Position destinationPosition = convertToPosition(destination);

        return new ArrayList<>(Arrays.asList(destinationPosition, waypointDestination, waypointOrigin, originPosition));
    }

    private static Position convertToPosition(MapPoint point) {
        return Optional.ofNullable(point)
                .map(mapPoint -> Position.fromCoordinates(mapPoint.getLongitude(),mapPoint.getLatitude()))
                .orElse(null);
    }

    private static Position convertToPosition(Location location) {
        return Position.fromCoordinates(location.getLongitude(), location.getLatitude());
    }

    private static Position convertToPosition(Point destination) {
        return Position.fromCoordinates(destination.longitude(), destination.latitude());
    }

    //for navigation start
    @Override
    public ArrayList<Point> getEstimatedWaypoints(Route possibleRoute, Location originLocation, Point destination) {
        Point waypointOrigin = convertToPoint(possibleRoute.getOrigin());
        Point waypointDestination = convertToPoint(possibleRoute.getDestination());
        Point originPosition = convertToPoint(originLocation);

        return new ArrayList<>(Arrays.asList(destination, waypointDestination, waypointOrigin, originPosition));
    }

    private static Point convertToPoint(MapPoint point) {
        return Optional.ofNullable(point)
                .map(mapPoint -> Point.fromLngLat(mapPoint.getLongitude(),mapPoint.getLatitude()))
                .orElse(null);
    }

    private static Point convertToPoint(Location location) {
        return Point.fromLngLat(location.getLongitude(), location.getLatitude());
    }


    /**
     * Response{protocol=http/1.1, code=200, message=OK, url=https://api.mapbox.com/directions/v5/mapbox/driving/45.978633,51.529302;45.998919,51.539317?access_token=pk.eyJ1IjoicGV0cm92cHMiLCJhIjoiY2t6MGlvdzBtMGtydTJ2bzBtdndzbW5mbyJ9.wQw0UGWlfGgai-c0dHTzTA&geometries=polyline&steps=true}
     * Response{protocol=http/1.1, code=200, message=OK, url=https://api.mapbox.com/directions/v5/mapbox/driving/45.978632,51.529302;45.998918,51.539316?access_token=pk.eyJ1IjoicGV0cm92cHMiLCJhIjoiY2t6MGlvdzBtMGtydTJ2bzBtdndzbW5mbyJ9.wQw0UGWlfGgai-c0dHTzTA&alternatives=true&geometries=polyline6&overview=full&steps=true&bearings=%3B&continue_straight=true&annotations=congestion%2Cdistance&language=en&roundabout_exits=true&voice_instructions=true&banner_instructions=true&voice_units=imperial&enable_refresh=true}
     *
     */

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
