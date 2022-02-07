package ru.petrovpavel.passingtransportation.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;

import ru.petrovpavel.passingtransportation.R;
import ru.petrovpavel.passingtransportation.adapter.AvailableRoutesAdapter;
import ru.petrovpavel.passingtransportation.callback.NavigationReadyCallback;
import ru.petrovpavel.passingtransportation.data.Route;
import ru.petrovpavel.passingtransportation.data.memory.RoutesHolder;
import ru.petrovpavel.passingtransportation.interfaces.NavigationStatusListener;
import ru.petrovpavel.passingtransportation.utils.RouteConfirmationDialogBuilder;

public class FragmentNavigation extends Fragment {

    private static final String TAG = "FragmentNavigation";

    @Nullable
    private NavigationView navigationView;
    private Point origin;
    private Point destination;
    private Activity mActivity;
    private NavigationReadyCallback onNavigationReadyCallback;

    private final FirebaseDatabase firebaseDB = FirebaseDatabase.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity = getActivity();
        return inflater.inflate(R.layout.route_navigation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPoints();
        navigationView = view.findViewById(R.id.navigation_view_fragment);
        navigationView.onCreate(savedInstanceState);
        onNavigationReadyCallback = new NavigationReadyCallback(
                getActivity(),
                navigationView,
                new NavigationListener(getActivity()),
                origin,
                destination);
        availableRoutesAdapterInitialization();
        navigationView.initialize(
                onNavigationReadyCallback,
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

    private void availableRoutesAdapterInitialization() {
        DatabaseReference routes = firebaseDB.getReference("available_routes");

        routes.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Route value = snapshot.getValue(Route.class);
                RoutesHolder.getInstance().addAvailableRoute(value);
                Log.d(TAG, "Added value: " + value);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                Route value = snapshot.getValue(Route.class);
//                RoutesHolder.getInstance().addAvailableRouteIfAbsent(value);
//                Log.d(TAG, "Changed value: " + value);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Route value = snapshot.getValue(Route.class);
                RoutesHolder.getInstance().removeAvailableRoute(value);
                Log.d(TAG, "Removed value: " + value);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

//        AvailableRoutesAdapter availableRoutesAdapter = new AvailableRoutesAdapter(mActivity, RoutesHolder.getInstance().getAvailableRoutes());
//        availableRoutesAdapter.setListener(route -> {
//            RouteConfirmationDialogBuilder.build(mActivity, route.getOrigin().getAlias(), route.getDestination().getAlias());
//        });
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
