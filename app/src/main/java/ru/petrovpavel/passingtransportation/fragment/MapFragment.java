package ru.petrovpavel.passingtransportation.fragment;
/*
 * Copyright (C)2021 Petrov Pavel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.animation.TypeEvaluator;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.mapbox.services.directions.v5.models.DirectionsWaypoint;
import com.mapbox.services.directions.v5.models.LegStep;
import com.mapbox.services.directions.v5.models.RouteLeg;
import com.mapbox.services.directions.v5.models.StepManeuver;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.GeocodingFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.petrovpavel.passingtransportation.data.MapData;
import ru.petrovpavel.passingtransportation.data.MotorContract;
import ru.petrovpavel.passingtransportation.utils.Utility;
import ru.petrovpavel.passingtransportation.widget.CollectionWidgetProvider;
import ru.petrovpavel.passingtransportation.R;

public class MapFragment extends Fragment {

    private final static String TAG = "MapFragment";
    private static final int PERMISSIONS_LOCATION = 0;
    private static String route_id;
    private final String FRAGMENT_TAG_REST = "FTAGR";
    FloatingActionButton floatingActionButton;
    LocationServices locationServices;
    private MapView mapView;
    private MapboxMap map;
    private String mode;
    private DirectionsRoute currentRoute;
    private Marker markerDestination;
    private Marker markerOrigin;
    private Position origin;
    private Position destination;
    private Polyline routePolyLine;
    private Activity mActivity;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        DrawerLayout drawer = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mActivity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        setRetainInstance(true);

        final GeocoderAutoCompleteView autocompleteStart = (GeocoderAutoCompleteView) rootView.findViewById(R.id.query_start);
        autocompleteStart.setAccessToken(getString(R.string.PUBLIC_TOKEN));
        autocompleteStart.setType(GeocodingCriteria.TYPE_POI);
        autocompleteStart.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
            @Override
            public void OnFeatureClick(GeocodingFeature feature) {
                Position position = feature.asPosition();
                updateMap(position.getLatitude(), position.getLongitude(), true);
            }
        });


        final GeocoderAutoCompleteView autocompleteDestination = (GeocoderAutoCompleteView) rootView.findViewById(R.id.query_destination);
        autocompleteDestination.setAccessToken(getString(R.string.PUBLIC_TOKEN));
        autocompleteDestination.setType(GeocodingCriteria.TYPE_POI);
        autocompleteDestination.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
            @Override
            public void OnFeatureClick(GeocodingFeature feature) {
                Position position = feature.asPosition();
                updateMap(position.getLatitude(), position.getLongitude(), false);
            }
        });

        locationServices = LocationServices.getLocationServices(mActivity);

        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        final AppBarLayout appBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar_layout);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {

                        appBarLayout.setExpanded(false);
                    }
                });
            }
        });

        rootView.findViewById(R.id.buttons).bringToFront();

        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.hasNetworkConnection(mActivity)) {
                    if (map != null) {
                        toggleGps(!map.isMyLocationEnabled(), autocompleteStart);
                    }
                } else {
                    Toast.makeText(mActivity, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!Utility.hasNetworkConnection(mActivity)) {
            Toast.makeText(mActivity, R.string.network_info, Toast.LENGTH_LONG).show();
        }

        ImageButton getCurrentLoc = (ImageButton) rootView.findViewById(R.id.get_current_location);

        getCurrentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location loc = map.getMyLocation();
                if (loc == null) {
                    if (!locationServices.areLocationPermissionsGranted()) {
                        ActivityCompat.requestPermissions(mActivity, new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
                    } else {
                        enableLocation(true, autocompleteStart);
                    }
                }
                autocompleteStart.requestFocus();
            }
        });

        ImageButton swapLoc = (ImageButton) rootView.findViewById(R.id.swap_endpoints);
        swapLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable startStr = autocompleteStart.getText();
                Editable destinationStr = autocompleteDestination.getText();
                autocompleteStart.setText(destinationStr);
                autocompleteDestination.setText(startStr);
                Position tmp = origin;
                origin = destination;
                destination = tmp;
                if (routePolyLine != null) {
                    map.removePolyline(routePolyLine);
                }
                if (markerOrigin != null) {
                    map.removeMarker(markerOrigin);
                }

                if (markerDestination != null) {
                    map.removeMarker(markerDestination);
                }

                if (destination != null) {
                    markerDestination = map.addMarker(new MarkerOptions()
                            .position(new LatLng(destination.getLatitude(), destination.getLongitude())).title(getString(R.string.destination)));
                }
                if (origin != null) {
                    markerOrigin = map.addMarker(new MarkerOptions()
                        .position(new LatLng(origin.getLatitude(), origin.getLongitude())).title(getString(R.string.origin)));
                }
            }
        });
        RecyclerView routesView = (RecyclerView) rootView.findViewById(R.id.routesList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        routesView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mActivity,
                layoutManager.getOrientation());
        routesView.addItemDecoration(dividerItemDecoration);

            }
        });

        routesView.setAdapter(availableRoutesAdapter);

        Button availableRoutesPath = (Button) rootView.findViewById(R.id.available_routes);
        availableRoutesPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                routesView.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
            }
        });


        Button driveButton = (Button) rootView.findViewById(R.id.drive);
        driveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routesView.setVisibility(View.GONE);
                if (Utility.hasNetworkConnection(mActivity)) {
                    if (map != null) {
                        mapView.setVisibility(View.VISIBLE);
                        try {
                            if (validateForm(autocompleteStart, autocompleteDestination)) {
                                getRoute(origin, destination, DirectionsCriteria.PROFILE_DRIVING, true);
                            }
                        } catch (ServicesException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(mActivity, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                }
            }
        });

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);

        collapsingToolbarLayout.setTitle(" ");

        if (routePolyLine == null) {
            rootView.findViewById(R.id.drive_toggle_fab).setVisibility(View.INVISIBLE);
        } else {
            rootView.findViewById(R.id.drive_toggle_fab).setVisibility(View.VISIBLE);
        }

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(getString(R.string.choose_dest));

                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appBarLayout.setExpanded(true, true);
            }
        });

        appBarLayout.setExpanded(true, true);

        rootView.findViewById(R.id.drive_toggle_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                autocompleteStart.clearFocus();
                autocompleteDestination.clearFocus();
                Bundle arguments = new Bundle();
                arguments.putString(Intent.EXTRA_TEXT, route_id);

                DriveFragment fragment = new DriveFragment();
                fragment.setArguments(arguments);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .addToBackStack(getString(R.string.back))
                        .add(R.id.frag_container, fragment, FRAGMENT_TAG_REST)
                        .commit();
            }
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(getString(R.string.map_data))) {
                MapData mapData = savedInstanceState.getParcelable(getString(R.string.map_data));
                if (mapData != null) {
                    if (mapData.getStart_lat() != 0) {
                        updateMap(mapData.getStart_lat(), mapData.getStart_long(), true);
                    }
                    if (mapData.getDest_lat() != 0) {
                        updateMap(mapData.getDest_lat(), mapData.getDest_long(), false);
                    }
                    if (mapData.getPolyLine() == 1) {
                        try {
                            getRoute(origin, destination, mapData.getMode(), false);
                        } catch (ServicesException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }

        return rootView;
    }

    private boolean validateForm(GeocoderAutoCompleteView autocompleteStart, GeocoderAutoCompleteView autoCompleteDestination) {
        boolean valid = true;

        String origin = autocompleteStart.getText().toString();
        if (TextUtils.isEmpty(origin)) {
            autocompleteStart.setError(Html.fromHtml("<font color='Red'>" + getString(R.string.required) + "</font>"));
            valid = false;
        } else {
            autocompleteStart.setError(null);
        }

        String destination = autoCompleteDestination.getText().toString();
        if (TextUtils.isEmpty(destination)) {
            autoCompleteDestination.setError(Html.fromHtml("<font color='Red'>" + getString(R.string.required) + "</font>"));
            valid = false;
        } else {
            autoCompleteDestination.setError(null);
        }

        return valid;
    }

    private void updateMap(double latitude, double longitude, boolean isOrigin) {
        if (routePolyLine != null) {
            map.removePolyline(routePolyLine);
        }
        if (!isOrigin) {
            if (markerDestination != null) {
                map.removeMarker(markerDestination);
            }
            markerDestination = map.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude)).title(getString(R.string.destination)));
            destination = Position.fromCoordinates(longitude, latitude);
            map.updateMarker(markerDestination);
        } else {
            if (markerOrigin != null) {
                map.removeMarker(markerOrigin);
            }
            markerOrigin = map.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude)).title(getString(R.string.origin)));
            origin = Position.fromCoordinates(longitude, latitude);
            map.updateMarker(markerOrigin);
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(15)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
    }

    private void getRoute(final Position origin, final Position destination, final String profile, final boolean info) throws ServicesException {

        if (null == origin) {
            Log.e(TAG, getString(R.string.origin_empty));
            Toast.makeText(mActivity, R.string.set_origin, Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == destination) {
            Log.e(TAG, getString(R.string.destination_empty));
            Toast.makeText(mActivity, R.string.set_dest, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Origin :: lat " + origin.getLatitude() + " long " + origin.getLongitude());
        Log.d(TAG, "Destination :: lat " + destination.getLatitude() + " long " + destination.getLongitude());

        ArrayList<Position> positions = new ArrayList<>();
        positions.add(destination);
        positions.add(Position.fromCoordinates(46.0201424, 51.5475297));
        positions.add(Position.fromCoordinates(46.0255263, 51.5401292));
        positions.add(origin);

        MapboxDirections client = new MapboxDirections.Builder()
//                .setOrigin(origin)
//                .setDestination(destination)
                .setCoordinates(positions)
                .setProfile(profile)
                .setSteps(true)
                .setAccessToken(getString(R.string.PUBLIC_TOKEN))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d(TAG, getString(R.string.response_code) + response.code());
                if (response.body() == null) {
                    Log.e(TAG, getString(R.string.route_error));
                    return;
                }
                mode = profile;
                rootView.findViewById(R.id.drive_toggle_fab).setVisibility(View.VISIBLE);
                try {

                    List<DirectionsWaypoint> mDirectionWaypoint = response.body().getWaypoints();
                    List<DirectionsRoute> mDirectionRoute = response.body().getRoutes();

                    ContentValues mWaypoint = new ContentValues();
                    Vector<ContentValues> cVVectorSteps = new Vector<>();

                    Geocoder geocoder = new Geocoder(mActivity, Locale.getDefault());

                    long ROUTE_ID = System.currentTimeMillis();
                    route_id = String.valueOf(ROUTE_ID);

                    try {
                        List<Address> address = geocoder.getFromLocation(
                                origin.getLatitude(),
                                origin.getLongitude(),
                                1);
                        mWaypoint.put(MotorContract.Waypoints.START_NAME, address.get(0).getFeatureName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        List<Address> address = geocoder.getFromLocation(
                                destination.getLatitude(),
                                destination.getLongitude(),
                                1);
                        mWaypoint.put(MotorContract.Waypoints.DEST_NAME, address.get(0).getFeatureName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mWaypoint.put(MotorContract.Waypoints.START_LONG, String.valueOf(mDirectionWaypoint.get(0).getLocation()[0]));
                    mWaypoint.put(MotorContract.Waypoints.START_LAT, String.valueOf(mDirectionWaypoint.get(0).getLocation()[1]));
                    mWaypoint.put(MotorContract.Waypoints.DEST_LONG, String.valueOf(mDirectionWaypoint.get(1).getLocation()[0]));
                    mWaypoint.put(MotorContract.Waypoints.DEST_LAT, String.valueOf(mDirectionWaypoint.get(1).getLocation()[1]));
                    mWaypoint.put(MotorContract.Waypoints.MODE, profile);

                    if (mDirectionRoute.size() > 0) {
                        mWaypoint.put(MotorContract.Waypoints.ROUTE_ID, String.valueOf(ROUTE_ID));
                        mWaypoint.put(MotorContract.Waypoints.ROUTE_DURATION, String.valueOf(mDirectionRoute.get(0).getDuration()));
                        mWaypoint.put(MotorContract.Waypoints.ROUTE_DISTANCE, String.valueOf(mDirectionRoute.get(0).getDistance()));


                        RouteLeg mLeg = response.body().getRoutes().get(0).getLegs().get(0);

                        for (LegStep mSteps : mLeg.getSteps()) {

                            ContentValues steps = new ContentValues();
                            StepManeuver maneuver = mSteps.getManeuver();
                            steps.put(MotorContract.Steps.ROUTE_ID, String.valueOf(ROUTE_ID));
                            steps.put(MotorContract.Steps.BEARING_BEFORE, String.valueOf(maneuver.getBearingBefore()));
                            steps.put(MotorContract.Steps.BEARING_AFTER, String.valueOf(maneuver.getBearingAfter()));
                            steps.put(MotorContract.Steps.LOCATION_LAT, String.valueOf(maneuver.getLocation()[1]));
                            steps.put(MotorContract.Steps.LOCATION_LONG, String.valueOf(maneuver.getLocation()[0]));
                            steps.put(MotorContract.Steps.TYPE, maneuver.getType());
                            steps.put(MotorContract.Steps.INSTRUCTION, maneuver.getInstruction());
                            steps.put(MotorContract.Steps.MODE, mSteps.getMode());
                            steps.put(MotorContract.Steps.DURATION, String.valueOf(mSteps.getDuration()));
                            steps.put(MotorContract.Steps.NAME, mSteps.getName());
                            steps.put(MotorContract.Steps.DISTANCE, String.valueOf(mSteps.getDistance()));

                            cVVectorSteps.add(steps);
                        }
                    }

                    int inserted = 0;

                    if (cVVectorSteps.size() > 0) {
                        ContentValues[] cvArray = new ContentValues[cVVectorSteps.size()];
                        cVVectorSteps.toArray(cvArray);
                        inserted = mActivity.getContentResolver().bulkInsert(MotorContract.Steps.CONTENT_URI, cvArray);
                    }
                    mActivity.getContentResolver().insert(MotorContract.Waypoints.CONTENT_URI, mWaypoint);

                    mActivity.sendBroadcast(new Intent(CollectionWidgetProvider.ACTION_UPDATE));

                    // Print some info about the route
                    currentRoute = response.body().getRoutes().get(0);
                    if (info) {
                        double distance = currentRoute.getDistance();
                        if (distance > 1000) {
                            distance /= 1000;
                            Toast.makeText(mActivity, String.format(Locale.US, getString(R.string.route_is) + "%.2f " + getString(R.string.km_long), distance), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mActivity, String.format(Locale.US, getString(R.string.route_is) + "%.2f " + getString(R.string.m_long), currentRoute.getDistance()), Toast.LENGTH_SHORT).show();
                        }
                    }
                    // Draw the route on the map
                    drawRoute(currentRoute, profile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, getString(R.string.error) + t.getMessage());
                Toast.makeText(mActivity, getString(R.string.error) + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(DirectionsRoute route, String profile) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        int color;
        if (profile.equals(DirectionsCriteria.PROFILE_CYCLING)) {
            color = ContextCompat.getColor(mActivity, R.color.polyBike);
        } else if (profile.equals(DirectionsCriteria.PROFILE_DRIVING)) {
            color = ContextCompat.getColor(mActivity, R.color.polyCar);
        } else {
            color = ContextCompat.getColor(mActivity, R.color.polyWalk);
        }

        if (routePolyLine != null) {
            map.removePolyline(routePolyLine);
        }

        routePolyLine = map.addPolyline(new PolylineOptions()
                .add(points)
                .color(color)
                .width(5));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        MapData mapData = new MapData(origin != null ? origin.getLatitude() : 0, origin != null ? origin.getLongitude() : 0, destination != null ? destination.getLatitude() : 0, destination != null ? destination.getLongitude() : 0, mode, markerOrigin != null ? 1 : 0, markerDestination != null ? 1 : 0, routePolyLine != null ? 1 : 0);
        outState.putParcelable(getString(R.string.map_data), mapData);
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @UiThread
    public void toggleGps(boolean enableGps, GeocoderAutoCompleteView autoCompleteView) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(mActivity, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
            } else {
                enableLocation(true, autoCompleteView);
            }
        } else {
            enableLocation(false, autoCompleteView);
        }
    }

    private Location enableLocation(boolean enabled, final GeocoderAutoCompleteView autoCompleteStart) {
        final Location[] newLocation = {null};
        final FlagGPSOneTime gps = new FlagGPSOneTime();
        if (enabled) {
            map.setMyLocationEnabled(true);
            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null && gps.flag) {
                        newLocation[0] = location;
                        if (routePolyLine != null) {
                            map.removePolyline(routePolyLine);
                        }
                        if (markerOrigin != null) {
                            map.removeMarker(markerOrigin);
                        }
                        IconFactory iconFactory = IconFactory.getInstance(mActivity);
                        Drawable iconDrawable = ContextCompat.getDrawable(mActivity, R.drawable.default_marker);
                        Icon icon = iconFactory.fromDrawable(iconDrawable);

                        markerOrigin = map.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude())).title(getString(R.string.origin)).icon(icon));


                        markerOrigin.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                        origin = Position.fromCoordinates(location.getLongitude(), location.getLatitude());
                        map.setCameraPosition(new CameraPosition.Builder()
                                .target(new LatLng(location))
                                .zoom(16)
                                .build());
                        Geocoder geocoder = new Geocoder(mActivity, Locale.getDefault());
                        try {
                            List<Address> address = geocoder.getFromLocation(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    1);
                            autoCompleteStart.setText(address.get(0).getFeatureName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    gps.flag = false;
                    map.setMyLocationEnabled(false);

                }
            });
            floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
            map.setMyLocationEnabled(false);
        }
        // Enable or disable the location layer on the map
        return newLocation[0];
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation(true, null);
                }
            }
        }
    }

    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.

        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude() +
                    ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude() +
                    ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }

    class FlagGPSOneTime {
        public boolean flag;

        FlagGPSOneTime() {
            this.flag = true;
        }
    }
}
