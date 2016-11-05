package main.tl.com.timelogger.model;

/**
 * Created by vipulmittal on 15/10/16.
 */
public class Reservation {
    private String name;
    private String address;
    private int rooms;
    private double price;
    private String checkIn;
    private String checkOut;

    public Reservation() {
    }

    public Reservation(String name, String address, int rooms, double price, String checkIn, String checkOut) {
        this.name = name;
        this.address = address;
        this.rooms = rooms;
        this.price = price;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRooms() {
        return rooms;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public Reservation clone() {
        Reservation c = new Reservation();

        c.setRooms(rooms);
        c.setPrice(price);
        c.setName(name);
        c.setAddress(address);
        c.setCheckIn(checkIn);
        c.setCheckOut(checkOut);

        return c;
    }
}
