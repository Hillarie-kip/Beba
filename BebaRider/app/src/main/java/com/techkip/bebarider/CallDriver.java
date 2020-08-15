package com.techkip.bebarider;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.techkip.bebarider.common.Common;
import com.techkip.bebarider.common.Connector;
import com.techkip.bebarider.common.PicassoImage;
import com.techkip.bebarider.model.Rider;
import com.techkip.bebarider.remote.IFCMService;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

import static com.techkip.bebarider.common.Constants.URLs.Url_Send_Request;


public class CallDriver extends AppCompatActivity {


    CircleImageView DriverPic, CarPic;
    TextView TxtAvailability, TxtDriverName, TxtDriverPhone, TxtDriverRates, TxtCarType, TxtCarSeats, TxtCarPlate, TxtDest, TxtLoc,TxtDestLat,TxtDestLng;
    Button BtnCallApp, BtnCallPhone;

    Location mLastLocation;

    IFCMService mService;

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
    String loc;
    String dest;
    String destlat;
    String destlng;
    EditText tvDriverId, tvRiderId, tvRiderName, tvRiderPhone, tvRiderImage, tvRiderLat, tvRiderLng;
    String GetDriverIdTv, GetRiderIdTv, GetRiderNameTv, GetRiderPhoneTv, GetRiderImageTv, GetRiderLatTv, GetRiderLngTv, GetRiderLocTv, GetRiderDestLatTv, GetRiderDestLngTv, GetRiderDestTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_driver);
        mService = Common.getFCMService();
        loc = (getIntent().getStringExtra("loc"));
        TxtLoc = findViewById(R.id.tv_PickupLocation);
        destlat = (getIntent().getStringExtra("destlat"));
        destlng = (getIntent().getStringExtra("destlng"));
        dest = (getIntent().getStringExtra("dest"));

        TxtLoc.setText(loc);



        TxtDestLat = findViewById(R.id. tv_DestLat);
        TxtDestLng= findViewById(R.id. tv_DestLng);
        TxtDest = findViewById(R.id.tv_Destination);
        TxtDestLat.setText(destlat);
        TxtDestLng.setText(destlng);
        TxtDest.setText(dest);


        TxtAvailability = findViewById(R.id.txt_availability);
        DriverPic = findViewById(R.id.iv_DriverProfile);
        TxtDriverName = findViewById(R.id.tv_DriverName);
        TxtDriverPhone = findViewById(R.id.tv_DriverPhone);
        TxtDriverRates = findViewById(R.id.tv_DriverRates);


        CarPic = findViewById(R.id.iv_CarProfile);
        TxtCarType = findViewById(R.id.tv_CarType);
        TxtCarSeats = findViewById(R.id.tv_CarSeats);
        TxtCarPlate = findViewById(R.id.tv_CarPlate);

        BtnCallApp = findViewById(R.id.btn_call_app);
        BtnCallPhone = findViewById(R.id.btn_call_phone);


        tvDriverId = findViewById(R.id.tv_driverId);
        tvRiderId = findViewById(R.id.tv_riderId);
        tvRiderName = findViewById(R.id.tv_riderName);
        tvRiderPhone = findViewById(R.id.tv_riderPhone);
        tvRiderImage = findViewById(R.id.tv_riderImage);
        tvRiderLat = findViewById(R.id.tv_riderLat);
        tvRiderLng = findViewById(R.id.tv_riderLng);


        BtnCallApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TxtAvailability.getText().toString().equals("available")) {
                    final android.app.AlertDialog pd = new SpotsDialog(CallDriver.this);
                    pd.show();
                    pd.setMessage("Sending request....");
                    pd.setCancelable(true);

                    if (Common.driverId != null && !Common.driverId.isEmpty()) {
                        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {

                            @Override
                            public void onSuccess(Account account) {
                                id = account.getId();
                                UploadServer();
                                requestPickUpHere(account.getId());
                                pd.dismiss();
                                // UploadServer();
                                // Common.sendRequestToDriver(Common.driverId, mService, getBaseContext(), mLastLocation);


                            }

                            public void UploadServer() {
                                tvDriverId.setText(Common.driverId);
                                tvRiderId.setText(id);
                                tvRiderName.setText(Common.currentUser.getName());
                                tvRiderPhone.setText(Common.currentUser.getPhone());
                                tvRiderImage.setText(Common.currentUser.getProfilePicUrl());
                                tvRiderLat.setText(String.valueOf(mLastLocation.getLatitude()));
                                tvRiderLng.setText(String.valueOf(mLastLocation.getLongitude()));
                                TxtLoc.setText(loc);
                                TxtDest.setText(dest);

                                GetDriverIdTv = tvDriverId.getText().toString();
                                GetRiderIdTv = tvRiderId.getText().toString();
                                GetRiderNameTv = tvRiderName.getText().toString();
                                GetRiderPhoneTv = tvRiderPhone.getText().toString();
                                GetRiderImageTv = tvRiderImage.getText().toString();
                                GetRiderLatTv = tvRiderLat.getText().toString();
                                GetRiderLngTv = tvRiderLng.getText().toString();
                                GetRiderLocTv = TxtLoc.getText().toString();
                                GetRiderDestLatTv = TxtDestLat.getText().toString();
                                GetRiderDestLngTv = TxtDestLng.getText().toString();
                                GetRiderDestTv = TxtDest.getText().toString();
                                class AsyncTaskUploadClass extends AsyncTask<Void, Void, String> {

                                    @Override
                                    protected void onPreExecute() {

                                        super.onPreExecute();

                                        progressDialog = ProgressDialog.show(CallDriver.this, "", "Processing...", false);


                                    }

                                    @SuppressLint("WrongConstant")
                                    @Override
                                    protected void onPostExecute(String string1) {

                                        super.onPostExecute(string1);

                                        Common.sendRequestToDriver(Common.driverId, mService, getBaseContext(), mLastLocation);

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
                            public void onError(AccountKitError accountKitError) {

                            }
                        });

                    }
                } else {
                    StyleableToast.makeText(CallDriver.this, "This Driver is Busy for now", Toast.LENGTH_SHORT, R.style.error).show();
                   // Toast.makeText(CallDriver.this, "Driver is Busy pick another", Toast.LENGTH_SHORT).show();
                }


            }


        });
        BtnCallPhone.setOnClickListener(new View.OnClickListener() {
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
        if (getIntent() != null) {
            Common.driverId = getIntent().getStringExtra("driverId");
            double lat = getIntent().getDoubleExtra("lat", -0.1);
            double lng = getIntent().getDoubleExtra("lng", -0.1);

            mLastLocation = new Location("");
            mLastLocation.setLatitude(lat);
            mLastLocation.setLongitude(lng);


            loadDriverInfo(Common.driverId);


        }


    }

    private void requestPickUpHere(final String uid) {
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

    private void loadDriverInfo(String driverId) {

        FirebaseDatabase.getInstance().getReference(Common.user_driver_table).child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Rider driverUser = dataSnapshot.getValue(Rider.class);

                TxtAvailability.setText(driverUser.getAvailability());
                TxtDriverName.setText(driverUser.getName());
                TxtDriverPhone.setText(driverUser.getPhone());
                TxtDriverRates.setText(driverUser.getRates());
                PicassoImage.downloadImage(CallDriver.this, driverUser.getCarPicUrl(), CarPic);
                PicassoImage.downloadImage(CallDriver.this, driverUser.getProfilePicUrl(), DriverPic);
                TxtCarType.setText(driverUser.getCarType());
                TxtCarSeats.setText(driverUser.getCarSeats());
                TxtCarPlate.setText(driverUser.getCarPlate());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


}
