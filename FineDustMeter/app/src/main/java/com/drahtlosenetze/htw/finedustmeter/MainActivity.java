package com.drahtlosenetze.htw.finedustmeter;

import android.Manifest;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private SensorData data;
    private final int MY_SOCKET_TIMEOUT_MS = 5000;
    private final int MY_MAX_RETRY = 2;
    private final int MY_BACKOFF_MULTI = 2;
    private Timer timer = new Timer();
    RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView myAwesomeTextView = (TextView) findViewById(R.id.myAwesomeTextView);
        myAwesomeTextView.setText("No response yet");
        queue = Volley.newRequestQueue(this);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                logger.info("SendRequestToServer() will be called.");
                sendRequestToServer();   //Your code here
            }
        }, 0, 60 * 250);//0,25 Minutes
    }

    private void sendRequestToServer() {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, getResources().getString(R.string.url), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        logger.info("A response was returned: " + response.toString());
                        TextView myAwesomeTextView = (TextView) findViewById(R.id.myAwesomeTextView);

                        try {
                            JSONObject jObject = new JSONObject(response.toString());
                            Double pm25 = jObject.getDouble("pm25");
                            Double pm10 = jObject.getDouble("pm10");
                            data = new SensorData(pm10, pm25);
                            getServiceLocation();
                            myAwesomeTextView.setText("The data is: " + data.toString());
                        } catch (JSONException e) {
                            logger.warning("The parsin didn't work: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        TextView myAwesomeTextView = (TextView) findViewById(R.id.myAwesomeTextView);
                        myAwesomeTextView.setText("The HTTP Request failed.");
                        logger.warning("There was an error with the request: ");
                        error.printStackTrace();
                    }
                });
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                MY_MAX_RETRY,
                MY_BACKOFF_MULTI));

        queue.add(jsObjRequest);
    }


    private void getServiceLocation() {
        int permissionCheckCoarseLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheckFineLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheckCoarseLocation == 0 && permissionCheckFineLocation == 0) {
            logger.info("Permission is granted.");
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new MyLocationListener());

            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                logger.info("Location is not null.");
                Double lat, lon;
                try {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    data.setLatitude(lat);
                    data.setLongitude(lon);
                    logger.info("Geolocations set");
                } catch (Exception e) {
                    logger.warning("Retrieving geolocation failed: " + e.getMessage());
                }
            } else {
                logger.info("Location is null.");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new MyLocationListener());
            }
        } else {
            logger.info("Permission is not granted.");
            logger.warning("The user doesn't allow app to use geolocations.");
        }
    }


    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            /*
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();*/
            Double longitudeDouble = loc.getLongitude();
            String longitude = "Longitude: " + longitudeDouble;
            logger.info("Longitude: " + longitude);
            Double latituteDouble = loc.getLatitude();
            String latitude = "Latitude: " + latituteDouble;
            logger.info("Latitude: " + latitude);
            data.setLatitude(latituteDouble);
            data.setLongitude(longitudeDouble);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }


}
