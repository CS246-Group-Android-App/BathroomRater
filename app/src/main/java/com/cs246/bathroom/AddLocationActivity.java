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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        Bundle bundle = getIntent().getParcelableExtra("Bundle");
        locationForMap = bundle.getParcelable("Location");
        setInputs();
        submit = (Button) findViewById(R.id.ratingSubmit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent post = new Intent(AddLocationActivity.this, DatabaseAccess.class);
                Bundle info = new Bundle();
                info.putString("Name", locationName.getText().toString());
                info.putParcelable("LatLng", locationForMap);
                post.putExtra("Bundle", info);
                startActivity(post);
            }
        });
    }

    public void setInputs() {
        locationName = (EditText) findViewById(R.id.locationName);
    }
}
