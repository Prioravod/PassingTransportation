package ru.petrovpavel.passingtransportation.activity;
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import ru.petrovpavel.passingtransportation.auth.AuthActivity;
import ru.petrovpavel.passingtransportation.fragment.AboutFragment;
import ru.petrovpavel.passingtransportation.fragment.DriveCollectionFragment;
import ru.petrovpavel.passingtransportation.fragment.DriveFragment;
import ru.petrovpavel.passingtransportation.fragment.MapFragment;
import ru.petrovpavel.passingtransportation.R;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, DriveCollectionFragment.Callback {

    private static final String FRAGMENT_TAG_MAP = "MAP_FRAGMENT";
    private final static String STATE_FRAGMENT = "stateFragment";
    private static final String TAG = "BaseActivity";

    private final String FRAGMENT_TAG_REST = "FTAGR";
    private int currentMenuItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        View hView = navigationView.getHeaderView(0);
        TextView nav_user_name = (TextView) hView.findViewById(R.id.user_name);
        nav_user_name.setText(String.format(Locale.ENGLISH, getString(R.string.welcome) + " %s", user != null ? user.getDisplayName() : null));
        TextView nav_user_email = (TextView) hView.findViewById(R.id.user_email);
        nav_user_email.setText(user != null ? user.getEmail() : null);

        if (savedInstanceState == null) {
            currentMenuItemId = R.id.nav_map;
            navigationView.getMenu().getItem(0).setChecked(true);
        } else {
            currentMenuItemId = savedInstanceState.getInt(STATE_FRAGMENT);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String routeId = extras.getString(getString(R.string.route_id));
            Bundle arguments = new Bundle();
            arguments.putString(Intent.EXTRA_TEXT, routeId);

            DriveFragment fragment = new DriveFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frag_container, fragment, FRAGMENT_TAG_REST)
                    .commit();

        } else if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP) == null && getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_REST) == null) {
            doMenuAction(currentMenuItemId);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0 && !getSupportFragmentManager().isDestroyed()) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    return true;
                } else {
                    finish();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doMenuAction(int menuItemId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (menuItemId) {
            case R.id.nav_map:

                fragmentManager.beginTransaction()
                        .replace(R.id.frag_container, new MapFragment(), FRAGMENT_TAG_MAP).commit();

                break;
            case R.id.nav_directions:

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frag_container, new DriveCollectionFragment(), FRAGMENT_TAG_REST)
                        .commit();

                break;
            case R.id.nav_about:

                fragmentManager.beginTransaction()
                        .replace(R.id.frag_container, new AboutFragment(), FRAGMENT_TAG_REST).commit();

                break;
            case R.id.nav_sign_out:
                signOut();
                break;
            default:
                //nothing;
        }
    }

    private void signOut() {

        // Firebase sign out

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int SIGN_IN_MODE = sharedPref.getInt(getString(R.string.login_mode), -1);
        // Google sign out
        switch (SIGN_IN_MODE) {
            case -1:
                Intent intent = new Intent(this, AuthActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
                break;

            case 0:
                //nothing
                break;
            case 1:
//                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestIdToken(getString(R.string.default_web_client_id))
//                        .requestEmail()
//                        .build();
//
//                GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
//                        .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
//                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                        .build();
//                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
//                        new ResultCallback<Status>() {
//                            @Override
//                            public void onResult(@NonNull Status status) {
//
//                            }
//                        });
                break;
            case 2:
                //nothing
                break;

            default:
                //
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.login_mode), -1);
        editor.apply();

        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        doMenuAction(id);
        currentMenuItemId = id;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_FRAGMENT, currentMenuItemId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, getString(R.string.connection_failed) + connectionResult);
        Toast.makeText(this, getString(R.string.google_play_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(String route_id) {
        Bundle arguments = new Bundle();
        arguments.putString(Intent.EXTRA_TEXT, route_id);

        DriveFragment fragment = new DriveFragment();
        fragment.setArguments(arguments);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frag_container, fragment, FRAGMENT_TAG_REST)
                .addToBackStack(getString(R.string.back))
                .commit();
    }
}
