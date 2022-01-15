package ru.petrovpavel.passingtransportation.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;

import ru.petrovpavel.passingtransportation.R;
import ru.petrovpavel.passingtransportation.callback.NavigationReadyCallback;
import ru.petrovpavel.passingtransportation.interfaces.NavigationStatusListener;

public class FragmentNavigation extends Fragment {

    @Nullable
    private NavigationView navigationView;

    private Point origin;

    private Point destination;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.route_navigation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPoints();
        navigationView = view.findViewById(R.id.navigation_view_fragment);
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(
                new NavigationReadyCallback(
                        getActivity(),
                        navigationView,
                        new NavigationListener(getActivity()),
                        origin,
                        destination),
                getInitialPosition());
    }

    private void initPoints() {
        Bundle arguments = getArguments();
        this.origin = getOriginPoint(arguments);
        this.destination = getDestinationPoint(arguments);
    }

    private Point getOriginPoint(Bundle arguments) {
        Double lng = arguments.getDouble(getString(R.string.origin_lng));
        Double lat = arguments.getDouble(getString(R.string.origin_lat));
        return Point.fromLngLat(lng, lat);
    }

    private Point getDestinationPoint(Bundle arguments) {
        Double lng = arguments.getDouble(getString(R.string.destination_lng));
        Double lat = arguments.getDouble(getString(R.string.destination_lat));
        return Point.fromLngLat(lng, lat);
    }

    private CameraPosition getInitialPosition() {
        return new CameraPosition.Builder()
                .target(getOrigin())
                .zoom(16)
                .build();
    }

    private LatLng getOrigin() {
        return new LatLng(origin.latitude(), origin.longitude());
    }


    @Override
    public void onStart() {
        super.onStart();
        if (navigationView != null) {
            navigationView.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationView != null) {
            navigationView.onResume();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (navigationView != null) {
            navigationView.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            if (navigationView != null) {
                navigationView.onRestoreInstanceState(savedInstanceState);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (navigationView != null) {
            navigationView.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (navigationView != null) {
            navigationView.onStop();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (navigationView != null) {
            navigationView.onLowMemory();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (navigationView != null) {
            navigationView.onDestroy();
        }
    }

    private static class NavigationListener implements NavigationStatusListener {

        private Activity activity;

        private NavigationListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onNavigationRunning() {
            activity = null;
        }

        @Override
        public void onNavigationFailed() {
            if (activity != null) {
                activity.onBackPressed();
            }
            activity = null;
        }
    }
}
