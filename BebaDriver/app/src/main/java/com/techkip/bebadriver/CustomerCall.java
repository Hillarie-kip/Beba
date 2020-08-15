package com.techkip.bebadriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.arsy.maps_library.MapRipple;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.techkip.bebadriver.common.Common;
import com.techkip.bebadriver.helper.DirectionJSONParser;
import com.techkip.bebadriver.model.DataMessage;
import com.techkip.bebadriver.model.FCMResponse;
import com.techkip.bebadriver.model.Token;
import com.techkip.bebadriver.remote.IFCMService;
import com.techkip.bebadriver.remote.IGoogleAPI;
import com.techkip.bebadriver.request.ShowAllRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;


    String riderLat, riderLng;

    public String customerId;


    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Marker riderMarker;
    private Marker driverMarker;

    Polyline direction;


    GeoFire geoFire;


    TextView TxtAddress,TxtTime, TxtDistance;
    Button btnCancel, btnAccept;
    MediaPlayer mediaPlayer;

    IGoogleAPI mService;
    IFCMService mFCMService;


    String lat, lng;
    String driverId;


    RelativeLayout RootLayout;

    private long timeCountInMilliSeconds = 30000;



    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;

    private ProgressBar progressBarCircle;
    private TextView textViewTime;
    private CountDownTimer countDownTimer;
    TextView textViewId;


    MapRipple mapRipple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_call);
        RootLayout = findViewById(R.id.rootlayout);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        progressBarCircle = (ProgressBar) findViewById(R.id.progressBarCircle);
        textViewTime = (TextView) findViewById(R.id.textViewTime);


        if (getIntent() != null) {
            riderLat = getIntent().getStringExtra("lat");
            riderLng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customerId");


        }


        setUpLocation();


        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        TxtTime = findViewById(R.id.txtTime);
        TxtDistance = findViewById(R.id.txtDistance);
        TxtAddress = findViewById(R.id.txtAddress);


        btnAccept = findViewById(R.id.btn_accept);
        btnCancel = findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
                   stopCountDownTimer();



            }
        });
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                driverId = account.getId();
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        DatabaseReference driverInfo = FirebaseDatabase.getInstance().getReference(Common.user_driver_table);
                        driverInfo.child(account.getId()).child("availability").setValue("Busy");
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });

                if (!TextUtils.isEmpty(customerId))
                    acceptBooking(customerId);

                stopCountDownTimer();

                if (TxtDistance.equals("Distance")) {
                    Toast.makeText(CustomerCall.this, "wait to load info", Toast.LENGTH_SHORT).show();

                } else {
                    Intent intent = new Intent(CustomerCall.this, DriverTracking.class);
                    //send location of user
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
                    intent.putExtra("customerId", customerId);
                    startActivity(intent);
                    finish();
                }
            }
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.wiz);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent() != null) {
            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customer");

            getDirection(lat, lng);


        }

        startCountDownTimer();

    }





    private void startCountDownTimer() {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));

                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));

            }

            @Override
            public void onFinish() {

                textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
                // call to initialize the progress bar values
                setProgressBarValues();
                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED;
                if (!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
                else
                    Toast.makeText(CustomerCall.this, "Customer Must have an ID", Toast.LENGTH_SHORT).show();

            }

        }.start();
        countDownTimer.start();
    }

    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }

    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }
    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;


    }


    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);
        //  Notification notification = new Notification("Cancel", "Driver has cancelled your request ");
        //  Sender sender = new Sender(token.getToken(), notification);
        Map<String, String> content = new HashMap<>();

        content.put("title", "Cancel");
        content.put("message", "Driver has cancelled your request ");
        DataMessage dataMessage = new DataMessage(token.getToken(), content);
        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1) {
                            Toast.makeText(CustomerCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });


    }

    private void acceptBooking(String customerId) {
        Token token = new Token(customerId);
        //  Notification notification = new Notification("Cancel", "Driver has cancelled your request ");
        //  Sender sender = new Sender(token.getToken(), notification);
        Map<String, String> content = new HashMap<>();

        content.put("title", "Accept");
        content.put("message", "request accepted Driver will arrived in apx.:" + TxtTime.getText());
        content.put("time", (String) TxtTime.getText());
        content.put("driverPic", Common.currentDriver.getProfilePicUrl());
        content.put("carPic", Common.currentDriver.getCarPicUrl());
        content.put("driverName", Common.currentDriver.getName());
        content.put("driverPhone", Common.currentDriver.getPhone());
        content.put("carPlate", Common.currentDriver.getCarPlate());
        DataMessage dataMessage = new DataMessage(token.getToken(), content);
        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1) {
                            Toast.makeText(CustomerCall.this, "you have accepted request", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });


    }

    private void getDirection(final String lat, String lng) {

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions" +
                    "/json?mode=driving&transit_routing_preference=less_driving&origin=" + Common.mLastLocation.getLatitude() + "," + Common.mLastLocation.getLongitude() +
                    "&destination=" + lat + "," + lng +
                    "&key=" + getResources().getString(R.string.google_direction_api);//browser api key

            Log.d("Hillarie", requestApi);
            //PRINT Url for debug

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");

                                JSONObject obj = routes.getJSONObject(0);
                                JSONArray legs = obj.getJSONArray("legs");
                                JSONObject legsObj = legs.getJSONObject(0);

                                JSONObject distance = legsObj.getJSONObject("distance");
                                TxtDistance.setText(distance.getString("text"));

                                JSONObject time = legsObj.getJSONObject("duration");
                                TxtTime.setText(time.getString("text"));

                                String addressfrom = legsObj.getString("end_address");
                                TxtAddress.setText(addressfrom);






                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustomerCall.this, "Google Map Error :" + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStop() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying())
            mediaPlayer.start();

    }


    private void setUpLocation() {
        buildLocationRequest();
        buildLocationCallBack();
        displayLocation();


    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }


    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Common.mLastLocation = location;
                }
                displayLocation();

            }
        };

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

        mMap.setOnInfoWindowClickListener(this);

        riderMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(riderLat), Double.parseDouble(riderLng)))
                .title("view Rider Info")
             //   .snippet(lat + " " + lng)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_marker)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(riderLat), Double.parseDouble(riderLng)), 15.0f));


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH){
            mapRipple = new MapRipple(mMap, new LatLng(Double.parseDouble(riderLat), Double.parseDouble(riderLng)), this);
            mapRipple.withNumberOfRipples(1);
            mapRipple.withFillColor(getResources().getColor(R.color.yellow));
            mapRipple.withStrokeColor(getResources().getColor(R.color.colorAccent));
            mapRipple.withStrokewidth(10);      // 10dp
            mapRipple.withDistance(100);      // 1000 metres radius
            mapRipple.withRippleDuration(20000);    //12000ms
            mapRipple.withTransparency(0.5f);
            mapRipple.startRippleMapAnimation();
        }

        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_table).child(Common.currentDriver.getCarType()));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(riderLat), Double.parseDouble(riderLng)), 0.05f);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

              /*  sendArrivedNotification(customerId);
                BtnStartTrip.setEnabled(true);
                BtnStartTrip.setText("Click to Start Trip");*/


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildLocationCallBack();
        buildLocationRequest();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Common.mLastLocation = location;
                if (Common.mLastLocation != null) {

                    final double latitude = Common.mLastLocation.getLatitude();
                    final double longitude = Common.mLastLocation.getLongitude();


                    if (driverMarker != null)
                        driverMarker.remove();

                    if (Common.currentDriver.getCarType().equals("Beba Bronze")) {

                        driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .title("You")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_bronze)));


                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));

                    }

                    if (Common.currentDriver.getCarType().equals("Beba Silver")) {

                        driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .title("You")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_silver)));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));

                    }
                    if (Common.currentDriver.getCarType().equals("Beba Gold")) {

                        driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .title("You")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_gold)));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));

                    }
                    if (Common.currentDriver.getCarType().equals("Beba Boda")) {

                        driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .title("You")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bike)));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));

                    }
                    if (direction != null)
                        direction.remove();//remove old direction
                    getDirection();
                  //  getDirectionTo();


                } else {
                    Log.d("Beba :", "cannot get your location");
                }
            }
        });


    }


    private void getDirection() {
        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions" +
                    "/json?mode=driving&transit_routing_preference=less_driving&origin=" + currentPosition.latitude + "," + currentPosition.longitude +
                    "&destination=" + riderLat + "," + riderLng +
                    "&key=" + getResources().getString(R.string.google_direction_api);//browser api key

            Log.d("Hillare", requestApi);
            //PRINT Url for debug

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {

                                new CustomerCall.ParseTask().execute(response.body().toString());


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustomerCall.this, "Error : " + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        // CustomerInfo();
        //Toast.makeText(this, driverId, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ShowAllRequest.class);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        intent.putExtra("iddriver", driverId);
        startActivity(intent);

    }


    private class ParseTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        ProgressDialog progressDialog = new ProgressDialog(CustomerCall.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading Map....");
            progressDialog.show();

        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();//we get from googleDirectionJSONParser.java
                routes = parser.parse(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();

            }
            return routes;

        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            progressDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions = null;
            for (int i = 0; i < lists.size(); i++) {

                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);


                }
                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);

            }
            direction = mMap.addPolyline(polylineOptions);

        }
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(customerId))
            cancelBooking(customerId);
        super.onBackPressed();

    }
}
