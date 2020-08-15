package com.techkip.bebarider;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.techkip.bebarider.common.Common;


import java.util.Calendar;
import java.util.HashMap;




public class TripDetailActivity extends FragmentActivity implements OnMapReadyCallback {


    String driverId;
    String id;
    String driverIdTag = "driver_id";
    String riderIdTag = "rider_id";
    String travelDistanceTag = "travel_distance";
    String travelFromTag = "travel_from";
    String travelToTag = "travel_to";
    String amountPaidTag = "amount_paid";
    String travelDateTag = "travel_date";

    ProgressDialog progressDialog;
    String GetDriverIdTv, GetRiderIdTv, GetTravelDistanceTv, GetTravelFromTv, GetTravelToTv, GetAmountPaidTv, GetTravelDateTv;



    private GoogleMap mMap;
    Button Complete,Ratee;

    private TextView TxtDate, TxtFee, TxtBaseFare, TxtTime, TxtDistance, TxtEstimatedPayout, TxtFrom, TxtTo,TxtDriverId,TxtRiderId;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("History");

        TxtDate = findViewById(R.id.tv_Date);
        TxtFee = findViewById(R.id.tv_Fee);
        TxtBaseFare = findViewById(R.id.tv_BaseFare);
        TxtTime = findViewById(R.id.tv_timetaken);
        TxtDistance = findViewById(R.id.tv_distance);
        TxtEstimatedPayout = findViewById(R.id.tv_estimatePayout);
        TxtFrom = findViewById(R.id.tv_From);
        TxtTo = findViewById(R.id.tv_To);
        TxtDriverId = findViewById(R.id.tv_driverId);
        TxtRiderId = findViewById(R.id.tv_riderId);
        Ratee = findViewById(R.id.btn_rate);
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                driverId =account.getId();
                TxtDriverId.setText(driverId);


            }
            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });

        Ratee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TripDetailActivity.this, RateActivity.class);
                startActivity(intent);
                finish();
            }
        });





    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.app_map)
            );
            if (!isSuccess)
                Log.d("Error", "Unable to load the map");

        } catch (Resources.NotFoundException E) {
            E.printStackTrace();
        }
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        settingInformation();
    }

    private void settingInformation() {
        if (getIntent() != null) {

            Calendar calendar = Calendar.getInstance();
            String date = String.format("%s,%d/%d/%d", convertToDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH),calendar.get(Calendar.YEAR));
            TxtDate.setText(date);

            TxtFee.setText(String.format("Ksh. %.2f", getIntent().getDoubleExtra("total", 0.0)));

            TxtEstimatedPayout.setText(String.format("%.2f", getIntent().getDoubleExtra("total", 0.0)));
            TxtBaseFare.setText(String.format("Ksh. %.2f", Common.base_fare));
            TxtTime.setText(String.format("%s min", getIntent().getStringExtra("time")));
            TxtDistance.setText(String.format("%s ", getIntent().getStringExtra("distance")));
            TxtFrom.setText(getIntent().getStringExtra("start_address"));
            TxtTo.setText(getIntent().getStringExtra("end_address"));
            TxtRiderId.setText(getIntent().getStringExtra("riderId"));

            //Add Marker
            String[] location_end = getIntent().getStringExtra("location_end").split(",");

            LatLng dropOff = new LatLng(Double.parseDouble(location_end[0]), Double.parseDouble(location_end[1]));

            mMap.addMarker(new MarkerOptions().position(dropOff)
                    .title("Drop off")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropOff, 12.0f));


        }

    }

    private String convertToDayOfWeek(int day) {
        switch (day) {
            case Calendar.SUNDAY:
                return "SUN";
            case Calendar.MONDAY:
                return "MON";
            case Calendar.TUESDAY:
                return "TUE";
            case Calendar.WEDNESDAY:
                return "WED";
            case Calendar.THURSDAY:
                return "THUR";
            case Calendar.FRIDAY:
                return "FRI";
            case Calendar.SATURDAY:
                return "SAT";
            default:
                return "DAY";
        }
    }

    @Override
    public void onBackPressed() {


    }


}
