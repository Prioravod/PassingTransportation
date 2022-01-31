package ru.petrovpavel.passingtransportation.engine;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import java.time.LocalDateTime;
import java.util.List;

import ru.petrovpavel.passingtransportation.data.Route;
import ru.petrovpavel.passingtransportation.data.memory.RoutesHolder;
import ru.petrovpavel.passingtransportation.data.memory.VehicleHolder;

public class RouteEngineImpl implements RouteEngine {

    public List<Route> findOptimalRoutes(Activity activity, Location location) {
        VehicleHolder vehicleHolder = VehicleHolder.getInstance();
        RoutesHolder routesHolder = RoutesHolder.getInstance();

        int availableCapacity = vehicleHolder.getAvailableCapacity();
        Log.d("RouteEngineImpl", LocalDateTime.now().toString() + " availableCapacity = [" + availableCapacity + "] AvailableRoutes = [" + routesHolder.getAvailableRoutes() + "]");
        return null;
    }

    /**TODO:
     * перенести подгрузку маршрутов,
     * вынести в поля класса список подгруженных маршрутов и обновлять в листенере при изменении/обновлении
     * добавить в активити заполнение ограничений и добавить в этот класс импортирование этих значений
     * следовательно инициализировать класс в классе фрагменте
     * при вызове findRoutes() проверять валидность ограничениям
     *
     * на вкладке инициализации
     * 1 параметр - флаг вкл/выкл поиск маршрутов
     * 2 параметр - объём текущей загрузки
     * 3 параметр - полная грузоподъёмность
     * 4 параметр - максимально возможная задержка по времени (+ 10 мин, + 20 мин, + 30 мин ...)
     *
     * оптимизация: проверить на удалённость каждого маршрута и запомнить насколько они не соответствуют требованиям
     * (пример: если маршрут увеличивается на 20 минут из 10 возможных, то проверить его через 5 минут)
     *
     * приоритизировать маршруты -> LinkedHashSet
     */

}
