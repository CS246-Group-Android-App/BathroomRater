package com.cs246.bathroom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by tyler on 2/26/16.
 */
    public class DatabaseAccess {
        //PHP database names
        public static final String KEY_NAME = "name";
        public static final String KEY_LAT = "lat";
        public static final String KEY_LNG = "lng";
        //Gathered from constructor
        String name;
        double lat;
        double lng;
        boolean isPost;
        //For Post method
        String response = null;
        //For Get Method
        public static String[] markerName;
        public static double[] markerLat;
        public static double[] markerLng;
        public static int numMarkers;

        DatabaseAccess(String name, LatLng position, boolean isPost) {
            this.name = name;
            this.lat = position.latitude;
            this.lng = position.longitude;
            this.isPost = isPost;
        }

        public void runAction() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (isPost) {
                        post();
                        numMarkers = getMethod();
                    }
                    else {
                        numMarkers = getMethod();
                    }
                }
            }).start();
        }

        public String post() {
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

        public int getMethod() {
            markerName = new String[50];
            markerLat = new double[50];
            markerLng = new double[50];
            numMarkers = 0;
            try {
                String request = "http://php-tsorenson.rhcloud.com/androidGet.php";
                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(in);
                String line = null;
                while( (line = br.readLine()) != null){
                    String[] marker = line.split(",");
                    marker[2] = marker[2].replace(";<br />", " ");
                    storeMarker(marker);
                    numMarkers++;
                }

                conn.disconnect();
                return numMarkers;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        public void storeMarker(String[] marker) {
            markerName[numMarkers] = marker[0];
            markerLat[numMarkers] = Double.parseDouble(marker[1]);
            markerLng[numMarkers] = Double.parseDouble(marker[2]);
        }
    }
