package com.cs246.bathroom;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

/**
 * Created by tyler on 2/25/16.
 */
public class AddLocationActivity extends AppCompatActivity{

    EditText locationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        setInputs();
    }

    public void setInputs() {
        locationName = (EditText) findViewById(R.id.locationName);
    }
}
