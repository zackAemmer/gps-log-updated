package com.example.gps_log;

/**
 * Basic object that is being created each time locationlistener updates, variables should match columns
 * in sqlite and on server.
 */
public class Track {
    private String devID;
    private int tripID;
    private String lat;
    private String lon;
    private String speed;
    private String time;
    private int sent; // 0:no 1:yes

    public Track(String devid, int tripid, String lat, String lon, String speed, String time, int sent) {
        this.devID = devid;
        this.tripID = tripid;
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.time = time;
        this.sent = sent;
    }

    public Track() {

    }

    public String getDevID() {
        return devID;
    }

    public void setDevID(String devID) {
        this.devID = devID;
    }

    public int getTripID() {
        return tripID;
    }

    public void setTripID(int tripID) {
        this.tripID = tripID;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSent() { return sent; }

    public void setSent(int sent) {
        this.sent = sent;
    }



    @Override
    public String toString() {
        return "Dev id: " +getDevID() +
                " Trip id: " +getTripID() +
                " lat: " +getLat() +
                " lon : " +getLon() +
                " speed: " +getSpeed() +
                " time: " +getTime() +
                " sent: " +getSent();
    }
}
