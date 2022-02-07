package ru.petrovpavel.passingtransportation.callback;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.common.util.CollectionUtils;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.petrovpavel.passingtransportation.BuildConfig;
import ru.petrovpavel.passingtransportation.R;
import ru.petrovpavel.passingtransportation.data.Route;
import ru.petrovpavel.passingtransportation.data.RouteStatus;
import ru.petrovpavel.passingtransportation.data.RouteWrapper;
import ru.petrovpavel.passingtransportation.data.memory.RoutesHolder;
import ru.petrovpavel.passingtransportation.engine.RouteEngineProxy;
import ru.petrovpavel.passingtransportation.interfaces.NavigationStatusListener;

public class NavigationReadyCallback implements OnNavigationReadyCallback {

    private static final String WAS_NAVIGATION_STOPPED = "was_navigation_stopped";
    private static final String WAS_IN_TUNNEL = "was_in_tunnel";
    private static List<Point> WAYPOINTS;

    private final Activity activity;
    private final NavigationView navigationView;
    private final NavigationStatusListener listener;
    private final Point origin;
    private final Point destination;
    private final RouteEngineProxy routeEngineProxy;
    private static Boolean IS_POPUP_SHOWED = false;

    public NavigationReadyCallback(Activity activity, NavigationView navigationView, NavigationStatusListener listener, Point origin, Point destination) {
        this.activity = activity;
        this.navigationView = navigationView;
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.routeEngineProxy = new RouteEngineProxy(activity);//TODO: init on routing running
    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        synchronized (navigationView) {
            updateNightMode();
            fetchRoute();
        }
    }

    private void updateNightMode() {
        if (wasNavigationStopped()) {
            updateWasNavigationStopped(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            activity.recreate();
        }
    }

    private void fetchRoute() {
        NavigationRoute.Builder builder = NavigationRoute.builder(activity)
                .accessToken(BuildConfig.MAPBOX_TOKEN)
                .profile(DirectionsCriteria.PROFILE_DRIVING);

        if (CollectionUtils.isEmpty(WAYPOINTS)) {
            builder.origin(origin)
                    .destination(destination)
                    .alternatives(true);
        } else {
            WAYPOINTS.forEach(builder::addWaypoint);
        }
        builder.build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        DirectionsResponse body = response.body();
                        if (body != null) {
                            final List<DirectionsRoute> routes = body.routes();
                            if (!routes.isEmpty()) {
                                startNavigation(routes.get(0));
                            } else {
                                if (listener != null) {
                                    listener.onNavigationFailed();
                                }
                            }
                        } else {
                            if (listener != null) {
                                listener.onNavigationFailed();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        if (listener != null) {
                            listener.onNavigationFailed();
                        }
                    }
                });
    }

    private void startNavigation(DirectionsRoute directionsRoute) {
        synchronized (navigationView) {
            if (directionsRoute == null) {
                return;
            }
            NavigationViewOptions options = NavigationViewOptions.builder()
                    .directionsRoute(directionsRoute)
                    .shouldSimulateRoute(shouldSimulateRoute())
                    .navigationListener(new NavigationListener() {
                        @Override
                        public void onCancelNavigation() {
                            navigationView.stopNavigation();
                            stopNavigation();
                        }

                        @Override
                        public void onNavigationFinished() {
                            RoutesHolder.getInstance().finishTrip();
                            stopNavigation();
                            navigationView.stopNavigation();
                        }

                        @Override
                        public void onNavigationRunning() {
                            //invoked when navigation was started
                        }
                    })
                    .progressChangeListener(new ProgressChangeListener() {
                        @Override
                        public void onProgressChange(Location currentLocation, RouteProgress routeProgress) {
                            RouteWrapper currentRoute = RoutesHolder.getInstance().getCurrentRoute();
                            if (currentRoute != null) {
                                Log.d("ROUTE CONFIRMED", currentRoute.toString());
                                ArrayList<Point> estimatedWaypoints = routeEngineProxy.getEstimatedWaypoints(currentRoute.getRoute(), currentLocation, destination);
                                WAYPOINTS = estimatedWaypoints;
                            }
                            String traveledInfo = "Traveled : " + routeProgress.distanceTraveled();
                            Log.d("DISTANCE", traveledInfo);
                            Route optimalRoute = routeEngineProxy.findOptimalRoute(activity, currentLocation, destination);
                            if (optimalRoute != null) {
                                suggestRoute(optimalRoute, currentLocation);
                            }
                        }
                    })
                    .build();
            try {
                navigationView.startNavigation(options);
                if (listener != null) {
                    listener.onNavigationRunning();
                }
            } catch (Throwable t) {
                if (listener != null) {
                    listener.onNavigationFailed();
                }
            }
        }
    }


    private boolean shouldSimulateRoute() {
        return BuildConfig.DEBUG;
    }

    private void stopNavigation() {
        updateWasNavigationStopped(true);
        updateWasInTunnel(false);
        WAYPOINTS = null;
    }

    private boolean wasInTunnel() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return preferences.getBoolean(WAS_IN_TUNNEL, false);
    }

    private void updateWasInTunnel(boolean wasInTunnel) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(WAS_IN_TUNNEL, wasInTunnel);
        editor.apply();
    }

    private void updateCurrentNightMode(int nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode);
        activity.recreate();
    }

    private boolean wasNavigationStopped() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return preferences.getBoolean(WAS_NAVIGATION_STOPPED, false);
    }

    private void updateWasNavigationStopped(boolean wasNavigationStopped) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(WAS_NAVIGATION_STOPPED, wasNavigationStopped);
        editor.apply();
    }


    private void suggestRoute(Route optimalRoute, Location location) {
        if (IS_POPUP_SHOWED) {
            return;
        }
        View popupView = LayoutInflater.from(activity).inflate(R.layout.route_confirm_popup, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        TextView popupMessage = popupView.findViewById(R.id.layout_popup_txtMessage);
        popupMessage.setText(optimalRoute.getOrigin().getAlias());

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 1, 1);
        IS_POPUP_SHOWED = popupWindow.isShowing();

        popupView.findViewById(R.id.accept_route_button).setOnClickListener(v -> {
            RoutesHolder.getInstance().updateStatus(optimalRoute, RouteStatus.CONFIRMED);
            popupWindow.dismiss();
            IS_POPUP_SHOWED = popupWindow.isShowing();
        });

        popupView.findViewById(R.id.dismiss_route_button).setOnClickListener(v -> {
            RoutesHolder.getInstance().updateStatus(optimalRoute, RouteStatus.REJECTED);
            popupWindow.dismiss();
            IS_POPUP_SHOWED = popupWindow.isShowing();
        });
    }
}
