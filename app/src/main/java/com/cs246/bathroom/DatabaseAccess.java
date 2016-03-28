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

    /**Class used to access the MYSQL database for users credentials and map markers
     * Created by tyler on 2/26/16.
     */
    public class DatabaseAccess {
        public static boolean done = true;
        //PHP database names
        public static final String KEY_NAME = "name";
        public static final String KEY_LAT = "lat";
        public static final String KEY_LNG = "lng";
        public static final String KEY_STARS = "stars";
        public static final String KEY_HAND = "handi";
        public static final String KEY_CHANGE = "change";
        public static final String KEY_COMMENTS = "comments";
        //Gathered from constructor
        String name;
        double lat;
        double lng;
        boolean isPost;
        Bundle addInfo = null;
        //For Post method
        String response = null;
        //For Get Method
        //Marker Variables
        public static String[] markerName;
        public static double[] markerLat;
        public static double[] markerLng;
        public static int[] markerStars;
        public static String[] markerHand;
        public static String[] markerChange;
        public static String[] markerComments;
        public static int numMarkers;
        //User Variables
        public static int numUsers;
        public static String[] usernames;
        public static String[] userPass;

        /**
         * Database access constructor
         * @param name
         * @param position
         * @param isPost
         */
        DatabaseAccess(String name, LatLng position, boolean isPost) {
            done = false;
            this.name = name;
            this.lat = position.latitude;
            this.lng = position.longitude;
            this.isPost = isPost;
        }

        /**
         * Non Default Database constructor for extra marker info
         * @param name
         * @param position
         * @param isPost
         * @param addInfo
         */
        DatabaseAccess(String name, LatLng position, boolean isPost, Bundle addInfo) {
            done = false;
            this.name = name;
            this.lat = position.latitude;
            this.lng = position.longitude;
            this.addInfo = addInfo;
            this.isPost = isPost;
        }

        /**
         * Performs get and post on a secondary thread
         */
        public void runAction() {
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

        /**
         * Method for Post actions to database/saving new markers
         * @return
         */
        public String post() {
            try {
                if(addInfo == null)
                    postNoAdd();
                else {
                    postAdd();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Method used to get saved markers and users credentials
         * @return
         */
        public int getMethod() {

            try {
                if (name.equals("login")) {
                    numUsers = 0;
                    usernames = new String[50];
                    userPass = new String[50];
                    return getUser();
                }
                else {
                    markerName = new String[50];
                    markerLat = new double[50];
                    markerLng = new double[50];
                    markerStars = new int[50];
                    markerChange = new String[50];
                    markerHand = new String[50];
                    markerComments = new String[50];
                    numMarkers = 0;
                    return getMarkers();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        /**
         * Performs get method for marker locations
         * @return
         * @throws IOException
         */
        public int getMarkers() throws IOException {
            String requestMarkers = "http://php-tsorenson.rhcloud.com/androidGet.php";
            URL url;
            url = new URL(requestMarkers);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String line = null;
            while( (line = br.readLine()) != null){
                readMarkers(line);
            }
            conn.disconnect();
            return numMarkers;
        }

        /**
         * Performs get method for user credentials
         * @return
         * @throws IOException
         */
        public int getUser() throws IOException {
            String requestUsers = "http://php-tsorenson.rhcloud.com/getUsers.php";
            URL url;
            url = new URL(requestUsers);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String line = null;
            while( (line = br.readLine()) != null){
                readUsers(line);
            }
            conn.disconnect();
            return numUsers;
        }

        /**
         * Performs post method with additional information
         * @throws IOException
         */
        public void postAdd() throws IOException {
            String urlParameters = KEY_NAME + "=" + name + "&" +
                    KEY_LAT + "=" + lat + "&" + KEY_LNG + "=" + lng  + "&" +
                    KEY_STARS + "=" + addInfo.getInt("stars") + "&" +
                    KEY_HAND + "=" + addInfo.getString("hand") + "&" +
                    KEY_CHANGE + "=" + addInfo.getString("change") + "&" +
                    KEY_COMMENTS + "=" + addInfo.getString("comments");
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
        }

        /**
         * Performs post method without additional information
         * @throws IOException
         */
        public void postNoAdd() throws IOException{
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
        }

        /**
         * Takes the line in from the reader and parses it into Google maps markers
         * @param line
         */
        public void readMarkers(String line) {
            String[] marker = line.split(",");
            marker[6] = marker[6].replace(";<br />", " ");
            storeMarker(marker);
            numMarkers++;
        }

        /**
         * Takes the line in from the reader and parses it into user variables
         * @param line
         */
        public void readUsers(String line) {
            String[] user = line.split(",");
            user[1] = user[1].replace(";<br />", "");
            storeUsers(user);
            numUsers++;
        }

        /**Method used to store the read markers from the DB to the static variables
         * for later access. No return value.
         *
         * @param marker
         */
        public void storeMarker(String[] marker) {
            markerName[numMarkers] = marker[0];
            markerLat[numMarkers] = Double.parseDouble(marker[1]);
            markerLng[numMarkers] = Double.parseDouble(marker[2]);
            markerStars[numMarkers] = Integer.valueOf(marker[3]);
            markerChange[numMarkers] = marker[4];
            markerHand[numMarkers] = marker[5];
            markerComments[numMarkers] = marker[6];
        }

        /**
         * stores the parsed data into static variables for universal access
         * @param users
         */
        public void storeUsers(String[] users) {
            usernames[numUsers] = users[0];
            userPass[numUsers] = users[1];
        }
    }
