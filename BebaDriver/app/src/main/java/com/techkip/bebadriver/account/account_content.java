package com.techkip.bebadriver.account;

/**
 * Created by hillarie on 12/7/2017.
 */
public class account_content {
    public   int id;
    public String driverId;
    public String driverFName;
    public String driverLName;
    public String driverLicenceId;
    public String driverPhone;
    public String driverImage;
    public String driverTotalDistance; //total distance
    public String driverTotalAmountEarned; //amount earned travelling
    public String driverTotalAmountDebt; //amount charged on mpesa distance *10
    public String driverTotalAmountPaid; //amount paid on mpesa
    public String driverTotalBalance; //amount charged- amount paid


    public account_content() {
    }

    public account_content(int id, String driverId, String driverFName, String driverLName, String driverLicenceId, String driverPhone, String driverImage, String driverTotalDistance, String driverTotalAmountEarned, String driverTotalAmountDebt, String driverTotalAmountPaid, String driverTotalBalance) {
        this.id = id;
        this.driverId = driverId;
        this.driverFName = driverFName;
        this.driverLName = driverLName;
        this.driverLicenceId = driverLicenceId;
        this.driverPhone = driverPhone;
        this.driverImage = driverImage;
        this.driverTotalDistance = driverTotalDistance;
        this.driverTotalAmountEarned = driverTotalAmountEarned;
        this.driverTotalAmountDebt = driverTotalAmountDebt;
        this.driverTotalAmountPaid = driverTotalAmountPaid;
        this.driverTotalBalance = driverTotalBalance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }


    public String getDriverFName() {
        return driverFName;
    }

    public void setDriverFName(String driverFName) {
        this.driverFName = driverFName;
    }


    public String getDriverLName() {
        return driverLName;
    }

    public void setDriverLName(String driverLName) {
        this.driverFName = driverLName;
    }


    public String getDriverLicenceId() {
        return driverLicenceId;
    }

    public void setDriverLicenceId(String driverLicenceId) {this.driverLicenceId = driverLicenceId;
    }


    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getDriverImage() {
        return driverImage;
    }

    public void setDriverImage(String driverImage) {
        this.driverImage = driverImage;
    }

    public String getDriverTotalDistance() {
        return driverTotalDistance;
    }

    public void setDriverTotalDistance(String driverTotalDistance) {
        this.driverTotalDistance = driverTotalDistance;
    }

    public String getDriverTotalAmountEarned() {
        return driverTotalAmountEarned;
    }

    public void setDriverTotalAmountEarned(String driverTotalAmountEarned) {
        this.driverTotalAmountEarned = driverTotalAmountEarned;
    }

    public String getDriverTotalAmountDebt() {
        return driverTotalAmountDebt;
    }

    public void setDriverTotalAmountDebt(String driverTotalAmountDebt) {
        this.driverTotalAmountDebt = driverTotalAmountDebt;
    }

    public String getDriverTotalAmountPaid() {
        return driverTotalAmountPaid;
    }

    public void setDriverTotalAmountPaid(String driverTotalAmountPaid) {
        this.driverTotalAmountPaid = driverTotalAmountPaid;
    }

    public String getDriverTotalBalance() {
        return driverTotalBalance;
    }

    public void setDriverTotalBalance(String driverTotalBalance) {
        this.driverTotalBalance = driverTotalBalance;
    }
}
