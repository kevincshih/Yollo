package com.kcs.yollo;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import org.json.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener {


    protected static final int REQUEST_LOCATION_COARSE = 1;
    protected static final String TAG = "MainActivity";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Spinner element
        final Spinner spinnerCategory = (Spinner) findViewById(R.id.spinner);
        final Spinner spinnerRating = (Spinner) findViewById(R.id.spinner2);
        final Spinner spinnerDistance = (Spinner) findViewById(R.id.spinner3);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<>();
        categories.add(getString(R.string.category_cafe));
        categories.add(getString(R.string.category_campground));
        categories.add(getString(R.string.category_museum));
        categories.add(getString(R.string.category_park));
        categories.add(getString(R.string.category_restaurant));
        categories.add(getString(R.string.category_shopping_mall));
        List<String> ratings = new ArrayList<>();
        ratings.add(getString(R.string.rating_1));
        ratings.add(getString(R.string.rating_2));
        ratings.add(getString(R.string.rating_3));
        ratings.add(getString(R.string.rating_4));
        List<String> distances = new ArrayList<>();
        distances.add(getString(R.string.distance_5));
        distances.add(getString(R.string.distance_10));
        distances.add(getString(R.string.distance_20));
        distances.add(getString(R.string.distance_30));

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ratings);
        ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, distances);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinnerCategory.setAdapter(dataAdapter);
        spinnerRating.setAdapter(dataAdapter2);
        spinnerDistance.setAdapter(dataAdapter3);

        final Button yolloButton = (Button) findViewById(R.id.button);

        yolloButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String item = spinnerCategory.getSelectedItem().toString();
                String item2 = spinnerRating.getSelectedItem().toString();
                String item3 = spinnerDistance.getSelectedItem().toString();
                new YolloTask().execute(item, item2, item3);
            }
        });

        buildGoogleApiClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
            updateLocation();
    }

    private void updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_COARSE);
        }
        else{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation == null) {
                Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_COARSE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                }
            }
        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace, int radius) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        String l = "location=" + latitude + "," + longitude;
        String r = "&radius=" + radius;
        String t = "&type=" + nearbyPlace;
        googlePlacesUrl.append(l);
        googlePlacesUrl.append(r);
        googlePlacesUrl.append(t);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + BuildConfig.GOOGLE_PLACES_API_TOKEN);
        return (googlePlacesUrl.toString());
    }

    private String callURL(String myURL) {
        System.out.println("Requeted URL:" + myURL);
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn;
        InputStreamReader in = null;
        try {
            URL url = new URL(myURL);
            urlConn = url.openConnection();
            if (urlConn != null)
                urlConn.setReadTimeout(60 * 1000);
            if (urlConn != null && urlConn.getInputStream() != null) {
                try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(),Charset.defaultCharset()))){
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception while calling URL:"+ myURL, e);
        }

        return sb.toString();
    }


    private String getRandomPlaceFromYollo(String category, String rating, String distance){
        String result = "";
        String type;
        int radius;
        int rate;
        if(category.equals(getString(R.string.category_cafe))){
            type = "cafe";
        }
        else if (category.equals(getString(R.string.category_campground))){
            type = "campground";
        }
        else if (category.equals(getString(R.string.category_museum))){
            type = "museum";
        }
        else if (category.equals(getString(R.string.category_park))){
            type = "park";
        }
        else if (category.equals(getString(R.string.category_restaurant))){
            type = "restaurant";
        }
        else if (category.equals(getString(R.string.category_shopping_mall))){
            type = "shopping_mall";
        }
        else {
            return result;
        }
        if (rating.equals(getString(R.string.rating_1))){
            rate = 1;
        }
        else if (rating.equals(getString(R.string.rating_2))){
            rate = 2;
        }
        else if (rating.equals(getString(R.string.rating_3))){
            rate = 3;
        }
        else if (rating.equals(getString(R.string.rating_4))){
            rate = 4;
        }
        else {
            return result;
        }
        if (distance.equals(getString(R.string.distance_5))){
            radius = 8000;
        }
        else if (distance.equals(getString(R.string.distance_10))){
            radius = 16000;
        }
        else if (distance.equals(getString(R.string.distance_20))){
            radius = 32000;
        }
        else if (distance.equals(getString(R.string.distance_30))){
            radius = 48000;
        }
        else{
            return result;
        }
        result = getRandomPlace(type, rate, radius);
        return result;
    }

    private String getRandomPlace(String type, int rating, int radius){
        String result = "";
        String url = getUrl(mLastLocation.getLatitude(),mLastLocation.getLongitude(),type,radius);
        String response = callURL(url);
        try{
            JSONObject obj = new JSONObject(response);
            JSONArray arr = obj.getJSONArray("results");
            List<Integer> indices = new ArrayList<>();
            for(int i = 0; i<arr.length(); i++){
                try {
                    String n = arr.getJSONObject(i).getString("name");
                    int r = arr.getJSONObject(i).getInt("rating");
                    if (r >= rating) {
                        indices.add(i);
                    }
                }
                catch (Exception e){
                    Log.d("yollo", "Exception", e);
                }
            }
            if (indices.size()==0){
                return result;
            }
            int rnd = new Random().nextInt(indices.size());
            JSONObject selectedPlace = arr.getJSONObject(indices.get(rnd));
            result = selectedPlace.getString("name") + ' ' + selectedPlace.getString("vicinity");
            return result;
        }
        catch(Exception e){
            Log.d("yollo","Unhandled exception", e);
            return result;
        }
    }

    private void StartMapIntent(String query){
        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+Uri.encode(query));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    class YolloTask extends AsyncTask<String, String, String> {

        private Exception exception;

        protected String doInBackground(String... strings) {
            String result = "";
            try {
                String category = strings[0];
                String rating = strings[1];
                String distance = strings[2];
                result = getRandomPlaceFromYollo(category, rating, distance);
            } catch (Exception e) {
                this.exception = e;
                Log.d("yollo", "Exception", e);
            }
            return result;
        }

        protected void onPostExecute(String result) {
            if (result != null && !result.isEmpty()){
                Toast.makeText(findViewById(android.R.id.content).getContext(), result, Toast.LENGTH_LONG).show();
                StartMapIntent(result);
            }
            else{
                Toast.makeText(findViewById(android.R.id.content).getContext(), getString(R.string.no_results_found), Toast.LENGTH_LONG).show();
            }
        }

    }


}
