package main.tl.com.timelogger.model;

import main.tl.com.timelogger.TripAdapter;

/**
 * Created by vipulmittal on 25/06/16.
 */
public class Trip implements TripAdapter.DisplayItem {
    private String date;
    private String name;
    private String key;

    public Trip() {
    }

    public Trip(String date, String name) {
        this.date = date;
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
