package com.cs246.bathroom;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by tyler on 2/26/16.
 */
public class DatabaseAccess extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_result);
        Bundle bundle = getIntent().getParcelableExtra("Bundle");
        String name = bundle.getString("Name");
        LatLng location = bundle.getParcelable("LatLng");
        Post post = new Post(name, location);
        post.execute();
    }

    public class Post extends AsyncTask {
        public static final String KEY_NAME = "name";
        public static final String KEY_LAT = "lat";
        public static final String KEY_LNG = "lng";
        String name;
        double lat;
        double lng;
        TextView responseView;
        String response = null;

        Post(String name, LatLng position)
        {
            this.name = name;
            this.lat = position.latitude;
            this.lng = position.longitude;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            responseView = (TextView) findViewById(R.id.response);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                String urlParameters = KEY_NAME + "=" + name + "&" +
                        KEY_LAT + "=" + lat + "&" + KEY_LNG + "=" + lng;
                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;
                String request = "http://php-tsorenson.rhcloud.com/androidPost.php";
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                conn.setUseCaches(false);
                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.write(postData);
                }

                InputStream in = new BufferedInputStream(conn.getInputStream());
                byte[] contents = new byte[1024];
                int bytesRead=0;
                while( (bytesRead = in.read(contents)) != -1){
                    response += new String(contents, 0, bytesRead);
                }
                Log.d("Retrieved", response);
                conn.disconnect();
                return response;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            responseView.setText(response);
        }
    }
}
