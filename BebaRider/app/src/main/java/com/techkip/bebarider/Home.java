package com.techkip.bebarider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.techkip.bebarider.Helper.CustomerWindowInfo;
import com.techkip.bebarider.Helper.DirectionJSONParser;
import com.techkip.bebarider.common.Common;
import com.techkip.bebarider.common.Connector;
import com.techkip.bebarider.common.PicassoImage;
import com.techkip.bebarider.history.History;
import com.techkip.bebarider.model.Rider;
import com.techkip.bebarider.model.Token;
import com.techkip.bebarider.remote.IFCMService;
import com.techkip.bebarider.remote.IGoogleAPI;
import com.techkip.bebarider.stupidstuff.AboutActivity;
import com.techkip.bebarider.stupidstuff.Dev.ProfileDev;
import com.techkip.bebarider.stupidstuff.HelpActivity;
import com.techkip.bebarider.stupidstuff.HowActivity;
import com.techkip.bebarider.stupidstuff.PrivacyActivity;
import com.techkip.bebarider.stupidstuff.TermsActivity;
import com.techkip.bebarider.updates.ForceUpdateChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.maps.model.JointType.ROUND;
import static com.techkip.bebarider.common.Constants.URLs.Url_Send_Request;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, ValueEventListener,ForceUpdateChecker.OnUpdateNeededListener {

    private GoogleMap mMap;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    //play service
    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICE_RES_REQUEST = 300193;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ref;
    GeoFire geoFire;
    Marker mUserMarker, mMarkerDestination;

    SupportMapFragment mMapFragment;

    Button btnPickUpRequest;


    int radius = 1; //1km
    int distance = 1;
    private static final int LIMIT = 1; //3km around

    // send alert
    IFCMService mService;

    //presence of driver
    DatabaseReference driversAvailable;
    DatabaseReference carType;


    PlaceAutocompleteFragment place_location, place_destination;
    AutocompleteFilter typeFilter;

    String mPlaceLocation, mPlaceDestination;
    TextView TxtRiderName, TxtRiderPhone, TxtRiderRate;
    CircleImageView ivRiderProfile;
    // declare to upload data
    FirebaseStorage mfirebaseStorage;
    StorageReference mstorageRef;

    //select car
    ImageView IvBebaBronze, IvBebaSilver, IvBebaGold, IvBebaBoda;
    boolean isAll, isBebaBronze, isBebaSilver, isBebaGold, isBebaBoda = true;
    TextView TxtBebaBronze, TxtBebaSilver, TxtBebaGold, TxtBebaBoda;
    android.support.design.widget.NavigationView navigationView;


    Geocoder geocoder;
    List<Address> addresses;

    TextView destnation,location;//hide in content home to pick destination

    MapRipple mapRipple;

    private BroadcastReceiver cancelBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Common.driverId="";
            Common.isDriverFound = false;
            btnPickUpRequest.setText("REQUEST PICK UP");
            btnPickUpRequest.setEnabled(true);
            if ( mapRipple.isAnimationRunning()) {
                mapRipple.stopRippleMapAnimation();
                mUserMarker.hideInfoWindow();
            }


        }
    };

    private BroadcastReceiver dropOffBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

             Common.driverId="";
            Common.isDriverFound = false;
            btnPickUpRequest.setText("REQUEST PICK UP");
            btnPickUpRequest.setEnabled(true);

        }
    };

    String id;
    String driverIdTag = "driver_id";
    String riderIdTag = "rider_id";
    String riderNameTag = "rider_name";
    String riderPhoneTag = "rider_phone";
    String riderImageTag = "rider_image";
    String riderLatTag = "rider_lat";
    String riderLngTag = "rider_lng";
    String riderLocTag = "loc_name";
    String riderDestLatTag = "dest_lat";
    String riderDestLngTag = "dest_lng";
    String riderDestTag = "dest_name";
    ProgressDialog progressDialog;

    EditText tvDriverId, tvRiderLat, tvRiderLng, TxtRiderDestLat, tvRiderDestLng;
    String GetDriverIdTv, GetRiderIdTv, GetRiderNameTv, GetRiderPhoneTv, GetRiderImageTv, GetRiderLatTv, GetRiderLngTv,  GetRiderLocTv, GetRiderDestLatTv, GetRiderDestLngTv,GetRiderDestTv;

    Polyline direction;
    IGoogleAPI mServiceApi;
    FloatingActionButton HomeBtn,SetHomeBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();

        tvDriverId = findViewById(R.id.tv_driverId);
        tvRiderLat = findViewById(R.id.tv_riderLat);
        tvRiderLng = findViewById(R.id.tv_riderLng);
        TxtRiderDestLat = findViewById(R.id.tv_PickupLocation);
        tvRiderDestLng = findViewById(R.id.tv_Destination);

        HomeBtn=findViewById(R.id.fab_home);
        SetHomeBtn= findViewById(R.id.fab_set_home);

        HomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Home.this, "you will be able to get home direction after being set", Toast.LENGTH_SHORT).show();
            }
        });
        SetHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Home.this, "you will be able to set home direction here", Toast.LENGTH_SHORT).show();
            }
        });


        geocoder = new Geocoder(this, Locale.getDefault());

        LocalBroadcastManager.getInstance(this).registerReceiver(cancelBroadcast, new IntentFilter(Common.CANCEL_BROADCAST_STRING));

        LocalBroadcastManager.getInstance(this).registerReceiver(dropOffBroadcast, new IntentFilter(Common.DROPOFF_BROADCAST_STRING));
        mServiceApi = Common.getGoogleAPI();
        mService = Common.getFCMService();
        mfirebaseStorage = FirebaseStorage.getInstance();
        mstorageRef = mfirebaseStorage.getReference();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView = navigationView.getHeaderView(0);
        TxtRiderName = navigationHeaderView.findViewById(R.id.tv_RiderName);
        TxtRiderRate = navigationHeaderView.findViewById(R.id.tv_Rate);
        ivRiderProfile = navigationHeaderView.findViewById(R.id.iv_RiderProfile);

        TxtRiderName.setText(String.format("%s", Common.currentUser.getName()));
        TxtRiderRate.setText(String.format("%s", Common.currentUser.getRates()));

        PicassoImage.downloadImage(Home.this, Common.currentUser.getProfilePicUrl(), ivRiderProfile);

        //maps
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(Home.this);


        IvBebaBronze = findViewById(R.id.iv_bebaBronze);
        IvBebaSilver = findViewById(R.id.iv_bebaSilver);
        IvBebaGold = findViewById(R.id.iv_bebaGold);
        IvBebaBoda = findViewById(R.id.iv_bebaBoda);

        TxtBebaBronze = findViewById(R.id.txt_bronze);
        TxtBebaSilver = findViewById(R.id.txt_silver);
        TxtBebaGold = findViewById(R.id.txt_gold);
        TxtBebaBoda = findViewById(R.id.txt_boda);


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
/*
                if (distance <= LIMIT) {
                    //load in 3 km
                    distance++;
                    mMap.clear();
                    loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                }*/
               // mMap.clear();
                if (driversAvailable != null)
                    driversAvailable.removeEventListener(Home.this);
                driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_table);
                driversAvailable.addValueEventListener(Home.this);
                loadAllAvailableDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
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

            /*    if (distance <= LIMIT) {
                    //load in 3 km
                    distance++;
                    mMap.clear();
                    loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                }*/

             //  mMap.clear();
                if (driversAvailable != null)
                    driversAvailable.removeEventListener(Home.this);
                driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_table);
                driversAvailable.addValueEventListener(Home.this);
                loadAllAvailableDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
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

               /* if (distance <= LIMIT) {
                    //load in 3 km
                    distance++;
                    mMap.clear();
                    loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                }*/

               //mMap.clear();
                if (driversAvailable != null)
                    driversAvailable.removeEventListener(Home.this);
                driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_table);
                driversAvailable.addValueEventListener(Home.this);
                loadAllAvailableDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
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

               /* if (distance <= LIMIT) {
                    //load in 3 km
                    distance++;
                    mMap.clear();
                    loadAllAvailableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                }*/

               // mMap.clear();
                if (driversAvailable != null)
                    driversAvailable.removeEventListener(Home.this);
                driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_table);
                driversAvailable.addValueEventListener(Home.this);
                loadAllAvailableDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
            }
        });

        btnPickUpRequest = findViewById(R.id.btn_request);

        btnPickUpRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              if (location != null) {
                    if (!Common.isDriverFound ) {
                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                            @Override
                            public void onSuccess(Account account) {
                                id = account.getId();
                               // UploadServer();
                                requestPickUpHere(account.getId());
                                // UploadServer();
                            }

                            @Override
                            public void onError(AccountKitError accountKitError) {

                            }
                        });
                    } else {
                        btnPickUpRequest.setEnabled(false);
                        Common.sendRequestToDriver(Common.driverId, mService, getBaseContext(), Common.mLastLocation);
                    }

                }else {
                    StyleableToast.makeText(Home.this, "Tap your Destination to select", Toast.LENGTH_LONG, R.style.nolocation).show();

                }
            }
        });
        place_destination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_destination);
        place_location = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_location);

        place_location.setHint("Where From..");


        place_destination.setHint("Where To..");
        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();

        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceLocation = place.getAddress().toString();

                //remove old marker
                mMap.clear();

                mUserMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_marker))
                        .title("Pick up Here")
                );
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
            }

            @Override
            public void onError(Status status) {

            }
        });

        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceDestination = place.getAddress().toString();

                //new location
              //  mMap.clear(); //i niliongeza
                mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_destination_marker))
                        .title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
                TxtRiderDestLat.setText(String.valueOf(place.getLatLng().latitude));
                tvRiderDestLng.setText(String.valueOf(place.getLatLng().longitude));

                try {
                    addresses = geocoder.getFromLocation(place.getLatLng().latitude, place. getLatLng().longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String destination = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                destnation = findViewById(R.id.txt_to);
                destnation .setText(destination);
                //  show info

                BottomSheetFragment mBottomSheet = BottomSheetFragment.newInstance(mPlaceLocation, mPlaceDestination, false);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());


            }


            @Override
            public void onError(Status status) {

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
    @Override
    public void onUpdateNeeded(final String updateUrl) {
        new AlertDialog.Builder(this, R.style.DialogStyle)
                .setTitle("New version available")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Please, update app to new version to continue.?")
                .setNegativeButton("Not Now", null)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        redirectStore(updateUrl);
                    }
                }).create().show();
    }



    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateFirebaseToken() {
       FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference tokens = database.getReference(Common.token_table);

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Token token = new Token(instanceIdResult.getToken());
                        tokens.child(account.getId()).setValue(token);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error Token", e.getMessage());
                       // Toast.makeText(Home.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.d("Error AccountKit", accountKitError.getUserFacingMessage());
            }
        });


    }

    private void requestPickUpHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickuprequest_table);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()),
                new GeoFire.CompletionListener() {

                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                }
        );

        if (mUserMarker.isVisible()) {
            mUserMarker.remove();

            //add new marker
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .title("Pick Up is Here")
                    .snippet("")
                    .position(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location_marker)));
            tvRiderLat.setText(String.valueOf(Common.mLastLocation.getLatitude()));
            tvRiderLng.setText(String.valueOf(Common.mLastLocation.getLongitude()));


            mUserMarker.showInfoWindow();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                mapRipple = new MapRipple(mMap, new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), this);
                mapRipple.withNumberOfRipples(3);
                mapRipple.withFillColor(getResources().getColor(R.color.yellow));
                mapRipple.withStrokeColor(getResources().getColor(R.color.colorAccent));
                mapRipple.withStrokewidth(15);      // 10dp
                mapRipple.withDistance(1300);      // 1000 metres radius
                mapRipple.withRippleDuration(8000);    //12000ms
                mapRipple.withTransparency(0.5f);
                mapRipple.startRippleMapAnimation();
            }
            btnPickUpRequest.setText("Wait for driver response");
            findDriver();


        }
    }

    private void findDriver() {
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

        GeoFire gf = new GeoFire(driverLoc);
        final GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                // if driver found
                if (!Common.isDriverFound) {
                    Common.isDriverFound = true;
                    Common.driverId = key;

                    tvDriverId.setText(key);
                    Toast.makeText(Home.this, "Driver : " + key, Toast.LENGTH_SHORT).show();
                    UploadServer();
                    btnPickUpRequest.setText("DRIVER FOUND REQUEST ");

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //increase radius if not found
                if (!Common.isDriverFound && radius < LIMIT) {
                    radius++;
                    findDriver();
                } else
                    {

                    if (!Common.isDriverFound)
                        StyleableToast.makeText(Home.this, "No Driver Near You", Toast.LENGTH_LONG, R.style.nodriver).show();
                        btnPickUpRequest.setText("REQUEST PICK UP");
                        geoQuery.removeAllListeners();




                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    public void UploadServer() {
        try {
            addresses = geocoder.getFromLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        String lastloc = addresses.get(0).getAddressLine(0);

        GetDriverIdTv = Common.driverId;
        GetRiderIdTv = id;
        GetRiderNameTv = Common.currentUser.getName();
        GetRiderPhoneTv = Common.currentUser.getPhone();
        GetRiderImageTv = Common.currentUser.getProfilePicUrl();
        GetRiderLatTv = String.valueOf(Common.mLastLocation.getLatitude());
        GetRiderLngTv = String.valueOf(Common.mLastLocation.getLongitude());
        GetRiderLocTv = lastloc;
        GetRiderDestLatTv = TxtRiderDestLat.getText().toString();
        GetRiderDestLngTv = tvRiderDestLng.getText().toString();
        GetRiderDestTv = destnation.getText().toString();
        class AsyncTaskUploadClass extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {

                super.onPreExecute();

                progressDialog = ProgressDialog.show(Home.this, "", "Processing...", false);


            }

            @SuppressLint("WrongConstant")
            @Override
            protected void onPostExecute(String string1) {

                super.onPostExecute(string1);

                //  Common.sendRequestToDriver(Common.driverId, mService, getBaseContext(), mLastLocation);

                progressDialog.dismiss();

            }

            @Override
            protected String doInBackground(Void... params) {

                Connector imageProcessClass = new Connector();

                HashMap<String, String> HashMapParams = new HashMap<String, String>();

                HashMapParams.put(driverIdTag, GetDriverIdTv);
                HashMapParams.put(riderIdTag, GetRiderIdTv);
                HashMapParams.put(riderNameTag, GetRiderNameTv);
                HashMapParams.put(riderPhoneTag, GetRiderPhoneTv);
                HashMapParams.put(riderImageTag, GetRiderImageTv);
                HashMapParams.put(riderLatTag, GetRiderLatTv);
                HashMapParams.put(riderLngTag, GetRiderLngTv);
                HashMapParams.put(riderLocTag, GetRiderLocTv);
                HashMapParams.put(riderDestLatTag, GetRiderDestLatTv);
                HashMapParams.put(riderDestLngTag, GetRiderDestLngTv);
                HashMapParams.put(riderDestTag, GetRiderDestTv);

                String FinalData = imageProcessClass.connect(HashMapParams, Url_Send_Request);

                return FinalData;
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();
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
                Common.mLastLocation = locationResult.getLastLocation();
                Common.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size() - 1);

                displayLocation();

            }
        };

    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            //Request Run Time
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE
            }, MY_PERMISSION_REQUEST_CODE);


        } else {
            buildLocationCallBack();
            buildLocationRequest();
            displayLocation();
        }

    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //  mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Common.mLastLocation = location;
                if (Common.mLastLocation != null) {

                    //create LatLng of lastLocation to be center point
                    //100000 in distance around AND 0 is northeast,90  is east,180 is south  270 is west
                    LatLng center = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

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
                    place_location.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);
                    place_destination.setBoundsBias(bounds);
                    place_destination.setFilter(typeFilter);

                    //Presence System
                    //    driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_table).child(isBebaBronze? "Beba Bronze":isBebaSilver?"Beba Silver":"Beba Gold");
                    driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_table);
                    driversAvailable.addValueEventListener(Home.this);


                    final double latitude = Common.mLastLocation.getLatitude();
                    final double longitude = Common.mLastLocation.getLongitude();

                    loadAllAvailableDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));


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
                .title("Pickup Location"));

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

                         /* mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .flat(true)
                                        .title("Driver Name:" + rider.getName())
                                        .snippet("Driver ID:" + dataSnapshot.getKey())
                                  .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_cost)));*/
                                if (isBebaBronze) {
                                    if (rider.getCarType().equals("Beba Bronze")) {
                                      mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(location.latitude, location.longitude))
                                                .flat(true)
                                                .title("Driver Name:" + rider.getName())
                                                .snippet("Driver ID:" + dataSnapshot.getKey())
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_bronze)));


                                    }
                                }

                                if (isBebaSilver) {
                                    if (rider.getCarType().equals("Beba Silver")) {
                                        mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(location.latitude, location.longitude))
                                                .flat(true)
                                                .title("Driver Name:" + rider.getName())
                                                .snippet("Driver ID:" + dataSnapshot.getKey())
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_silver)));
                                    }
                                }
                                if (isBebaGold) {
                                    if (rider.getCarType().equals("Beba Gold")) {
                                        mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(location.latitude, location.longitude))
                                                .flat(true)
                                                .title("Driver Name:" + rider.getName())
                                                .snippet("Driver ID:" + dataSnapshot.getKey())
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_gold)));
                                    }
                                }
                                if (isBebaBoda) {
                                    if (rider.getCarType().equals("Beba Boda")) {
                                        mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(location.latitude, location.longitude))
                                                .flat(true)
                                                .title("Driver Name:" + rider.getName())
                                                .snippet("Driver ID:" + dataSnapshot.getKey())
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_boda)));
                                    }
                                }

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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
            Intent intent = new Intent(Home.this, Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        if (id == R.id.menu_help) {
            Intent intent = new Intent(Home.this, HelpActivity.class);
            startActivity(intent);

        }
        if (id == R.id.menu_terms) {
            Intent intent = new Intent(Home.this, TermsActivity.class);
            startActivity(intent);

        }
        if (id == R.id.menu_about) {
            Intent intent = new Intent(Home.this, AboutActivity.class);
            startActivity(intent);

        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            updateProfileInfoDialog();
        } else if (id == R.id.nav_tripHistory) {
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    String driverId = account.getId();
                    Intent intent = new Intent(Home.this, History.class);
                    intent.putExtra("riderId", driverId);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);


                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });
        } else if (id == R.id.nav_signOut) {

           SignOut();


        } else if (id == R.id.nav_rate) {
            try {
                Uri rate = Uri.parse(getString(R.string.playstoreUri));
                Intent rateIntent = new Intent(Intent.ACTION_VIEW, rate);
                startActivity(rateIntent);
            } catch (ActivityNotFoundException e) {
                Uri rate = Uri.parse(getString(R.string.ratelink));
                Intent rateIntent = new Intent(Intent.ACTION_VIEW, rate);
                startActivity(rateIntent);
            }
        }

        else if (id == R.id.nav_share) {
            share();

        } else if (id == R.id.nav_help) {
            Intent help = new Intent(Home.this,HelpActivity.class);
            startActivity(help);

        } else if (id == R.id.nav_privacy) {
            Intent privacy = new Intent(Home.this,PrivacyActivity.class);
            startActivity(privacy);
        }
        else if (id == R.id.nav_developer) {
            Intent prof = new Intent(Home.this,ProfileDev.class);
            startActivity(prof);

        }
        else if (id == R.id.nav_how) {
            Intent how = new Intent(Home.this,HowActivity.class);
            startActivity(how);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void share() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.putExtra(Intent.EXTRA_TEXT, (getString(R.string.share)));
        startActivity(Intent.createChooser(share, "Share App via:"));
    }

    private void updateProfileInfoDialog() {

        final AlertDialog.Builder UpdateDialog = new AlertDialog.Builder(this);
        UpdateDialog.setTitle("Update Info");
        LayoutInflater inflater = LayoutInflater.from(this);
        View updateinfo_Layout = inflater.inflate(R.layout.layout_updateinfo, null);

        ivRiderProfile = updateinfo_Layout.findViewById(R.id.iv_RiderProfile);
        TxtRiderName = updateinfo_Layout.findViewById(R.id.et_RiderName);
        TxtRiderPhone = updateinfo_Layout.findViewById(R.id.et_RiderPhoneNumber);

        UpdateDialog.setView(updateinfo_Layout);
        ivRiderProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


        UpdateDialog.setView(updateinfo_Layout);
        UpdateDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.dismiss();

                final SpotsDialog progressDialog = new SpotsDialog(Home.this);
                progressDialog.show();

//update
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        String name = TxtRiderName.getText().toString();
                        String phone = TxtRiderPhone.getText().toString();

                        Map<String, Object> updateInfo = new HashMap<>();

                        if (!TextUtils.isEmpty(name))
                            updateInfo.put("name", name);
                        if (!TextUtils.isEmpty(phone))
                            updateInfo.put("phone", phone);
                        DatabaseReference riderInfo = FirebaseDatabase.getInstance().getReference(Common.user_rider_table);
                        riderInfo.child(account.getId())
                                .updateChildren(updateInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful())
                                    StyleableToast.makeText(Home.this, "Info Updated Successfully", Toast.LENGTH_LONG, R.style.success).show();
                                else
                                    StyleableToast.makeText(Home.this, "Error while updating", Toast.LENGTH_LONG, R.style.error).show();
                            }

                        });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });
            }


        });


        UpdateDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        UpdateDialog.show();


    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri saveUri = data.getData();
            if (saveUri != null) {

                final ProgressDialog pd = new ProgressDialog(this);
                pd.setMessage("Uploading image..");
                pd.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = mstorageRef.child("images").child("Riders/" + imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                pd.dismiss();


                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(final Uri uri) {

                                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                            @Override
                                            public void onSuccess(Account account) {
                                                Map<String, Object> picUpdate = new HashMap<>();
                                                picUpdate.put("profilePicUrl", uri.toString());

                                                DatabaseReference riderInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_table);
                                                riderInformation.child(account.getId())
                                                        .updateChildren(picUpdate)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                    StyleableToast.makeText(Home.this, "Image Uploaded", Toast.LENGTH_LONG, R.style.success).show();
                                                                else
                                                                    StyleableToast.makeText(Home.this, "Error Uploading", Toast.LENGTH_LONG, R.style.nodriver).show();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onError(AccountKitError accountKitError) {

                                            }
                                        });


                                    }
                                });
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                pd.setMessage("uploading..." + progress + "%");
                            }
                        });


            }
        }
    }

    private void SignOut() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setIcon(R.mipmap.ic_logout);
        alertDialogBuilder.setTitle("Account");
        alertDialogBuilder.setMessage("Are you sure you want to logout?");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        AccountKit.logOut();
                        Paper.book().destroy();
                        Intent intent = new Intent(Home.this, MainActivity.class);
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
                // pia ii
                if (mMarkerDestination != null)
                    mMarkerDestination.remove();
                mMarkerDestination = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_destination_marker))
                        .position(latLng)
                       .draggable(true)
                        .title("Destination"));
                TxtRiderDestLat.setText(String.valueOf(mMarkerDestination.getPosition().latitude));
                tvRiderDestLng.setText(String.valueOf(mMarkerDestination.getPosition().longitude));



                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String destination = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                destnation = findViewById(R.id.txt_to);
                place_destination.setText(destination);
                destnation .setText(destination);



               getDirection();


                try {
                    addresses = geocoder.getFromLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String lastloc = addresses.get(0).getAddressLine(0);

                location = findViewById(R.id.txt_from) ;
                place_location.setText(lastloc);

                location.setText(lastloc);

                BottomSheetFragment mBottomSheet = BottomSheetFragment.newInstance(lastloc, destination, true);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());


            }


        });
        if (ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildLocationCallBack();
        buildLocationRequest();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());


        mMap.setOnInfoWindowClickListener(this);


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

                                new Home.ParseTask().execute(response.body().toString());


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            //StyleableToast.makeText(Home.this, "No Driver Near You", Toast.LENGTH_LONG, R.style.error).show();
                            Toast.makeText(Home.this, "Error : " + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {


        try {
            addresses = geocoder.getFromLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        String lastloc = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
       // location = findViewById(R.id.txt_from);
       // location.setText(lastloc);
        place_location.setText(lastloc);


        if (!marker.getTitle().equals("Pickup Location")) {

            if (destnation != null ) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    mapRipple = new MapRipple(mMap, new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), this);
                    mapRipple.withNumberOfRipples(3);
                    mapRipple.withFillColor(getResources().getColor(R.color.yellow));
                    mapRipple.withStrokeColor(getResources().getColor(R.color.colorAccent));
                    mapRipple.withStrokewidth(15);      // 10dp
                    mapRipple.withDistance(1300);      // 1000 metres radius
                    mapRipple.withRippleDuration(8000);    //12000ms
                    mapRipple.withTransparency(0.5f);
                    mapRipple.startRippleMapAnimation();
                }
                Intent intent = new Intent(Home.this, CallDriver.class);
                intent.putExtra("driverId", marker.getSnippet().replaceAll("\\D+", ""));
                intent.putExtra("lat", Common.mLastLocation.getLatitude());
                intent.putExtra("lng", Common.mLastLocation.getLongitude());
                intent.putExtra("loc", String.valueOf(lastloc));
                intent.putExtra("destlat",  TxtRiderDestLat.getText().toString());
                intent.putExtra("destlng",tvRiderDestLng.getText().toString());
                intent.putExtra("dest", destnation.getText().toString());

                startActivity(intent);
            } else {
                StyleableToast.makeText(Home.this, "Tap your Destination to select", Toast.LENGTH_LONG, R.style.nolocation).show();
            }


        } else{
            StyleableToast.makeText(Home.this, "This is your pickup location find a car", Toast.LENGTH_LONG, R.style.success).show();

        }



    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        loadAllAvailableDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(cancelBroadcast);
        super.onDestroy();
    }
    private class ParseTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        ProgressDialog progressDialog = new ProgressDialog(Home.this);

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


}
