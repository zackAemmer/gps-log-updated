package com.example.gps_log;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

/**
 * This class outlines the structure of the sqlite database to be implemented by TrackOperations
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "transitInterference.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_TRACKS = "tracks";
    public static final String COLUMN_DEVID = "devID";
    public static final String COLUMN_TRIPID = "tripID";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LON = "lon";
    public static final String COLUMN_SPEED = "speed";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_SENT = "sent";

    public static final String TABLE_CREATE =
            "CREATE TABLE " +TABLE_TRACKS +" (" +
                    COLUMN_DEVID +" TEXT, " +
                    COLUMN_TRIPID +" INT, " +
                    COLUMN_LAT +" TEXT, " +
                    COLUMN_LON +" TEXT, " +
                    COLUMN_SPEED +" TEXT, " +
                    COLUMN_TIME +" TEXT, " +
                    COLUMN_SENT +" INT" +
                    ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +TABLE_TRACKS);
        db.execSQL(TABLE_CREATE);
    }

}
