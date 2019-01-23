package com.example.gps_log;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    final int UNSTARTED = 0;
    final int WAITINGFORDATA = 1;
    final int STARTED = 2;
    final String CHANNEL_ID = "App Running Notification";
    final String SERVER_URL = "";

    Button startButton;
    Button stopButton;
    Button mapsButton;

    example.busrecoverytimes.TrackOperations trackOperations;
    RequestQueue requestQueue;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notificationBuilder;
    LocationManager locationManager;
    LocationListener locationListener;

    int lastHiddenState = 3; //Start at 3 to always send the first track
    int startStatus = UNSTARTED;
    private long tripNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start_logging);
        stopButton = (Button) findViewById(R.id.stop_logging);
        mapsButton = (Button) findViewById(R.id.go_to_map);

        //Set up tools needed to track, store, and send location data
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS is not enabled - prompt user
        }
        requestQueue = Volley.newRequestQueue(this);
        trackOperations = new example.busrecoverytimes.TrackOperations(getApplicationContext());
        trackOperations.openDatabase();
        tripNum = trackOperations.getNextTrip();
        //Prompt user for GPS permission if it is not already given
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        //Set up tools to notify user that app is running
        notificationManager = NotificationManagerCompat.from(this);
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Recording")
                .setContentText("Currently tracking, press stop when done")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //Provide functionality for buttons based on the current tracking state
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (startStatus) {
                    case UNSTARTED:
                        startButton.setText("Unstarted");
                        startStatus = WAITINGFORDATA;
                        tripNum = trackOperations.getNextTrip() + 1;
                        startService(new Intent(getBaseContext(), LocationService.class));
                        break;
                    case WAITINGFORDATA:
                        stopService(new Intent(getBaseContext(), LocationService.class));
                        startButton.setText("Unstarted");
                        startStatus = UNSTARTED;
                        break;
                    case STARTED:
                        locationManager.removeUpdates(locationListener);
                        sendDataToServer(trackOperations.getUnsentTracks(), SERVER_URL);
                        trackOperations.markAllTracksSent();
                        startButton.setText("Unstarted");
                        startStatus = UNSTARTED;
                        break;
                }
            }
        });
        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Make the tracks show with google maps
            }
        });
    }

    //Verifies connection to the url, then uses Volley to send the JSON object via POST to that url
    public void sendDataToServer (JSONObject json, String url) {
        int numberOfRows = json.length();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MAIN", "SERVER RESPONSE: " + response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MAIN", "SERVER RESPONSE ERROR: " + error.getMessage());
                    }
                })
        {   @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String,String> headers = new HashMap<String, String>();
            headers.put("Content-Type","application/json; charset=utf-8");
            return headers;
        }
        };
        requestQueue.add(jsonObjectRequest);
        Toast.makeText(getApplicationContext(), numberOfRows + " rows sent successfully, thank you!", Toast.LENGTH_LONG).show();
    }

    //Converts string array to JSON, adds device id and trip id to the JSON object
    public JSONObject jsonFormatSurveyResponses(String[] userResponses) {
        JSONObject formattedResponses = new JSONObject();
        for (int i = 0; i < userResponses.length; i++) {
            try {
                formattedResponses.put(""+Integer.toString(i),userResponses[i]);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            formattedResponses.put("serial", android.os.Build.SERIAL);
            formattedResponses.put("tripNum", tripNum-1);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return formattedResponses;
    }

    //Alerts the user that gps is disabled and prompts for change
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    //Returns a string containing the device's time
    private String getCurrentTime() {
        String time = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        time = df.format(c.getTime());
        return time;
    }
}
