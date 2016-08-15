package main.tl.com.timelogger.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

import main.tl.com.timelogger.TimeListAdapter;

/**
 * Created by vipulmittal on 25/06/16.
 */
public class TimeEntry implements TimeListAdapter.DisplayItem {
    private String date;
    private double distance;
    private double time;
    private String key;

    public TimeEntry() {
    }

    public TimeEntry(String date, double distance, double time) {
        this.date = date;
        this.distance = distance;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    @JsonIgnore
    public double getSpeed() {
        Double toBeTruncated = new Double(distance / time);
        Double truncatedDouble = new BigDecimal(toBeTruncated)
                .setScale(3, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return truncatedDouble;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
