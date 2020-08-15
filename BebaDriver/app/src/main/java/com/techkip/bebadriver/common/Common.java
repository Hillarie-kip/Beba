package com.techkip.bebadriver.common;


import android.location.Location;

import com.techkip.bebadriver.model.Driver;
import com.techkip.bebadriver.remote.FCMClient;
import com.techkip.bebadriver.remote.IFCMService;
import com.techkip.bebadriver.remote.IGoogleAPI;
import com.techkip.bebadriver.remote.RetrofitClient;

/**
 * ilitengenezwa na hillarie on 3/15/18.
 */

public class Common {




    public static final String driver_table = "Drivers"; //after driver signs up there details here i.e location
    public static final String user_driver_table = "DriversInformation";//after driver signs up
    public static final String user_rider_table = "RidersInformation";//after rider signs up
    public static final String pickuprequest_table = "PickupRequest";//location of a rider
    public static final String token_table = "Tokens";//location of a rider

    public static final String rate_detail_table = "RateDetails";



    public static Driver currentDriver;
    public static String RiderIdHolder="";

    public static Location mLastLocation = null;


    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";



    public static final int GALLERY_REQUEST_ONE = 9999;
    public static final int GALLERY_REQUEST_TWO = 1000;

    public   static double base_fare =85; //base of uber in new york 2.55 0.35 1.75
    private  static double time_rate =4;
    private  static double distance_rateGold =45;
    private  static double distance_rateSilver =30;
    private  static double distance_rateBronze =20;
    private  static double distance_rateboda =30;

    public  static double formulaCostSilver(double km , double min){

        return (base_fare+(distance_rateSilver*km)+(time_rate*min));
    }
    public  static double formulaCostBronze(double km , double min){

        return (base_fare+(distance_rateBronze*km)+(time_rate*min));
    }
    public  static double formulaCostGold(double km , double min){

        return (base_fare+(distance_rateGold*km)+(time_rate*min));
    }
    public  static double formulaCostBoda(double km ,double min){

        return (base_fare+(distance_rateboda*km)+(time_rate*min));
    }

    public static IGoogleAPI getGoogleAPI() {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
    public static IFCMService getFCMService() {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }




}
