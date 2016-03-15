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
        public static boolean done = true;
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
        public static int numUsers;
        public static String[] usernames;
        public static String[] userPass;

        DatabaseAccess(String name, LatLng position, boolean isPost) {
            done = false;
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
                        if (name.equals("login")) {
                            numUsers = getMethod();
                        }
                        else {
                            numMarkers = getMethod();
                        }
                    }
                    else {
                        if (name.equals("login")) {
                            numUsers = getMethod();
                        }
                        else {
                            numMarkers = getMethod();
                        }
                    }
                    if (numMarkers == 0)
                        Log.d("Markers", Integer.toString(numMarkers));
                    done = true;
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
            numUsers = 0;
            usernames = new String[50];
            userPass = new String[50];
            try {
                String requestMarkers = "http://php-tsorenson.rhcloud.com/androidGet.php";
                String requestUsers = "http://php-tsorenson.rhcloud.com/getUsers.php";
                URL url;
                if (name.equals("login")) {
                    url = new URL(requestUsers);
                }
                else {
                    url = new URL(requestMarkers);
                }
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(in);
                String line = null;
                while( (line = br.readLine()) != null){
                    if(name.equals("login")) {
                        readUsers(line);
                    }
                    else {
                        readMarkers(line);
                    }
                }

                conn.disconnect();
                if (name.equals("login")) {
                    return numUsers;
                }
                else {
                    return numMarkers;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        public void readMarkers(String line) {
            String[] marker = line.split(",");
            marker[2] = marker[2].replace(";<br />", " ");
            storeMarker(marker);
            numMarkers++;
        }

        public void readUsers(String line) {
            String[] user = line.split(",");
            user[1] = user[1].replace(";<br />", "");
            storeUsers(user);
            numUsers++;
        }

        public void storeMarker(String[] marker) {
            markerName[numMarkers] = marker[0];
            markerLat[numMarkers] = Double.parseDouble(marker[1]);
            markerLng[numMarkers] = Double.parseDouble(marker[2]);
        }

        public void storeUsers(String[] users) {
            usernames[numUsers] = users[0];
            userPass[numUsers] = users[1];
        }
    }
