package com.cs246.bathroom;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;

import com.google.android.gms.maps.model.LatLng;

/**Class used to create new save bathrooms
 * Created by tyler on 2/25/16.
 */
public class AddLocationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    EditText locationName, comments;
    RadioButton hand, change;
    RadioGroup handGroup, changeGroup;
    RatingBar stars;
    Button submit;
    LatLng locationForMap;
    DatabaseAccess db;
    DrawerLayout drawerLayout;
    Bundle additionalInfo = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_location_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

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
                getInputs();
                savedNewLocation send = new savedNewLocation();
                send.execute();
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
        hand = (RadioButton) findViewById(R.id.radioHC);
        change = (RadioButton) findViewById(R.id.radioCT);
        stars = (RatingBar) findViewById(R.id.ratingBar);
        comments = (EditText) findViewById(R.id.comments);
        handGroup = (RadioGroup) findViewById(R.id.handGroup);
        changeGroup = (RadioGroup) findViewById(R.id.changeGroup);
    }

    public void getInputs() {
        try {
            additionalInfo.putString("hand", getRadioValue(handGroup));
            additionalInfo.putString("change", getRadioValue(changeGroup));
            additionalInfo.putInt("stars", ((int) stars.getRating()));
            additionalInfo.putString("comments", comments.getText().toString());
        }
        catch (NullPointerException ex) {
            Snackbar.make(drawerLayout, "Please enter all values before submitting!", Snackbar.LENGTH_SHORT);
            getInputs();
        }
    }

    public String getRadioValue(RadioGroup group) {
        int selected = group.getCheckedRadioButtonId();
        RadioButton radioSelected = (RadioButton) findViewById(selected);
        return radioSelected.getText().toString();
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
    public class savedNewLocation extends AsyncTask<Void, Void, Void> {
        /**
         * Sends new location to database for saving
         */
        public void sendToDb() {
            //Send the post request
            db = new DatabaseAccess(
                    locationName.getText().toString(),
                    locationForMap,
                    true,
                    additionalInfo);
            db.runAction();
        }

        @Override
        protected Void doInBackground(Void ... params) {
            sendToDb();
            return null;
        }

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
