package com.kcs.yollo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, ConnectionCallbacks, OnConnectionFailedListener {


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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Spinner element
        final Spinner spinnerCategory = (Spinner) findViewById(R.id.spinner);
        final Spinner spinnerRating = (Spinner) findViewById(R.id.spinner2);
        final Spinner spinnerDistance = (Spinner) findViewById(R.id.spinner3);


        // Spinner click listener
        spinnerCategory.setOnItemSelectedListener(this);
        spinnerRating.setOnItemSelectedListener(this);
        spinnerDistance.setOnItemSelectedListener(this);


        // Spinner Drop down elements
        List<String> categories = new ArrayList<>();
        categories.add(getString(R.string.category_parks));
        categories.add(getString(R.string.category_restaurants));
        categories.add(getString(R.string.category_shopping));
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
        final Button showAllButton = (Button) findViewById(R.id.button2);

        yolloButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String item = spinnerCategory.getSelectedItem().toString();
                String item2 = spinnerRating.getSelectedItem().toString();
                String item3 = spinnerDistance.getSelectedItem().toString();
                new YolloTask().execute(item, item2, item3);
                //Toast.makeText(v.getContext(), "Yollo: " + item + ", " + item2 + ", " + item3, Toast.LENGTH_LONG).show();
                //Toast.makeText(v.getContext(), getRandomPlaceFromYollo(item, item2, item3), Toast.LENGTH_LONG).show();
            }
        });

        showAllButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String item = spinnerCategory.getSelectedItem().toString();
                String item2 = spinnerRating.getSelectedItem().toString();
                String item3 = spinnerDistance.getSelectedItem().toString();
                Toast.makeText(v.getContext(), "Show All: " + item + ", " + item2 + ", " + item3, Toast.LENGTH_LONG).show();
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
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
        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                String latitude = String.format("%f", mLastLocation.getLatitude());
                String longitude = String.format("%f", mLastLocation.getLongitude());
                Toast.makeText(this, latitude + "," + longitude, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
            }
        }
        catch(SecurityException e){
            Toast.makeText(this, R.string.security_exception, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
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

    private String getUrl(double latitude, double longitude, String nearbyPlace, int radius) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + radius);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyATuUiZUkEc_UgHuqsBJa1oqaODI-3mLs0");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    private String callURL(String myURL) {
        System.out.println("Requeted URL:" + myURL);
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(myURL);
            urlConn = url.openConnection();
            if (urlConn != null)
                urlConn.setReadTimeout(60 * 1000);
            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(),
                        Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
            }
            in.close();
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
        if(category == getString(R.string.category_parks)) {
            type = "park";
        }
        else if (category == getString(R.string.category_restaurants)) {
            type = "food";
        }
        else if (category == getString(R.string.category_shopping)) {
            type = "store";
        }
        else {
            return result;
        }
        if (rating == getString(R.string.rating_1)){
            rate = 1;
        }
        else if (rating == getString(R.string.rating_2)){
            rate = 2;
        }
        else if (rating == getString(R.string.rating_3)){
            rate = 3;
        }
        else if (rating == getString(R.string.rating_4)){
            rate = 4;
        }
        else {
            return result;
        }
        if (distance == getString(R.string.distance_5)){
            radius = 8000;
        }
        else if (distance == getString(R.string.distance_10)){
            radius = 16000;
        }
        else if (distance == getString(R.string.distance_20)){
            radius = 32000;
        }
        else if (distance == getString(R.string.distance_30)){
            radius = 48000;
        }
        else{
            return result;
        }
        result = getRandomPlace(type, rate, radius);
        return result;
    }

    private String getRandomPlace(String type, int rating, int radius) {
        String result = "";
        String url = getUrl(mLastLocation.getLatitude(),mLastLocation.getLongitude(),type,radius);
        String response = callURL(url);
        try{
            JSONObject obj = new JSONObject(response);
            JSONArray arr = obj.getJSONArray("results");
            List<Integer> indices = new ArrayList<Integer>();
            for(int i = 0; i<arr.length(); i++){
                try {
                    String n = arr.getJSONObject(i).getString("name");
                    int r = arr.getJSONObject(i).getInt("rating");
                    if (r >= rating) {
                        indices.add(i);
                    }
                    Log.d("yollo", "Name=" + n + " Rating=" + String.valueOf(r) + " Total=" + String.valueOf(indices.size()));
                }
                catch (Exception e){
                    String s = arr.getJSONObject(i).toString();
                    Log.d("yollo", "s=" + s);
                    Log.d("yollo", "Exception", e);
                }
            }
            if (indices.size()==0){
                return result;
            }
            int rnd = new Random().nextInt(indices.size());
            result = arr.getJSONObject(indices.get(rnd)).getString("name");
            return result;
        }
        catch(Exception e){
            Log.d("yollo","Unhandled exception", e);
            return result;
        }
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
            }
            return result;
        }

        protected void onPostExecute(String result) {
            Toast.makeText(findViewById(android.R.id.content).getContext(), result, Toast.LENGTH_LONG).show();
        }

    }


}
