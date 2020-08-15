package com.techkip.bebarider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.SphericalUtil;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.techkip.bebarider.Helper.CustomerWindowInfo;
import com.techkip.bebarider.Helper.DirectionJSONParser;
import com.techkip.bebarider.common.Common;
import com.techkip.bebarider.common.PicassoImage;
import com.techkip.bebarider.model.Rider;
import com.techkip.bebarider.model.Token;
import com.techkip.bebarider.remote.IFCMService;
import com.techkip.bebarider.remote.IGoogleAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.maps.model.JointType.ROUND;

public class Tracking extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    //play service
    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;


    Marker mUserMarker, mMarkerDestination;

    SupportMapFragment mMapFragment;


    int radius = 1; //1km
    int distance = 3;
    private static final int LIMIT = 3; //3km around
    PlaceAutocompleteFragment place_location, place_destination;
    AutocompleteFilter typeFilter;
    // send alert
    IFCMService mService;

    //presence of driver
    DatabaseReference driversAvailable;

    String mPlaceLocation, mPlaceDestination;
    TextView TxtDriverName, TxtDriverPhone, TxtCarPlate, TxtDriverArrivalTime;
    CircleImageView ivDriverPic, ivCarPic;


    String timer,driverPic,carPic,driverName,driverPhone,carPlate;
    // declare to upload data
    FirebaseStorage mfirebaseStorage;
    StorageReference mstorageRef;

    //select car
    ImageView IvBebaBronze, IvBebaSilver, IvBebaGold, IvBebaBoda;
    boolean isBebaBronze, isBebaSilver, isBebaGold, isBebaBoda = true;
    TextView TxtBebaBronze, TxtBebaSilver, TxtBebaGold, TxtBebaBoda;

    IGoogleAPI mServiceApi;
    Polyline direction;
    Button CancelRide,CallDriverBtn;

    private long timeCountInMilliSeconds = 20000;



    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;

    private TextView TxtOne,textViewTime,TxtTwo;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mServiceApi = Common.getGoogleAPI();
        mService = Common.getFCMService();
        mfirebaseStorage = FirebaseStorage.getInstance();
        mstorageRef = mfirebaseStorage.getReference();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //maps
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(Tracking.this);
        textViewTime = findViewById(R.id.textViewTime);
        TxtOne = findViewById(R.id.textone);
        TxtTwo = findViewById(R.id.texttwo);
        TxtDriverName = findViewById(R.id.txt_driverName);
        TxtDriverPhone = findViewById(R.id.txt_driverPhone);
        ivDriverPic = findViewById(R.id.iv_DriverProfile);
        ivCarPic = findViewById(R.id.iv_CarProfile);
        TxtCarPlate = findViewById(R.id.txt_carPlate);
        TxtDriverArrivalTime = findViewById(R.id.txt_arrivalTime);

        CancelRide = findViewById(R.id.btn_cancelRide);
        CallDriverBtn=findViewById(R.id.btn_callDriver);


        timer = (getIntent().getStringExtra("time"));
        TxtDriverArrivalTime.setText(timer);

        driverName = (getIntent().getStringExtra("driverName"));
        TxtDriverName.setText(driverName);

        driverPhone = (getIntent().getStringExtra("driverPhone"));
        TxtDriverPhone.setText(driverPhone);

        carPlate = (getIntent().getStringExtra("carPlate"));
        TxtCarPlate.setText(carPlate);


        driverPic = (getIntent().getStringExtra("driverPic"));
        carPic = (getIntent().getStringExtra("carPic"));

       PicassoImage.downloadImage(Tracking.this, driverPic, ivDriverPic);
       PicassoImage.downloadImage(Tracking.this, carPic, ivCarPic);
        IvBebaBronze = findViewById(R.id.iv_bebaBronze);
        IvBebaSilver = findViewById(R.id.iv_bebaSilver);
        IvBebaGold = findViewById(R.id.iv_bebaGold);
        IvBebaBoda = findViewById(R.id.iv_bebaBoda);

        TxtBebaBronze = findViewById(R.id.txt_bronze);
        TxtBebaSilver = findViewById(R.id.txt_silver);
        TxtBebaGold = findViewById(R.id.txt_gold);
        TxtBebaBoda = findViewById(R.id.txt_boda);
//        loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        startCountDownTimer();

        CancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    final android.app.AlertDialog pd = new SpotsDialog(Tracking.this);
                    pd.show();
                    pd.setMessage("Sending request....");
                    pd.setCancelable(true);

                    if (Common.driverId != null && !Common.driverId.isEmpty()) {
                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {

                            @Override
                            public void onSuccess(Account account) {


                                requestCancePickUpHere(account.getId());
                                pd.dismiss();
                                Common.sendCancelRequestToDriver(Common.driverId, mService, getBaseContext(), mLastLocation);


                            }



                            @Override
                            public void onError(AccountKitError accountKitError) {

                            }
                        });

                    }

            }
        });

        CallDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + TxtDriverPhone.getText().toString()));
                if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                startActivity(intent);
            }
        });
        IvBebaBronze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isBebaBronze = true;
                isBebaSilver = false;
                isBebaGold = false;
                isBebaBoda = false;
                if (isBebaBronze) {
                    IvBebaBronze.setImageResource(R.mipmap.ic_car_bronze);
                    IvBebaSilver.setImageResource(R.mipmap.ic_car_silver_gray);
                    IvBebaGold.setImageResource(R.mipmap.ic_car_gold_gray);
                    IvBebaBoda.setImageResource(R.mipmap.ic_car_bike_g);
                    TxtBebaBronze.setTextColor(getResources().getColor(R.color.colorAccent));
                    TxtBebaSilver.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaGold.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaBoda.setTextColor(getResources().getColor(R.color.white));

                }
                {
                    IvBebaBronze.setImageResource(R.mipmap.ic_car_bronze);
                    IvBebaSilver.setImageResource(R.mipmap.ic_car_silver_gray);
                    IvBebaGold.setImageResource(R.mipmap.ic_car_gold_gray);
                    IvBebaBoda.setImageResource(R.mipmap.ic_car_bike_g);

                    TxtBebaBronze.setTextColor(getResources().getColor(R.color.colorAccent));
                    TxtBebaSilver.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaGold.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaBoda.setTextColor(getResources().getColor(R.color.white));
                }

                mMap.clear();
                loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }
        });

        IvBebaSilver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isBebaBronze = false;
                isBebaSilver = true;
                isBebaGold = false;
                isBebaBoda = false;
                if (isBebaSilver) {

                    IvBebaBronze.setImageResource(R.mipmap.ic_car_bronze_gray);
                    IvBebaSilver.setImageResource(R.mipmap.ic_car_silver);
                    IvBebaGold.setImageResource(R.mipmap.ic_car_gold_gray);
                    IvBebaBoda.setImageResource(R.mipmap.ic_car_bike_g);

                    TxtBebaBronze.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaSilver.setTextColor(getResources().getColor(R.color.colorAccent));
                    TxtBebaGold.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaBoda.setTextColor(getResources().getColor(R.color.white));
                } else {
                    IvBebaBronze.setImageResource(R.mipmap.ic_car_bronze_gray);
                    IvBebaSilver.setImageResource(R.mipmap.ic_car_silver_gray);
                    IvBebaGold.setImageResource(R.mipmap.ic_car_gold_gray);
                    IvBebaBoda.setImageResource(R.mipmap.ic_car_bike_g);

                    TxtBebaBronze.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaSilver.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaGold.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaBoda.setTextColor(getResources().getColor(R.color.white));
                }
                mMap.clear();
                loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }
        });

        IvBebaGold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isBebaBronze = false;
                isBebaSilver = false;
                isBebaGold = true;
                isBebaBoda = false;
                if (isBebaGold) {
                    IvBebaBronze.setImageResource(R.mipmap.ic_car_bronze_gray);
                    IvBebaSilver.setImageResource(R.mipmap.ic_car_silver_gray);
                    IvBebaGold.setImageResource(R.mipmap.ic_car_gold);
                    IvBebaBoda.setImageResource(R.mipmap.ic_car_bike_g);

                    TxtBebaBronze.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaSilver.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaGold.setTextColor(getResources().getColor(R.color.colorAccent));
                    TxtBebaBoda.setTextColor(getResources().getColor(R.color.white));
                } else {
                    IvBebaBronze.setImageResource(R.mipmap.ic_car_bronze_gray);
                    IvBebaSilver.setImageResource(R.mipmap.ic_car_silver_gray);
                    IvBebaGold.setImageResource(R.mipmap.ic_car_gold_gray);
                    IvBebaBoda.setImageResource(R.mipmap.ic_car_bike_g);

                    TxtBebaBronze.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaSilver.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaGold.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaBoda.setTextColor(getResources().getColor(R.color.white));
                }
                mMap.clear();
                loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }
        });
        IvBebaBoda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isBebaBronze = false;
                isBebaSilver = false;
                isBebaGold = false;
                isBebaBoda = true;
                if (isBebaBoda) {
                    IvBebaBronze.setImageResource(R.mipmap.ic_car_bronze_gray);
                    IvBebaSilver.setImageResource(R.mipmap.ic_car_silver_gray);
                    IvBebaGold.setImageResource(R.mipmap.ic_car_gold_gray);
                    IvBebaBoda.setImageResource(R.mipmap.ic_car_bike);

                    TxtBebaBronze.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaSilver.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaGold.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaBoda.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    IvBebaBronze.setImageResource(R.mipmap.ic_car_bronze_gray);
                    IvBebaSilver.setImageResource(R.mipmap.ic_car_silver_gray);
                    IvBebaGold.setImageResource(R.mipmap.ic_car_gold_gray);
                    IvBebaBoda.setImageResource(R.mipmap.ic_car_bike_g);

                    TxtBebaBronze.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaSilver.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaGold.setTextColor(getResources().getColor(R.color.white));
                    TxtBebaBoda.setTextColor(getResources().getColor(R.color.white));
                }
                mMap.clear();
                loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }
        });

        setUpLocation();
        updateFirebaseToken();


//start
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(this.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }
        //end


    }



    private void startCountDownTimer() {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));

               // progressBarCircle.setProgress((int) (millisUntilFinished / 1000));

            }

            @Override
            public void onFinish() {

                textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
                // call to initialize the progress bar values
              //  setProgressBarValues();
                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED;
              CancelRide.setEnabled(false);
              textViewTime.setText("Your Time is Up");
              TxtOne.setText("");
              TxtTwo.setText("Yow will be charged for Cancelling ride");
            }

        }.start();
        countDownTimer.start();
    }

    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }


    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;


    }


    private void requestCancePickUpHere(final String uid) {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickuprequest_table);
                GeoFire mGeoFire = new GeoFire(dbRequest);
                mGeoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
               /* dbRequest.child("name").setValue(Common.currentUser.getName());
                dbRequest.child("Number").setValue(Common.currentUser.getPhone());
                dbRequest.child("riderId").setValue(Common.riderId);
                dbRequest.child("driverId").setValue(Common.driverId);
                dbRequest.child("lat").setValue(mLastLocation.getLatitude());
                dbRequest.child("lng").setValue(mLastLocation.getLongitude());*/
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });


    }

    private void updateFirebaseToken() {

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference tokens = database.getReference(Common.token_table);

                Token token = new Token(FirebaseInstanceId.getInstance().getToken());
                tokens.child(account.getId())
                        .setValue(token);
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildLocationCallBack();
                    buildLocationRequest();
                    displayLocation();


                }
                break;
        }
    }

    @SuppressLint("RestrictedApi")
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
                mLastLocation = locationResult.getLastLocation();
                Common.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size() - 1);

                displayLocation();

            }
        };

    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            //Request Run Time
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE
            }, MY_PERMISSION_REQUEST_CODE);


        } else {
            buildLocationCallBack();
            buildLocationRequest();
            displayLocation();
        }

    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //  mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;
                if (mLastLocation != null) {

                    //create LatLng of lastLocation to be center point
                    //100000 in distance around AND 0 is northeast,90  is east,180 is south  270 is west
                    LatLng center = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                    LatLng northSide = SphericalUtil.computeOffset(center, 10000, 0);
                    LatLng southSide = SphericalUtil.computeOffset(center, 10000, 180);
                    LatLng EastSide = SphericalUtil.computeOffset(center, 10000, 90);
                    LatLng WestSide = SphericalUtil.computeOffset(center, 10000, 270);

                    LatLngBounds bounds = LatLngBounds.builder()
                            .include(northSide)
                            .include(southSide)
                            .include(EastSide)
                            .include(WestSide)
                            .build();
                   /* place_location.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);
                    place_destination.setBoundsBias(bounds);
                    place_destination.setFilter(typeFilter);*/


                    //Presence System
                    driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_table);
                    driversAvailable.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    final double latitude = mLastLocation.getLatitude();
                    final double longitude = mLastLocation.getLongitude();


                    loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));


                    Log.d("Beba", String.format("Location was changed : %f / %f", latitude, longitude));
                } else {
                    Log.d("Beba", "cannot get your location");
                }
            }
        });

    }

    private void loadAllAvailableDrivers(final LatLng location) {

        //Update Firebase

        mMap.clear();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_marker))
                .position(location)
                .title("Your Location"));

        //move cam to tz postion
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

        //load all available driver in 3km
        DatabaseReference driverLoc = null;
        if (isBebaBronze) {
            driverLoc = FirebaseDatabase.getInstance().getReference(Common.driver_table).child("Beba Bronze");
        }

        if (isBebaSilver) {
            driverLoc = FirebaseDatabase.getInstance().getReference(Common.driver_table).child("Beba Silver");
        }

        if (isBebaGold) {
            driverLoc = FirebaseDatabase.getInstance().getReference(Common.driver_table).child("Beba Gold");
        }
        if (isBebaBoda) {
            driverLoc = FirebaseDatabase.getInstance().getReference(Common.driver_table).child("Beba Boda");
        }

      /* if (!(isBebaBronze &&isBebaSilver&&isBebaSilver&&isBebaBoda)){
           driverLoc = FirebaseDatabase.getInstance().getReference(Common.driver_table);
       }*/


        GeoFire geoFire = new GeoFire(driverLoc);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                //USE key to get email fom table users
                // table driverinfo is when the driver registers
                FirebaseDatabase.getInstance().getReference(Common.user_driver_table)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Rider model here
                                Rider rider = dataSnapshot.getValue(Rider.class);
                                //Add dere to map


                                if (Common.driverId.equals(dataSnapshot.getKey())) {
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(location.latitude, location.longitude))
                                            .flat(true)
                                            .title("Driver:" + rider.getName())
                                            .snippet("Driver ID:" + dataSnapshot.getKey())
                                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_ride)));
                                          getDirection();
                                }

                              /*  if (Common.driverId.equals(dataSnapshot.getKey()) && rider.getCarType().equals("Beba Boda")) {
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(location.latitude, location.longitude))
                                            .flat(true)
                                            .title("Driver Name:" + rider.getName())
                                            .snippet("Driver ID:" + dataSnapshot.getKey())
                                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car_bike)));
                                }*/


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <= LIMIT) {
                    //load in 3 km
                    distance++;
                    loadAllAvailableDrivers(location);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void getDirection() {
        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions" +
                    "/json?mode=driving&transit_routing_preference=less_driving&origin=" + currentPosition.latitude + "," + currentPosition.longitude +
                    "&destination=" + mMarkerDestination.getPosition().latitude + "," + mMarkerDestination.getPosition().longitude +
                    "&key=" + getResources().getString(R.string.google_direction_api);//browser api key

            Log.d("Hillare", requestApi);
            //PRINT Url for debug

            mServiceApi.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(retrofit2.Call<String> call, Response<String> response) {
                            try {

                                new Tracking.ParseTask().execute(response.body().toString());


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(Tracking.this, "Error : " + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ParseTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        ProgressDialog progressDialog = new ProgressDialog(Tracking.this);

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
                polylineOptions.color(getResources().getColor(R.color.colorAccent));
                polylineOptions.geodesic(true);
                polylineOptions.startCap(new SquareCap());
                polylineOptions.endCap(new SquareCap());
                polylineOptions.jointType(ROUND);


            }
            direction = mMap.addPolyline(polylineOptions);

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            Intent intent = new Intent(Tracking.this, Tracking.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        if (id == R.id.menu_help) {

        }
        if (id == R.id.menu_terms) {

        }
        if (id == R.id.menu_about) {

        }


        return super.onOptionsItemSelected(item);
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
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomerWindowInfo(this));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMarkerDestination != null)
                    mMarkerDestination.remove();
                mMarkerDestination = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_destination_marker))
                        .position(latLng)
                        .title("Destination"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                BottomSheetFragment mBottomSheet = BottomSheetFragment.newInstance(String.format("%f,%f", mLastLocation.getLatitude(), mLastLocation.getLongitude()), String.format("%f,%f", latLng.latitude, latLng.longitude), true);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());

                Toast.makeText(Tracking.this, "" + latLng.latitude, Toast.LENGTH_SHORT).show();

            }
        });
        if (ActivityCompat.checkSelfPermission(Tracking.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Tracking.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildLocationCallBack();
        buildLocationRequest();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());


        mMap.setOnInfoWindowClickListener(this);


    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        if (!marker.getTitle().equals("Your Location")) {
            //call to new activity :call driver
            Intent intent = new Intent(Tracking.this, CallDriver.class);
            intent.putExtra("driverId", marker.getSnippet().replaceAll("\\D+", ""));
            intent.putExtra("lat", mLastLocation.getLatitude());
            intent.putExtra("lng", mLastLocation.getLongitude());
            startActivity(intent);
        } else
            Toast.makeText(this, "Pick a driver to come to this location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
      //  super.onBackPressed();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setIcon(R.mipmap.ic_launcher);
        alertDialogBuilder.setMessage("Are you sure you want to exit tracking your driver?");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Tracking.this, Home.class);
                        startActivity(intent);
                        finish();

                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        //Showing the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
