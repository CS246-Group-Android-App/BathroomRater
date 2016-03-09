package com.cs246.bathroom;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    CoordinatorLayout coordinatorLayout;
    LocationManager locationManager;
    LocationListener locationListener;
    Location usersCurrentLocation;
    LinearLayout buttonPanel;
    Button addLocation;
    Marker currentMarker;
    int numMarkers;
    String[] names;
    double[] lats;
    double[] longs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        //Get the users location
        setUpUserLocation();

        //Set up button listener
        setInputs();
        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addLocationForm = new Intent(MapsActivity.this, AddLocationActivity.class);
                addLocationForm.putExtra("Location", currentMarker.getPosition().toString());
                Bundle markerLocation = new Bundle();
                LatLng position = currentMarker.getPosition();
                markerLocation.putParcelable("Location", position);
                addLocationForm.putExtra("Bundle", markerLocation);
                startActivity(addLocationForm);
                stopLocationListner();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Read locations from Database
        ReadLocationsFromDatabase read = new ReadLocationsFromDatabase();
        read.execute();



        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }




        mMap.setMyLocationEnabled(true);
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
                marker.showInfoWindow();
                buttonPanel.setVisibility(View.VISIBLE);
                currentMarker = marker;
                return false;
            }
        });
        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                buttonPanel.setVisibility(View.GONE);
            }
        });
        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            View.OnClickListener undoOnClickListener;

            @Override
            public void onInfoWindowLongClick(final Marker marker) {
                marker.remove();
                Snackbar.make(coordinatorLayout, "Marker removed", Snackbar.LENGTH_LONG)
                        .setAction("Undo", undoOnClickListener)
                        .show();

                undoOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMap.addMarker(new MarkerOptions()
                                .position(marker.getPosition())
                                .title("Click add location to add a rating!"));
                    }
                };
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
        usersCurrentLocation = locationManager.getLastKnownLocation(locationProvider);
        if (usersCurrentLocation == null) {
            usersCurrentLocation = new Location("fakeLocation");
            usersCurrentLocation.setLatitude(0);
            usersCurrentLocation.setLongitude(0);
        }
    }

    public void setInputs() {
        addLocation = (Button) findViewById(R.id.addLocation);
        buttonPanel = (LinearLayout) findViewById(R.id.buttonPanel);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinateLayout);
    }

    public void stopLocationListner() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
                    // contacts-related task you need to do.
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                            (new LatLng(usersCurrentLocation.getLatitude(),
                                            usersCurrentLocation.getLongitude()),
                                    15));
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private class ReadLocationsFromDatabase extends AsyncTask {
        ReadLocationsFromDatabase() {

        }

        @Override
        protected Object doInBackground(Object[] params) {
            Bundle bundle = getIntent().getParcelableExtra("Bundle");
            numMarkers = bundle.getInt("NumberOfLocations");
            names = bundle.getStringArray("Names");
            lats = bundle.getDoubleArray("Lats");
            longs = bundle.getDoubleArray("Long");
            for (int i = 0;i < numMarkers;i++) {
                final int finalI = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(lats[finalI], longs[finalI])).title(names[finalI]));
                    }
                });
            }
            return null;
        }

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
}
