package com.techkip.bebadriver;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.techkip.bebadriver.stupdstuff.AboutActivity;
import com.techkip.bebadriver.stupdstuff.Dev.ProfileDev;
import com.techkip.bebadriver.stupdstuff.HelpActivity;
import com.techkip.bebadriver.stupdstuff.HowActivity;
import com.techkip.bebadriver.stupdstuff.PrivacyActivity;
import com.techkip.bebadriver.stupdstuff.TermsActivity;
import com.techkip.bebadriver.common.Common;
import com.techkip.bebadriver.common.PicassoImage;
import com.techkip.bebadriver.history.History;
import com.techkip.bebadriver.model.Driver;
import com.techkip.bebadriver.model.Token;
import com.techkip.bebadriver.remote.IGoogleAPI;
import com.techkip.bebadriver.updates.ForceUpdateChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,ForceUpdateChecker.OnUpdateNeededListener {


    MaterialEditText TxtName, TxtPhone, TxtPlateNumber;
    TextView TxtDriverName, TxtDriverRate;
    CircleImageView IvProfilePic, ivDriverProfile, Iv_carPic;
    RadioButton RbtnBebaBronze, RbtnBebaSilver, RbtnBebaGold, RbtnBebaBoda;
    RadioButton RbtnOne, RbtnTwo, RbtnThree, RbtnFour, RbtnFive, RbtnSix;

    private GoogleMap mMap;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    //play service
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference drivers;
    GeoFire geoFire;
    Marker mCurrent;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;

    //car animation
    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition, currentPosition;
    private int index, next;
    private PlaceAutocompleteFragment places;
    AutocompleteFilter typeFilter;
    private String destination;
    private PolylineOptions polylineOptions, blackPolylineOPtions;
    private Polyline blackPolyline, greyPolyline;

    private IGoogleAPI mService;

    //presence
    DatabaseReference onlineRef, currentUserRef;
    FirebaseStorage mfirebaseStorage;
    StorageReference mstorageRef;

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index < polyLineList.size() - 1) {
                index++;
                next = index + 1;
            }
            if (index < polyLineList.size() - 1) {

                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);
            }

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                    lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                    LatLng newPosition = new LatLng(lat, lng);

                    carMarker.setPosition(newPosition);
                    carMarker.setAnchor(0.5f, 0.5f);
                    carMarker.setRotation(getBearing(startPosition, newPosition));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPosition)
                                    .zoom(15.5f)
                                    .build()
                    ));


                }
            });
            valueAnimator.start();
            handler.postDelayed(this, 3000);
        }
    };


    private float getBearing(LatLng startPosition, LatLng endPosition) {

        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);
        if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check(); //for updates
        mfirebaseStorage = FirebaseStorage.getInstance();
        mstorageRef = mfirebaseStorage.getReference();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navigationHeaderView = navigationView.getHeaderView(0);
        TxtDriverName = navigationHeaderView.findViewById(R.id.tv_DriverName);
        TxtDriverRate = navigationHeaderView.findViewById(R.id.tv_Rate);
        ivDriverProfile = navigationHeaderView.findViewById(R.id.iv_DriverProfile);
        TxtDriverName.setText(Common.currentDriver.getName());
        TxtDriverRate.setText(Common.currentDriver.getRates());


        PicassoImage.downloadImage(DriverHome.this, Common.currentDriver.getProfilePicUrl(), ivDriverProfile);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


     /*   //presence using firebASE
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
                currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_table)
                        .child(Common.currentDriver.getCarType())
                        .child(account.getId());

                onlineRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUserRef.onDisconnect().removeValue();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });
*/
        //Init View
        location_switch = findViewById(R.id.location_switch);
        final Button OffOn = findViewById(R.id.btn_offon);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {
                    FirebaseDatabase.getInstance().goOnline();
                    if (ActivityCompat.checkSelfPermission(DriverHome.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(DriverHome.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    buildLocationRequest();
                    buildLocationCallBack();
                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());

                    drivers = FirebaseDatabase.getInstance().getReference(Common.driver_table).child(Common.currentDriver.getCarType());
                    geoFire = new GeoFire(drivers);
                    displayLocation();
                    OffOn.setText("You are Online");
                    OffOn.setBackgroundResource(R.drawable.button_on);
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            DatabaseReference driverInfo = FirebaseDatabase.getInstance().getReference(Common.user_driver_table);
                            driverInfo.child(account.getId()).child("availability").setValue("available");
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {

                        }
                    });


                } else {

                    FirebaseDatabase.getInstance().goOffline();
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    if (mCurrent != null) {
                        mCurrent.remove();

                    }

                    mMap.clear();
                    if (handler != null)
                        handler.removeCallbacks(drawPathRunnable);
                    OffOn.setText("You are Offline");
                    OffOn.setBackgroundResource(R.drawable.button_off);


                }

            }
        });

        polyLineList = new ArrayList<>();


        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();
        places = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setHint("Enter Location..");
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (location_switch.isChecked()) {

                    destination = place.getAddress().toString();
                    destination = destination.replace(" ", "+");
                    getDirection();
                } else {
                    StyleableToast.makeText(DriverHome.this, "you must be ONLINE", Toast.LENGTH_SHORT, R.style.nolocation).show();

                }

            }

            @Override
            public void onError(Status status) {
                Toast.makeText(DriverHome.this, "" + status.toString(), Toast.LENGTH_SHORT).show();

            }
        });


        setUpLocation();
        mService = Common.getGoogleAPI();

        updateFirebaseToken();


        //start if gps is not checked
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

   /* @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please, update app to new version to continue reposting.")
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton("No, thanks",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create();
        dialog.show();
    }*/

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


        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference tokens = database.getReference(Common.token_table);
                Token token = new Token(FirebaseInstanceId.getInstance().getToken());
                tokens.child(account.getId()).setValue(token);
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });

    }

    private void getDirection() {
        currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions" +
                    "/json?mode=driving&transit_routing_preference=less_driving&origin=" + currentPosition.latitude + "," + currentPosition.longitude +
                    "&destination=" + destination +
                    "&key=" + getResources().getString(R.string.google_direction_api);//browser api key

            Log.d("Hillare", requestApi);
            //PRINT Url for debug

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);

                                    //adjust bounds

                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    for (LatLng latLng : polyLineList) builder.include(latLng);
                                    LatLngBounds bounds = builder.build();
                                    CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                                    mMap.animateCamera(mCameraUpdate);

                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(R.color.colorPrimaryDark);
                                    polylineOptions.width(5);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.endCap(new SquareCap());
                                    polylineOptions.jointType(JointType.ROUND);
                                    polylineOptions.addAll(polyLineList);
                                    greyPolyline = mMap.addPolyline(polylineOptions);


                                    blackPolylineOPtions = new PolylineOptions();
                                    blackPolylineOPtions.color(R.color.red);
                                    blackPolylineOPtions.width(5);
                                    blackPolylineOPtions.startCap(new SquareCap());
                                    blackPolylineOPtions.endCap(new SquareCap());
                                    blackPolylineOPtions.jointType(JointType.ROUND);
                                    blackPolyline = mMap.addPolyline(blackPolylineOPtions);

                                    mMap.addMarker(new MarkerOptions()
                                            .position(polyLineList.get(polyLineList.size() - 1))
                                            .title("Customer Pick Up"));

                                    //ANimation
                                    ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
                                    polyLineAnimator.setDuration(2000);
                                    polyLineAnimator.setInterpolator(new LinearInterpolator());
                                    polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                            List<LatLng> points = greyPolyline.getPoints();
                                            int percentValue = (int) valueAnimator.getAnimatedValue();
                                            int size = points.size();
                                            int newPoints = (int) (size * (percentValue / 100.0f));
                                            List<LatLng> p = points.subList(0, newPoints);
                                            blackPolyline.setPoints(p);
                                        }
                                    });
                                    polyLineAnimator.start();
                                    if (Common.currentDriver.getCarType().equals("Beba Bronze")) {
                                        carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                                .flat(true)
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_bronze)));

                                    }
                                    if (Common.currentDriver.getCarType().equals("Beba Silver")) {
                                        carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                                .flat(true)
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_silver)));

                                    }
                                    if (Common.currentDriver.getCarType().equals("Beba Gold")) {
                                        carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                                .flat(true)
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_gold)));

                                    }
                                    if (Common.currentDriver.getCarType().equals("Beba Boda")) {
                                        carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                                .flat(true)
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bike)));

                                    }

                                    handler = new Handler();
                                    index = -1;
                                    next = 1;
                                    handler.postDelayed(drawPathRunnable, 3000);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverHome.this, "Google Map Error : " + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildLocationCallBack();
                    buildLocationRequest();
                    if (location_switch.isChecked())

                        displayLocation();

                }

        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request Run Time
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);


        } else {
            buildLocationRequest();
            buildLocationCallBack();
            if (location_switch.isChecked()) {
                drivers = FirebaseDatabase.getInstance().getReference(Common.driver_table).child(Common.currentDriver.getCarType());
                geoFire = new GeoFire(drivers);
            }

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
                for (Location location : locationResult.getLocations()) {
                    Common.mLastLocation = location;
                }

                displayLocation();

            }
        };

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
                    if (location_switch.isChecked()) {

                        final double latitude = Common.mLastLocation.getLatitude();
                        final double longitude = Common.mLastLocation.getLongitude();


                        //create LatLng of lastLocation to be center point
                        //100000 in distance around AND 0 is northeast,90  is east,180 is south  270 is west
                        LatLng center = new LatLng(latitude, longitude);

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
                        places.setBoundsBias(bounds);
                        places.setFilter(typeFilter);

                        //Update Firebase


                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                            @Override
                            public void onSuccess(Account account) {
                                geoFire.setLocation(account.getId(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        if (mCurrent != null)
                                            mCurrent.remove();
                                        if (Common.currentDriver.getCarType().equals("Beba Bronze")) {
                                            mCurrent = mMap.addMarker(new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_bronze))
                                                    .position(new LatLng(latitude, longitude))
                                                    .title("Your Location"));
                                        }
                                        if (Common.currentDriver.getCarType().equals("Beba Silver")) {
                                            mCurrent = mMap.addMarker(new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_silver))
                                                    .position(new LatLng(latitude, longitude))
                                                    .title("Your Location"));
                                        }
                                        if (Common.currentDriver.getCarType().equals("Beba Gold")) {
                                            mCurrent = mMap.addMarker(new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.beba_gold))
                                                    .position(new LatLng(latitude, longitude))
                                                    .title("Your Location"));
                                        }
                                        if (Common.currentDriver.getCarType().equals("Beba Boda")) {
                                            mCurrent = mMap.addMarker(new MarkerOptions()
                                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bike))
                                                    .position(new LatLng(latitude, longitude))
                                                    .title("Your Location"));
                                        }
                                        //move cam to tz postion
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
                                        //rotateMarker(mCurrent, -360, mMap);
                                    }
                                });
                            }

                            @Override
                            public void onError(AccountKitError accountKitError) {

                            }
                        });
                    }
                } else {
                    Log.d("Error", "cannot get your location");
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setIcon(R.mipmap.ic_logout);
            alertDialogBuilder.setMessage("Are you sure you want to exit?");
            alertDialogBuilder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            finishAffinity();

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            Intent intent = new Intent(DriverHome.this, DriverHome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        if (id == R.id.menu_help) {
            Intent intent = new Intent(DriverHome.this, HelpActivity.class);
            startActivity(intent);

        }
        if (id == R.id.menu_terms) {
            Intent intent = new Intent(DriverHome.this, TermsActivity.class);
            startActivity(intent);

        }
        if (id == R.id.menu_about) {
            Intent intent = new Intent(DriverHome.this, AboutActivity.class);
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
        } else if (id == R.id.nav_carProfile) {
            updateCarInfoDialog();

        } else if (id == R.id.nav_tripHistory) {
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    String driverId = account.getId();
                    Intent intent = new Intent(DriverHome.this, History.class);
                    intent.putExtra("driverId", driverId);
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
        } else if (id == R.id.nav_share) {
            share();
        } else if (id == R.id.nav_help) {
        Intent help = new Intent(DriverHome.this,HelpActivity.class);
        startActivity(help);

    } else if (id == R.id.nav_privacy) {
        Intent privacy = new Intent(DriverHome.this,PrivacyActivity.class);
        startActivity(privacy);
    }
        else if (id == R.id.nav_developer) {
        Intent prof = new Intent(DriverHome.this,ProfileDev.class);
        startActivity(prof);

    }
        else if (id == R.id.nav_how) {
        Intent how = new Intent(DriverHome.this,HowActivity.class);
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

        final AlertDialog.Builder updateInfoDialog = new AlertDialog.Builder(this);
        updateInfoDialog.setTitle("UPDATE ACCOUNT");
        LayoutInflater inflater = LayoutInflater.from(this);

        View update_Layout = inflater.inflate(R.layout.layout_updatedriverinfo, null);

        TxtName = update_Layout.findViewById(R.id.et_Name);
        TxtPhone = update_Layout.findViewById(R.id.et_PhoneNumber);
        IvProfilePic = update_Layout.findViewById(R.id.iv_Profile);

        IvProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        PicassoImage.downloadImage(DriverHome.this, Common.currentDriver.getProfilePicUrl(), IvProfilePic);
        TxtName.setText(Common.currentDriver.getName());
        TxtPhone.setText(Common.currentDriver.getPhone());

        updateInfoDialog.setView(update_Layout);
        updateInfoDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.dismiss();
                final SpotsDialog progressDialog = new SpotsDialog(DriverHome.this);
                progressDialog.show();


                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        String name = TxtName.getText().toString();
                        String phone = TxtPhone.getText().toString();

                        Map<String, Object> updateInfo = new HashMap<>();

                        if (!TextUtils.isEmpty(name))
                            updateInfo.put("name", name);
                        if (!TextUtils.isEmpty(phone))
                            updateInfo.put("phone", phone);

                        DatabaseReference driverInfo = FirebaseDatabase.getInstance().getReference(Common.user_driver_table);
                        driverInfo.child(account.getId())
                                .updateChildren(updateInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            StyleableToast.makeText(DriverHome.this, "Info Successfully Updated", Toast.LENGTH_SHORT, R.style.success).show();



                                        else
                                            StyleableToast.makeText(DriverHome.this, "Unsuccessful info update", Toast.LENGTH_SHORT, R.style.error).show();

                                        progressDialog.dismiss();
                                    }

                                });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });


            }
        });


        updateInfoDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        updateInfoDialog.show();


    }

    private void updateCarInfoDialog() {

        final AlertDialog.Builder updateCarInfoDialog = new AlertDialog.Builder(this);
        updateCarInfoDialog.setTitle("UPDATE CAR INFO ");
        LayoutInflater inflater = LayoutInflater.from(this);
        View update_Layout = inflater.inflate(R.layout.layout_updatecarinfo, null);


        TxtPlateNumber = update_Layout.findViewById(R.id.et_PlateNumber);
        Iv_carPic = update_Layout.findViewById(R.id.iv_carProfile);
        Iv_carPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                carImage();
            }
        });
        PicassoImage.downloadImage(DriverHome.this, Common.currentDriver.getCarPicUrl(), Iv_carPic);
        TxtPlateNumber.setText(Common.currentDriver.getCarPlate());

        RbtnBebaBronze = update_Layout.findViewById(R.id.rbtn_bebaBronze);
        RbtnBebaSilver = update_Layout.findViewById(R.id.rbtn_bebaSilver);
        RbtnBebaGold = update_Layout.findViewById(R.id.rbtn_bebaGold);
        RbtnBebaBoda = update_Layout.findViewById(R.id.rbtn_bebaBoda);

        RbtnOne = update_Layout.findViewById(R.id.rbtn_one);
        RbtnTwo = update_Layout.findViewById(R.id.rbtn_two);
        RbtnThree = update_Layout.findViewById(R.id.rbtn_three);
        RbtnFour = update_Layout.findViewById(R.id.rbtn_four);
        RbtnFive = update_Layout.findViewById(R.id.rbtn_five);
        RbtnSix = update_Layout.findViewById(R.id.rbtn_six);


        //load default
        if (Common.currentDriver.getCarType().equals("Beba Bronze"))
            RbtnBebaBronze.setChecked(true);
        else if (Common.currentDriver.getCarType().equals("Beba Silver"))
            RbtnBebaSilver.setChecked(true);
        else if (Common.currentDriver.getCarType().equals("Beba Gold"))
            RbtnBebaGold.setChecked(true);
        else if (Common.currentDriver.getCarType().equals("Beba Boda"))
            RbtnBebaBoda.setChecked(true);

        if (Common.currentDriver.getCarSeats().equals("1"))
            RbtnOne.setChecked(true);
        else if (Common.currentDriver.getCarSeats().equals("2"))
            RbtnTwo.setChecked(true);
        else if (Common.currentDriver.getCarSeats().equals("3"))
            RbtnThree.setChecked(true);
        else if (Common.currentDriver.getCarSeats().equals("4"))
            RbtnFour.setChecked(true);
        else if (Common.currentDriver.getCarSeats().equals("5"))
            RbtnFive.setChecked(true);
        else if (Common.currentDriver.getCarSeats().equals("6"))
            RbtnSix.setChecked(true);


        updateCarInfoDialog.setView(update_Layout);
        updateCarInfoDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.dismiss();
                final SpotsDialog progressDialog = new SpotsDialog(DriverHome.this);
                progressDialog.show();


                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(final Account account) {
                        Map<String, Object> updateInfo = new HashMap<>();
                        String plate = TxtPlateNumber.getText().toString();

                        if (!TextUtils.isEmpty(plate))
                            updateInfo.put("carPlate", plate);
                        if (RbtnBebaBronze.isChecked())
                            updateInfo.put("carType", RbtnBebaBronze.getText().toString());
                        else if (RbtnBebaSilver.isChecked())
                            updateInfo.put("carType", RbtnBebaSilver.getText().toString());
                        else if (RbtnBebaGold.isChecked())
                            updateInfo.put("carType", RbtnBebaGold.getText().toString());
                        else if (RbtnBebaBoda.isChecked())
                            updateInfo.put("carType", RbtnBebaBoda.getText().toString());


                        if (RbtnOne.isChecked())
                            updateInfo.put("carSeats", RbtnOne.getText().toString());
                        else if (RbtnTwo.isChecked())
                            updateInfo.put("carSeats", RbtnTwo.getText().toString());
                        else if (RbtnThree.isChecked())
                            updateInfo.put("carSeats", RbtnThree.getText().toString());
                        else if (RbtnFour.isChecked())
                            updateInfo.put("carSeats", RbtnFour.getText().toString());
                        else if (RbtnFive.isChecked())
                            updateInfo.put("carSeats", RbtnFive.getText().toString());
                        else if (RbtnSix.isChecked())
                            updateInfo.put("carSeats", RbtnSix.getText().toString());

                        DatabaseReference driverInfo = FirebaseDatabase.getInstance().getReference(Common.user_driver_table);
                        driverInfo.child(account.getId())
                                .updateChildren(updateInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_table)
                                                    .child(Common.currentDriver.getCarType())
                                                    .child(account.getId());
                                            StyleableToast.makeText(DriverHome.this, "Car info Successfully Updated", Toast.LENGTH_SHORT, R.style.success).show();

                                        } else
                                            StyleableToast.makeText(DriverHome.this, "Unsuccessful Update", Toast.LENGTH_SHORT, R.style.error).show();

                                        progressDialog.dismiss();
                                    }

                                });
                        //refresh
                        driverInfo.child(account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Common.currentDriver = dataSnapshot.getValue(Driver.class);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });


            }
        });


        updateCarInfoDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        updateCarInfoDialog.show();


    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), Common.GALLERY_REQUEST_ONE);

    }

    private void carImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Car Picture"), Common.GALLERY_REQUEST_TWO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.GALLERY_REQUEST_ONE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri saveUri = data.getData();
            if (saveUri != null) {

                final ProgressDialog pd = new ProgressDialog(this);
                pd.setMessage("Uploading image..");
                pd.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = mstorageRef.child("images").child("Drivers/" + Common.currentDriver.getPhone() + imageName);
                imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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

                                        DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_table);
                                        driverInformation.child(account.getId())
                                                .updateChildren(picUpdate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                            StyleableToast.makeText(DriverHome.this, "Image Uploaded", Toast.LENGTH_SHORT, R.style.error).show();


                                                        PicassoImage.downloadImage(DriverHome.this, Common.currentDriver.getProfilePicUrl(), IvProfilePic);


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
                                pd.setMessage("uploaded " + progress + "%");
                            }
                        });


            }
        }
        if (requestCode == Common.GALLERY_REQUEST_TWO && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri saveUri = data.getData();
            if (saveUri != null) {

                final ProgressDialog pd = new ProgressDialog(this);
                pd.setMessage("Uploading image..");
                pd.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = mstorageRef.child("images").child("Cars/" + Common.currentDriver.getPhone() + imageName);
                imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                        picUpdate.put("carPicUrl", uri.toString());

                                        DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_table);
                                        driverInformation.child(account.getId())
                                                .updateChildren(picUpdate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                            StyleableToast.makeText(DriverHome.this, "Car Image Uploaded", Toast.LENGTH_SHORT, R.style.success).show();

                                                        else
                                                            StyleableToast.makeText(DriverHome.this, "Error Uploading", Toast.LENGTH_SHORT, R.style.error).show();

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
                                pd.setMessage("uploaded " + progress + "%");
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
                        Intent intent = new Intent(DriverHome.this, MainActivity.class);
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

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildLocationCallBack();
        buildLocationRequest();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
    }


    @Override
    protected void onResume() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
                currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_table)
                        .child(Common.currentDriver.getCarType())
                        .child(account.getId());

                onlineRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUserRef.onDisconnect().removeValue();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        FirebaseDatabase.getInstance().goOffline();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);


        mMap.clear();
        if (handler != null) {
            handler.removeCallbacks(drawPathRunnable);
            mCurrent.remove();
        }
        super.onDestroy();


    }


}
