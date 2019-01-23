package com.example.gps_log;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LocationService extends Service {
    final int TRUE = 1;
    final int FALSE = 0;
    final int ACCELERATION = 1;
    final int FREEFLOW = 2;
    final int STOPPED = 0;
    private int currentState = 3; //Always send first track
    private int previousState = 3;

    private static final String TAG = "GPSService";
    final String CHANNEL_ID = "App Running Notification";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 2000;
    private static final float LOCATION_DISTANCE = 0;
    private int tripNum;
    private example.busrecoverytimes.TrackOperations trackOperations = new example.busrecoverytimes.TrackOperations(this);
    private HMMClassifier hmmClassifier = new HMMClassifier();

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            previousState = currentState;
            currentState = hmmClassifier.getDiscreteSpeed(location.getSpeed());
            Track track = new Track();
            track.setDevID(android.os.Build.SERIAL);
            track.setTripID(tripNum);
            track.setLat(location.getLatitude() + "");
            track.setLon(location.getLongitude() + "");
            track.setSpeed(location.getSpeed() + "");
            track.setTime(getCurrentTime());
            track.setSent(0);
            track.setToSend(determineToSend(previousState, currentState, getCurrentTime()));
            trackOperations.addTrack(track);

            Log.i("MAIN", "TRACK ADDED TO DB");
            System.out.println("" + track.toString());
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
            Toast.makeText(getApplicationContext(), "Please Re-enable GPS to keep tracking", Toast.LENGTH_LONG);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        trackOperations.openDatabase();
        tripNum = trackOperations.getNextTrip();
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    //Determines whether a track should be sent based on HMM properties
    private int determineToSend(int lastHiddenState, int hiddenState, String time) {
        int toSend;
        char lastChar = time.charAt(time.length() - 1);
        if(lastHiddenState == STOPPED && hiddenState == STOPPED){
            toSend = FALSE;
            //sends first and last stopped
        }
        else if(lastHiddenState == FREEFLOW && hiddenState == FREEFLOW && ( lastChar != '0')){
            toSend = FALSE;
            //sends 1 in 10 freeflow
        }
        else{
            toSend = TRUE;
            //sends all acceleration
        }
        return toSend;
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