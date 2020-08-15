package com.techkip.bebarider.common;


import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.facebook.accountkit.AccountKit;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.techkip.bebarider.Home;
import com.techkip.bebarider.model.DataMessage;
import com.techkip.bebarider.model.FCMResponse;
import com.techkip.bebarider.model.Rider;
import com.techkip.bebarider.model.Token;
import com.techkip.bebarider.remote.FCMClient;
import com.techkip.bebarider.remote.GoogleMapClient;
import com.techkip.bebarider.remote.IFCMService;
import com.techkip.bebarider.remote.IGoogleAPI;
import com.techkip.bebarider.remote.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ilitengenezwa na hillarie on 3/15/18.
 */

public class Common {

    public static final int PICK_IMAGE_REQUEST = 9999;
    public static  boolean isDriverFound = false;
    public static String driverId = "";
    public static String riderId = "";

    public static Rider currentUser = new Rider();
    public  static  final String DROPOFF_BROADCAST_STRING ="Drop_Off";
    public  static  final String CANCEL_BROADCAST_STRING="cancel_request";

    public static Location mLastLocation;
    public static final String driver_table = "Drivers"; //after driver signs up there details here i.e location
    public static final String user_driver_table = "DriversInformation";//after driver signs up
    public static final String user_rider_table = "RidersInformation";//after rider signs up
    public static final String pickuprequest_table = "PickupRequest";//location of a rider
    public static final String token_table = "Tokens";//location of a rider
    public static final String rate_detail_table = "RateDetails";


    public static final String user_field = "usr";
    public static final String pwd_field = "pwd";

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



    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static final String googleAPIUrl = "https://maps.googleapis.com";

    public static IFCMService getFCMService() {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

    public static IGoogleAPI getGoogleService() {
        return GoogleMapClient.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }
    public static IGoogleAPI getGoogleAPI() {
        return RetrofitClient.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }

    public static void sendRequestToDriver(String driverId, final IFCMService mService, final Context context, final Location currentLocation) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_table);
        tokens.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override

                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {

                            Token token = postSnapShot.getValue(Token.class);// get Token Object from data with  key

                            //make raw payload - convert latlng to json

                            String riderToken = FirebaseInstanceId.getInstance().getToken();

                            Map<String, String> content = new HashMap<>();
                            content.put("customer", riderToken);
                            content.put("riderId", riderId);
                            content.put("lat", String.valueOf(currentLocation.getLatitude()));
                            content.put("lng", String.valueOf(currentLocation.getLongitude()));
                            DataMessage dataMessage = new DataMessage(token.getToken(), content);


                            mService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {

                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                                            if (response.body().success == 1)

                                                Toast.makeText(context, "Request Sent! wait for Driver Response ", Toast.LENGTH_SHORT).show();


                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                                            Toast.makeText(context, "Unable to send your request retry!", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    public static void sendCancelRequestToDriver(String driverId, final IFCMService mService, final Context context, final Location currentLocation) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_table);
        tokens.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {

                    Token token = postSnapShot.getValue(Token.class);// get Token Object from data with  key

                    //make raw payload - convert latlng to json

                    String riderToken = FirebaseInstanceId.getInstance().getToken();

                    Map<String, String> content = new HashMap<>();
                    content.put("customer", riderToken);
                    content.put("riderId", riderId);
                    content.put("lat", String.valueOf(currentLocation.getLatitude()));
                    content.put("lng", String.valueOf(currentLocation.getLongitude()));
                    DataMessage dataMessage = new DataMessage(token.getToken(), content);


                    mService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {

                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                            if (response.body().success == 1)

                                Toast.makeText(context, "Request Sent! wait for Driver Response ", Toast.LENGTH_SHORT).show();


                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                            Toast.makeText(context, "Unable to send your request retry!", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
