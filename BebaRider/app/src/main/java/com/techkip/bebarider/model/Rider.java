package com.techkip.bebarider.model;

/**
 * Created by hillarie on 09/04/2018.
 */

public class Rider {
    private String availability, Name, Phone, profilePicUrl, rates, carPicUrl, carType, carSeats, carPlate;

    public Rider() {

    }



    public Rider(String name, String phone, String profilePicUrl, String rates, String carPicUrl, String carType, String carSeats, String carPlate, String availability) {


        Name = name;
        Phone = phone;
        this.profilePicUrl = profilePicUrl;
        this.rates = rates;
        this.carPicUrl = carPicUrl;
        this.carType = carType;
        this.carSeats = carSeats;
        this.carPlate = carPlate;
        this.availability = availability;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getCarPicUrl() {
        return carPicUrl;
    }

    public void setCarPicUrl(String carPicUrl) {
        this.carPicUrl = carPicUrl;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getCarSeats() {
        return carSeats;
    }

    public void setCarSeats(String carSeats) {
        this.carSeats = carSeats;
    }

    public String getCarPlate() {
        return carPlate;
    }

    public void setCarPlate(String carPlate) {
        this.carPlate = carPlate;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }
}

