package com.drahtlosenetze.htw.finedustmeter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by l.r. on 30.12.17.
 */

public class SensorData {

    private double pm10;
    private double pm25;
    private double longitude;
    private double latitude;
    private long timestamp;


    public SensorData(double pm10, double pm25){
        this.pm10 = pm10;
        this.pm25 = pm25;
        this.timestamp = System.currentTimeMillis();
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String toString(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MMM yyyy HH:mm");
        Date resultdate = new Date(timestamp);
        return "PM10: " + pm10 + "\n PM25: " + pm25 + "\n Zeit: " + sdf.format(resultdate) + "\n latitude: " + latitude + "\n longitude: " + longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
