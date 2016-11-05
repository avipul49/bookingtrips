package main.tl.com.timelogger.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

import main.tl.com.timelogger.TripAdapter;

/**
 * Created by vipulmittal on 25/06/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trip implements TripAdapter.DisplayItem {
    private String startDate;
    private String name;
    private String key;
    private ArrayList<City> cities;
    private int personsCount = 1;
    private boolean selected;

    public Trip() {
    }

    public Trip(String date, String name) {
        this.startDate = date;
        this.name = name;
    }

    public int getPersonsCount() {
        return personsCount;
    }

    public void setPersonsCount(int personsCount) {
        this.personsCount = personsCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
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

    public ArrayList<City> getCities() {
        return cities;
    }

    public void setCities(ArrayList<City> cities) {
        this.cities = cities;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public double getTotalCost() {
        double temp = 0;
        if (cities != null)
            for (City city : cities) {
                if (city.getReservation() != null)
                    temp += city.getReservation().getPrice();
            }
        return temp;
    }
}
