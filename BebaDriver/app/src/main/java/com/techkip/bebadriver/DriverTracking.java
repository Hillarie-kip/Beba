package com.techkip.bebadriver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DriverTracking extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;


    String riderLat, riderLng;

    String customerId;
    String id;

    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Circle riderMarker;
    private Marker driverMarker;

    Polyline direction;

    IGoogleAPI mService;
    IFCMService mFCMService;

    GeoFire geoFire;

    Button BtnStartTrip;

    Location pickUpLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
       fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (getIntent() != null) {
            riderLat = getIntent().getStringExtra("lat");
            riderLng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customerId");


        }
        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();
        setUpLocation();

        BtnStartTrip = findViewById(R.id.btn_StartTrip);
        BtnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BtnStartTrip.getText().equals("Click to Start Trip")) {

                    pickUpLocation = Common.mLastLocation;

                    BtnStartTrip.setText("DROP OFF HERE");


                } else if (BtnStartTrip.getText().equals("DROP OFF HERE")) {
                    BtnStartTrip.setEnabled(true);
                    if (Common.currentDriver.getCarType().equals("Beba Bronze")){
                        calculateCashFeeBronze(pickUpLocation, Common.mLastLocation);
                    }
                    if (Common.currentDriver.getCarType().equals("Beba Silver")){
                        calculateCashFeeSilver(pickUpLocation, Common.mLastLocation);
                    }
                    if (Common.currentDriver.getCarType().equals("Beba Gold")){
                        calculateCashFeeGold(pickUpLocation, Common.mLastLocation);
                    }

                    if (Common.currentDriver.getCarType().equals("Beba Boda")){
                        calculateCashFeeBoda(pickUpLocation, Common.mLastLocation);
                    }


                }
            }
        });


    }

    private void calculateCashFeeBronze(final Location pickUpLocation, Location mLastLocation) {

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions" +
                    "/json?mode=driving&transit_routing_preference=less_driving&origin=" + pickUpLocation.getLatitude() + "," + pickUpLocation.getLongitude() +
                    "&destination=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() +
                    "&key=" + getResources().getString(R.string.google_direction_api);//browser api key
            Log.d("Kushy", requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                //extract Json
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");
                                JSONObject legsObject = legs.getJSONObject(0);

                                //get distance

                                JSONObject distance = legsObject.getJSONObject("distance");
                                String distance_text = distance.getString("text");

                                //use regex to extract double from string remove all text;

                                Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));

                                JSONObject timeObj = legsObject.getJSONObject("duration");
                                String time_text = timeObj.getString("text");

                                //use regex to extract double from string remove all text; to take number from string to parse
                                Double time_value = Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+", ""));

                                sendDropOffNotification(customerId);


                                Intent intent = new Intent(DriverTracking.this, TripDetailActivity.class);
                                intent.putExtra("start_address", legsObject.getString("start_address"));
                                intent.putExtra("end_address", legsObject.getString("end_address"));
                                intent.putExtra("time", String.valueOf(time_value));
                                intent.putExtra("distance", String.valueOf(distance_value));
                                intent.putExtra("total", Common.formulaCostBronze(distance_value, time_value));
                                intent.putExtra("location_start", String.format("%f,%f", pickUpLocation.getLatitude(), pickUpLocation.getLongitude()));
                                intent.putExtra("location_end", String.format("%f,%f", Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                    @Override
                                    public void onSuccess(Account account) {
                                         id=account.getId();
                                    }

                                    @Override
                                    public void onError(AccountKitError accountKitError) {

                                    }
                                });
                                intent.putExtra("driverId",id);
                                intent.putExtra("riderId", Common.RiderIdHolder);

                                startActivity(intent);
                                finish();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }



                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, "Map Error :" + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void calculateCashFeeSilver(final Location pickUpLocation, Location mLastLocation) {

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions" +
                    "/json?mode=driving&transit_routing_preference=less_driving&origin=" + pickUpLocation.getLatitude() + "," + pickUpLocation.getLongitude() +
                    "&destination=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() +
                    "&key=" + getResources().getString(R.string.google_direction_api);//browser api key
            Log.d("Kushy", requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                //extract Json
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");
                                JSONObject legsObject = legs.getJSONObject(0);

                                //get distance

                                JSONObject distance = legsObject.getJSONObject("distance");
                                String distance_text = distance.getString("text");

                                //use regex to extract double from string remove all text;

                                Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));

                                JSONObject timeObj = legsObject.getJSONObject("duration");
                                String time_text = timeObj.getString("text");

                                //use regex to extract double from string remove all text; to take number from string to parse
                                Double time_value = Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+", ""));

                                sendDropOffNotification(customerId);


                                Intent intent = new Intent(DriverTracking.this, TripDetailActivity.class);
                                intent.putExtra("start_address", legsObject.getString("start_address"));
                                intent.putExtra("end_address", legsObject.getString("end_address"));
                                intent.putExtra("time", String.valueOf(time_value));
                                intent.putExtra("distance", String.valueOf(distance_value));
                                intent.putExtra("total", Common.formulaCostSilver(distance_value, time_value));
                                intent.putExtra("location_start", String.format("%f,%f", pickUpLocation.getLatitude(), pickUpLocation.getLongitude()));
                                intent.putExtra("location_end", String.format("%f,%f", Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                    @Override
                                    public void onSuccess(Account account) {
                                        id=account.getId();
                                    }

                                    @Override
                                    public void onError(AccountKitError accountKitError) {

                                    }
                                });
                                intent.putExtra("driverId",id);
                                intent.putExtra("riderId", Common.RiderIdHolder);

                                startActivity(intent);
                                finish();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }



                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, "Map Error :" + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateCashFeeGold(final Location pickUpLocation, Location mLastLocation) {

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions" +
                    "/json?mode=driving&transit_routing_preference=less_driving&origin=" + pickUpLocation.getLatitude() + "," + pickUpLocation.getLongitude() +
                    "&destination=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() +
                    "&key=" + getResources().getString(R.string.google_direction_api);//browser api key
            Log.d("Kushy", requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                //extract Json
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");
                                JSONObject legsObject = legs.getJSONObject(0);

                                //get distance

                                JSONObject distance = legsObject.getJSONObject("distance");
                                String distance_text = distance.getString("text");

                                //use regex to extract double from string remove all text;

                                Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));

                                JSONObject timeObj = legsObject.getJSONObject("duration");
                                String time_text = timeObj.getString("text");

                                //use regex to extract double from string remove all text; to take number from string to parse
                                Double time_value = Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+", ""));

                                sendDropOffNotification(customerId);


                                Intent intent = new Intent(DriverTracking.this, TripDetailActivity.class);
                                intent.putExtra("start_address", legsObject.getString("start_address"));
                                intent.putExtra("end_address", legsObject.getString("end_address"));
                                intent.putExtra("time", String.valueOf(time_value));
                                intent.putExtra("distance", String.valueOf(distance_value));
                                intent.putExtra("total", Common.formulaCostGold(distance_value, time_value));
                                intent.putExtra("location_start", String.format("%f,%f", pickUpLocation.getLatitude(), pickUpLocation.getLongitude()));
                                intent.putExtra("location_end", String.format("%f,%f", Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                    @Override
                                    public void onSuccess(Account account) {
                                        id=account.getId();
                                    }

                                    @Override
                                    public void onError(AccountKitError accountKitError) {

                                    }
                                });
                                intent.putExtra("driverId",id);
                                intent.putExtra("riderId", Common.RiderIdHolder);

                                startActivity(intent);
                                finish();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }



                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, "Map Error :" + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateCashFeeBoda(final Location pickUpLocation, Location mLastLocation) {

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions" +
                    "/json?mode=driving&transit_routing_preference=less_driving&origin=" + pickUpLocation.getLatitude() + "," + pickUpLocation.getLongitude() +
                    "&destination=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() +
                    "&key=" + getResources().getString(R.string.google_direction_api);//browser api key
            Log.d("Kushy", requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                //extract Json
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");
                                JSONObject legsObject = legs.getJSONObject(0);

                                //get distance

                                JSONObject distance = legsObject.getJSONObject("distance");
                                String distance_text = distance.getString("text");

                                //use regex to extract double from string remove all text;

                                Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));

                                JSONObject timeObj = legsObject.getJSONObject("duration");
                                String time_text = timeObj.getString("text");

                                //use regex to extract double from string remove all text; to take number from string to parse
                                Double time_value = Double.parseDouble(time_text.replaceAll("[^0-9\\\\.]+", ""));

                                sendDropOffNotification(customerId);


                                Intent intent = new Intent(DriverTracking.this, TripDetailActivity.class);
                                intent.putExtra("start_address", legsObject.getString("start_address"));
                                intent.putExtra("end_address", legsObject.getString("end_address"));
                                intent.putExtra("time", String.valueOf(time_value));
                                intent.putExtra("distance", String.valueOf(distance_value));
                                intent.putExtra("total", Common.formulaCostBoda(distance_value, time_value));
                                intent.putExtra("location_start", String.format("%f,%f", pickUpLocation.getLatitude(), pickUpLocation.getLongitude()));
                                intent.putExtra("location_end", String.format("%f,%f", Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                    @Override
                                    public void onSuccess(Account account) {
                                        id=account.getId();
                                    }

                                    @Override
                                    public void onError(AccountKitError accountKitError) {

                                    }
                                });
                                intent.putExtra("driverId",id);
                                intent.putExtra("riderId", Common.RiderIdHolder);

                                startActivity(intent);
                                finish();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }



                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, "Map Error :" + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
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
      riderMarker = mMap.addCircle(new CircleOptions()
                .center(new LatLng(Double.parseDouble(riderLat), Double.parseDouble(riderLng)))
                .radius(50) //50metres.
                .strokeColor(Color.YELLOW)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f));
       mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(riderLat), Double.parseDouble(riderLng)))
                .title("Customer Location")
               // .snippet(String.valueOf(riderLat))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_marker)));


        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_table).child(Common.currentDriver.getCarType()));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(riderLat), Double.parseDouble(riderLng)), 0.05f);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArrivedNotification(customerId);
                Intent intent = new Intent(getApplicationContext(), ShowAllRequest.class);
                intent.putExtra("lat", riderLat);
                intent.putExtra("lng", riderLng);
                //  intent.putExtra("iddriver", );
                startActivity(intent);
                BtnStartTrip.setEnabled(true);
                BtnStartTrip.setText("Click to Start Trip");




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

    private void sendArrivedNotification(String customerId) {
        Token token = new Token(customerId);
        //   Notification notification = new Notification("Arrived", (String.format("Beba driver %s has arrived at your location", Common.currentDriver.getName())));
        //    Sender sender = new Sender(token.getToken(), notification);
        Map<String, String> content = new HashMap<>();
        content.put("title", "Arrived");
        content.put("message", (String.format("Beba driver %s has arrived your location", Common.currentDriver.getName())));
        DataMessage dataMessage = new DataMessage(token.getToken(), content);
        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                    finish();

                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void sendDropOffNotification(String customerId) {
        Token token = new Token(customerId);
        //  Notification notification = new Notification("DropOff", customerId);
        //  Sender sender = new Sender(token.getToken(), notification);
        Map<String, String> content = new HashMap<>();
        content.put("title", "DropOff");
        content.put("message", customerId);
        DataMessage dataMessage = new DataMessage(token.getToken(), content);
        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                    finish();

                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
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
                    }
                    if (Common.currentDriver.getCarType().equals("Beba Silver")) {
                        driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .title("You")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_silver)));
                    }
                    if (Common.currentDriver.getCarType().equals("Beba Gold")) {
                        driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .title("You")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_gold)));
                    }
                    if (Common.currentDriver.getCarType().equals("Beba Boda")) {
                        driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                .title("You")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bike)));
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));

                    if (direction != null)
                        direction.remove();//remove old direction
                    getDirection();


                } else {
                    Log.d("Error", "cannot get your location");
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

                                new ParseTask().execute(response.body().toString());


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, "Google Map : " + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void getDirectionTo() {
        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions" +
                    "/json?mode=driving&transit_routing_preference=less_driving&origin=" + riderLat + "," + riderLat +
                    "&destination=" + riderLat + "," + riderLng +
                    "&key=" + getResources().getString(R.string.google_direction_api);//browser api key

            Log.d("Hillare", requestApi);
            //PRINT Url for debug

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {

                                new ParseTask().execute(response.body().toString());


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, "Google Map : " + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(this, ShowAllRequest.class);
        intent.putExtra("lat", riderLat);
        intent.putExtra("lng", riderLng);
      //  intent.putExtra("iddriver", );
        startActivity(intent);

    }


    private class ParseTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        ProgressDialog progressDialog = new ProgressDialog(DriverTracking.this);

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
}
