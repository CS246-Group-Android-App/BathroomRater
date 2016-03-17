package com.cs246.bathroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

/**Class used to create new save bathrooms
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
        if (locationForMap != null){
            Log.i("markerLocation", "Pin location successfully found");
        }

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

    /**
     * sets the inputs for the users information(EditText & Submit button)
     * TODO:Add other form inputs
     */
    public void setInputs() {
        locationName = (EditText) findViewById(R.id.locationName);
        submit = (Button) findViewById(R.id.ratingSubmit);
    }

    /**
     * Sends new location to database for saving
     */
    public void sendToDb() {
        //Send the post request
        db = new DatabaseAccess(
                locationName.getText().toString(),
                locationForMap,
                true);
        db.runAction();
    }

    /**
     * Returns to the map for the user to continue browsing
     */
    public void returnToMap() {

        Log.i("ActivitySwitch", "Switching to Map Activity");
        //Return to the map with the added locations
        Intent returnToMap = new Intent(AddLocationActivity.this, MapsActivity.class);
        startActivity(returnToMap);
    }
}
