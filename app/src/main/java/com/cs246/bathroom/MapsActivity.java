package com.cs246.bathroom;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Use the Google maps API to set locations
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    boolean userLocation = true;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    CoordinatorLayout coordinatorLayout;
    LocationManager locationManager;
    LocationListener locationListener;
    Location usersCurrentLocation;
    Button addLocation;
    Marker currentMarker;
    int numMarkers;
    int[] stars;
    String[] names, comments, hand, change;
    double[] lats;
    double[] longs;
    Map markerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == 0) {
            //Get the users location
            setUpUserLocation();
        }

        //Set up button listener
        setInputs();

        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMarker.getTitle().equals("Click add location to add a rating!")) {
                    Intent addLocationForm = new Intent(MapsActivity.this, AddLocationActivity.class);
                    addLocationForm.putExtra("Location", currentMarker.getPosition().toString());
                    Bundle markerLocation = new Bundle();
                    LatLng position = currentMarker.getPosition();
                    markerLocation.putParcelable("Location", position);
                    addLocationForm.putExtra("Bundle", markerLocation);
                    startActivity(addLocationForm);
                    stopLocationListner();
                } else {
                    Intent getLocationDetails = new Intent(MapsActivity.this, LocationDetails.class);
                    Bundle info = new Bundle();
                    info.putString("Name", currentMarker.getTitle());
                    Bundle values = findMarkerInfo(currentMarker);
                    info.putBundle("Info", values);
                    getLocationDetails.putExtra("Bundle", info);
                    startActivity(getLocationDetails);
                    stopLocationListner();
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Read locations from Database
        ReadLocationsFromDatabase read = new ReadLocationsFromDatabase();
        read.execute();

        mMap.setPadding(0, 250, 0, 0);



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == 0) {
            mMap.setMyLocationEnabled(true);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                    usersCurrentLocation.getLatitude(), usersCurrentLocation.getLongitude()), 15));
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("Click add location to add a rating!"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
                if (!marker.getTitle().equals("Click add location to add a rating!")) {
                    addLocation.setText("View Details");
                }
                marker.showInfoWindow();
                addLocation.setVisibility(View.VISIBLE);
                mMap.setPadding(0, 250, 0, 200);
                currentMarker = marker;
                return false;
            }
        });
        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                addLocation.setVisibility(View.GONE);
                addLocation.setText("Add location");
                mMap.setPadding(0,250,0,0);
            }
        });
        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(final Marker marker) {
                View.OnClickListener undoOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMap.addMarker(new MarkerOptions()
                                .position(marker.getPosition())
                                .title("Click add location to add a rating!"));
                    }
                };

                if (!marker.getTitle().equals("Click add location to add a rating!")) {
                    Snackbar.make(coordinatorLayout, "Cannot remove this location!", Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
                marker.remove();
                Snackbar.make(coordinatorLayout, "Marker removed", Snackbar.LENGTH_LONG)
                        .setAction("Undo", undoOnClickListener)
                        .show();

            }
        });

    }

    public void setUpUserLocation() {
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                usersCurrentLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == 0) {
            locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
            usersCurrentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (usersCurrentLocation == null) {
                usersCurrentLocation = new Location("fakeLocation");
                usersCurrentLocation.setLatitude(37);
                usersCurrentLocation.setLongitude(-120);
            }
        }
    }

    public void setInputs() {
        addLocation = (Button) findViewById(R.id.addLocation);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinateLayout);
    }

    public void stopLocationListner() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    userLocation = true;
                } else {
                    userLocation = false;
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Read locations from DB and show markers
     */
    private class ReadLocationsFromDatabase extends AsyncTask {
        ReadLocationsFromDatabase() {

        }

        @Override
        protected Object doInBackground(Object[] params) {
            numMarkers = DatabaseAccess.numMarkers;
            names = DatabaseAccess.markerName;
            lats = DatabaseAccess.markerLat;
            longs = DatabaseAccess.markerLng;
            stars = DatabaseAccess.markerStars;
            hand = DatabaseAccess.markerHand;
            comments = DatabaseAccess.markerComments;
            change = DatabaseAccess.markerChange;
            markerInfo = new HashMap();
            for (int i = 0;i < numMarkers;i++) {
                final int finalI = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Marker marker = null;
                        Bundle info = new Bundle();
                        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lats[finalI], longs[finalI])).title(names[finalI]));
                        info.putInt("stars", stars[finalI]);
                        info.putString("hand", hand[finalI]);
                        info.putString("change", change[finalI]);
                        info.putString("comments", comments[finalI]);
                        markerInfo.put(marker, info);
                    }
                });
            }
            return null;
        }

        /**
         * Completed submission
         * @param o
         */
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(coordinatorLayout, numMarkers + " markers added", Snackbar.LENGTH_LONG)
                            .show();
                }
            });
        }
    }

    /**
     * Returns the info Bundle for the marker given
     * @param marker
     * @return
     */
    private Bundle findMarkerInfo(Marker marker) {
        if (markerInfo.containsKey(marker)) {
            return (Bundle) markerInfo.get(marker);
        }
        return null;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
