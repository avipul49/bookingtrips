package main.tl.com.timelogger.model;

import java.math.BigDecimal;
import java.util.ArrayList;

import main.tl.com.timelogger.TripAdapter;

/**
 * Created by vipulmittal on 28/06/16.
 */
public class WeekDetail implements TripAdapter.DisplayItem {
    private int year;
    private int weekOfYear;
    private String startDate;
    private String endDate;
    private double totalDistance;
    private double totalTime;
    private int numberOfEntries;
    private ArrayList<Trip> timeEntries = new ArrayList<>();

    public ArrayList<Trip> getTimeEntries() {
        return timeEntries;
    }

    public void setTimeEntries(ArrayList<Trip> timeEntries) {
        this.timeEntries = timeEntries;
    }

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    public void setNumberOfEntries(int numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    public void setWeekOfYear(int weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getSpeed() {
        Double toBeTruncated = new Double(totalDistance / totalTime);
        Double truncatedDouble = new BigDecimal(toBeTruncated)
                .setScale(3, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return truncatedDouble;
    }
}
