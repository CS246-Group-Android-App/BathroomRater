package com.cs246.bathroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by tyler on 2/25/16.
 */
public class AddLocationActivity extends AppCompatActivity{

    EditText locationName;
    Button submit;
    LatLng locationForMap;
    DatabaseAccess db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        //Get location from the MapsActivity
        Bundle bundle = getIntent().getParcelableExtra("Bundle");
        locationForMap = bundle.getParcelable("Location");

        //Set up all the user inputs
        setInputs();

        //Submit button listener
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToDb();
                returnToMap();
            }
        });
    }

    public void setInputs() {
        locationName = (EditText) findViewById(R.id.locationName);
        submit = (Button) findViewById(R.id.ratingSubmit);
    }

    public void sendToDb() {
        //Send the post request
        db = new DatabaseAccess(
                locationName.getText().toString(),
                locationForMap,
                true);
        db.runAction();
    }

    public void returnToMap() {
        //Return to the map with the added locations
        Intent returnToMap = new Intent(AddLocationActivity.this, MapsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("NumberOfLocations", db.numMarkers);
        bundle.putStringArray("Names", db.markerName);
        bundle.putDoubleArray("Lats", db.markerLat);
        bundle.putDoubleArray("Long", db.markerLng);
        returnToMap.putExtra("Bundle", bundle);
        startActivity(returnToMap);
    }
}
