package com.example.gps_log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.gps_log.DBHelper;
import com.example.gps_log.Track;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Acts as an interface for the sqlite database
 */
public class TrackOperations {

    SQLiteOpenHelper dbhelper;
    SQLiteDatabase database;

    public static final String[] allColumns = {
            DBHelper.COLUMN_DEVID,
            DBHelper.COLUMN_TRIPID,
            DBHelper.COLUMN_LAT,
            DBHelper.COLUMN_LON,
            DBHelper.COLUMN_SPEED,
            DBHelper.COLUMN_TIME,
            DBHelper.COLUMN_SENT,
    };

    public TrackOperations(Context context) {
        dbhelper = new DBHelper(context);
    }

    //Opens the sqlite database
    public void openDatabase() {
        database = dbhelper.getWritableDatabase();
    }

    //Closes the sqlite database - shouldn't have to use this
    public void close() {
        dbhelper.close();
    }

    //Adds values pertaining to the passed track to the sqlite database
    public Track addTrack(Track track) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_DEVID, track.getDevID());
        values.put(DBHelper.COLUMN_TRIPID, track.getTripID());
        values.put(DBHelper.COLUMN_LAT, track.getLat());
        values.put(DBHelper.COLUMN_LON, track.getLon());
        values.put(DBHelper.COLUMN_SPEED, track.getSpeed());
        values.put(DBHelper.COLUMN_TIME, track.getTime());
        values.put(DBHelper.COLUMN_SENT, track.getSent());
        database.insert(DBHelper.TABLE_TRACKS, null, values);
        return track;
    }

    public JSONObject getUnsentTracks() {
        JSONObject finalJson = new JSONObject();
        Cursor cursor = database.query("tracks", allColumns, "sent=0", null, null, null, "time");

        int i = 0;
        while (cursor.moveToNext()) {
            JSONObject currentTrack = new JSONObject();
            try {
                currentTrack.put("dev_id", cursor.getString(0));
                currentTrack.put("trip_id", cursor.getString(1));
                currentTrack.put("lat", cursor.getString(2));
                currentTrack.put("lon", cursor.getString(3));
                currentTrack.put("speed", cursor.getString(4));
                currentTrack.put("time", cursor.getString(5));
                currentTrack.put("sent", cursor.getString(6));
                finalJson.put(""+i, currentTrack);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            i++;
        }
        cursor.close();
        return finalJson;
    }

    public JSONObject getAllTracks() {
        JSONObject finalJson = new JSONObject();
        Cursor cursor = database.query("tracks", allColumns, null, null, null, null, "time");

        int i = 0;
        JSONObject currentTrack = new JSONObject();
        while (cursor.moveToNext()) {
            try {
                currentTrack.put("dev_id", cursor.getString(0));
                currentTrack.put("trip_id", cursor.getString(1));
                currentTrack.put("lat", cursor.getString(2));
                currentTrack.put("lon", cursor.getString(3));
                currentTrack.put("speed", cursor.getString(4));
                currentTrack.put("time", cursor.getString(5));
                currentTrack.put("sent", cursor.getString(6));
                finalJson.put(""+i, currentTrack);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            i++;
        }
        cursor.close();

        return finalJson;
    }

    public JSONObject getTracksToSend() {
        JSONObject finalJson = new JSONObject();
        Cursor cursor = database.query("tracks", allColumns, "sent=0", null, null, null, "time");

        int i = 0;
        while (cursor.moveToNext()) {
            JSONObject currentTrack = new JSONObject();
            try {
                currentTrack.put("dev_id", cursor.getString(0));
                currentTrack.put("trip_id", cursor.getString(1));
                currentTrack.put("lat", cursor.getString(2));
                currentTrack.put("lon", cursor.getString(3));
                currentTrack.put("speed", cursor.getString(4));
                currentTrack.put("time", cursor.getString(5));
                currentTrack.put("sent", cursor.getString(6));
                finalJson.put(""+i, currentTrack);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            i++;
        }
        cursor.close();
        return finalJson;
    }

    //Marks each track in the sqlite database as sent - performed on server response when sending
    public void markAllTracksSent() {
        database.execSQL("UPDATE tracks SET sent = 1 WHERE sent = 0");
    }

    //Returns the most recent trip ID in the sqlite database plus 1
    public int getNextTrip() {
        int lastTrip;
        try {
            Cursor cursor = database.query("tracks", allColumns, null, null, null, null, "tripID DESC");
            cursor.moveToNext();
            lastTrip = cursor.getInt(1) + 1;
            cursor.close();
        }
        catch (android.database.CursorIndexOutOfBoundsException e) {
            Log.d("TRACKOPS", "No trips in database, assuming 1st trip");
            return 1;
        }

        return lastTrip;
    }

}
