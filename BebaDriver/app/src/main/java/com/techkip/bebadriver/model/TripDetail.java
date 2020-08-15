package com.techkip.bebadriver.model;

/**
 * ilitengenezwa na hillarie on 3/13/18.
 */

public class TripDetail {
    private String driverId,riderId,date,amount,distance,from,to;

    public TripDetail() {
    }

    public TripDetail(String driverId, String riderId, String date, String amount, String distance, String from, String to) {
        this.driverId = driverId;
        this.riderId = riderId;
        this.date = date;
        this.amount = amount;
        this.distance = distance;
        this.from = from;
        this.to = to;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}