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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.petrovpavel.passingtransportation.BuildConfig;
import ru.petrovpavel.passingtransportation.adapter.AvailableRoutesAdapter;
import ru.petrovpavel.passingtransportation.data.MapData;
import ru.petrovpavel.passingtransportation.data.MotorContract;
import ru.petrovpavel.passingtransportation.data.Route;
import ru.petrovpavel.passingtransportation.data.memory.VehicleHolder;
import ru.petrovpavel.passingtransportation.listener.OnSearchPassingRoutesCheckedChangeListener;
import ru.petrovpavel.passingtransportation.listener.VehicleSliderChangeListener;
import ru.petrovpavel.passingtransportation.listener.VehicleTextInputEditTextListener;
import ru.petrovpavel.passingtransportation.utils.RouteConfirmationDialogBuilder;
import ru.petrovpavel.passingtransportation.utils.Utility;
import ru.petrovpavel.passingtransportation.widget.CollectionWidgetProvider;
import ru.petrovpavel.passingtransportation.R;

public class MapFragment extends Fragment {

    private final static String TAG = "MapFragment";
    private static final int PERMISSIONS_LOCATION = 0;
    private final String FRAGMENT_TAG_REST = "FTAGR";

    private static String route_id;
    FloatingActionButton floatingActionButton;
    LocationComponent locationComponent;
    LocationEngine locationEngine;
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

        final GeocoderAutoCompleteView autocompleteStart = getAutoCompleteView(R.id.query_start, true);
        final GeocoderAutoCompleteView autocompleteDestination = getAutoCompleteView(R.id.query_destination, false);

        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        final AppBarLayout appBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar_layout);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull LatLng point) {
                        appBarLayout.setExpanded(false);
                        return true;
                    }
                });
                mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public boolean onMapLongClick(@NonNull LatLng point) {
                        appBarLayout.setExpanded(true);
                        return true;
                    }
                });
                map.setStyle(Style.MAPBOX_STREETS);
                map.setCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(51.5295907, 45.9787404))
                                .zoom(10)
                                .build());
                map.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        locationComponent = map.getLocationComponent();
                        locationComponent.activateLocationComponent(new LocationComponentActivationOptions.Builder(mActivity, style).build());
                        locationEngine = locationComponent.getLocationEngine();
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
                        toggleGps(!locationComponent.isLocationComponentEnabled(), autocompleteStart);
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
                Location loc = map.getLocationComponent().getLastKnownLocation();
                if (loc == null) {
                    if (!PermissionsManager.areLocationPermissionsGranted(mActivity)) {
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
        ConstraintLayout vehicleLayout = (ConstraintLayout) rootView.findViewById(R.id.vehicleSettings);

        Slider capacitySlider = (Slider) rootView.findViewById(R.id.sliderCapacity);
        TextInputEditText capacityValue = rootView.findViewById(R.id.editTextCapacity);
        TextInputLayout capacityEditTextIL = (TextInputLayout) rootView.findViewById(R.id.layoutCapacity);

        capacityValue.setOnKeyListener(new VehicleTextInputEditTextListener(capacityEditTextIL, capacitySlider));
        capacitySlider.addOnChangeListener(new VehicleSliderChangeListener(capacityEditTextIL, capacityValue));

        Slider delaySlider = (Slider) rootView.findViewById(R.id.sliderDelay);
        TextInputEditText delayValue = rootView.findViewById(R.id.editTextDelay);
        TextInputLayout delayEditTextIL = (TextInputLayout) rootView.findViewById(R.id.layoutDelay);

        delayValue.setOnKeyListener(new VehicleTextInputEditTextListener(delayEditTextIL, delaySlider));
        delaySlider.addOnChangeListener(new VehicleSliderChangeListener(delayEditTextIL, delayValue));

        SwitchMaterial switchSearchRoutes = rootView.findViewById(R.id.switchSearchPassingRoutes);
        switchSearchRoutes.setOnCheckedChangeListener(new OnSearchPassingRoutesCheckedChangeListener(capacityValue, delayValue));


        Button driveButton = (Button) rootView.findViewById(R.id.route_tab);
        driveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vehicleLayout.setVisibility(View.GONE);
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

        Button vehicleButton = (Button) rootView.findViewById(R.id.vehicle_tab);
        vehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vehicleLayout.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
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
                Optional.ofNullable(capacityValue.getText())
                        .map(CharSequence::toString)
                        .map(Integer::parseInt)
                        .ifPresent(value -> VehicleHolder.getInstance().setAvailableCapacity(value));
                autocompleteStart.clearFocus();
                autocompleteDestination.clearFocus();
                Bundle arguments = new Bundle();
                arguments.putDouble(getString(R.string.origin_lng), origin.getLongitude());
                arguments.putDouble(getString(R.string.origin_lat), origin.getLatitude());
                arguments.putDouble(getString(R.string.destination_lng), destination.getLongitude());
                arguments.putDouble(getString(R.string.destination_lat), destination.getLatitude());

                FragmentNavigation fragmentNavigation = new FragmentNavigation();
                fragmentNavigation.setArguments(arguments);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .addToBackStack(getString(R.string.back))
                        .add(R.id.frag_container, fragmentNavigation, FRAGMENT_TAG_REST)
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

    @NonNull
    private GeocoderAutoCompleteView getAutoCompleteView(int autoCompleteViewId, boolean isOrigin) {
        final GeocoderAutoCompleteView autocompleteStart = (GeocoderAutoCompleteView) rootView.findViewById(autoCompleteViewId);

        autocompleteStart.setAccessToken(BuildConfig.MAPBOX_TOKEN);
        autocompleteStart.setType(GeocodingCriteria.TYPE_POI);

        autocompleteStart.setOnFeatureListener(feature -> {
            Position position = feature.asPosition();
            updateMap(position.getLatitude(), position.getLongitude(), isOrigin);
        });
        return autocompleteStart;
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


        MapboxDirections client = new MapboxDirections.Builder()
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(profile)
                .setSteps(true)
                .setAccessToken(BuildConfig.MAPBOX_TOKEN)
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
            if (!PermissionsManager.areLocationPermissionsGranted(mActivity)) {
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

    @SuppressWarnings( {"MissingPermission"})
    private Location enableLocation(boolean enabled, final GeocoderAutoCompleteView autoCompleteStart) {
        final Location[] newLocation = {null};
        final FlagGPSOneTime gps = new FlagGPSOneTime();

        if (enabled) {
            locationComponent.setLocationComponentEnabled(true);
            locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    Location location = result.getLastLocation();
                    if (location != null && gps.flag) {
                        newLocation[0] = location;
                        if (routePolyLine != null) {
                            map.removePolyline(routePolyLine);
                        }
                        if (markerOrigin != null) {
                            map.removeMarker(markerOrigin);
                        }

                        Icon icon = IconFactory.getInstance(mActivity).fromBitmap(bitmapFromVector(mActivity, R.drawable.ic_my_location_24dp));

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
                            autoCompleteStart.setText(new String(address.get(0).getAddressLine(0).getBytes()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    gps.flag = false;
                    locationComponent.setLocationComponentEnabled(false);
                }

                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
            floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
            locationComponent.setLocationComponentEnabled(false);
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

    private Bitmap bitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        return Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
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
