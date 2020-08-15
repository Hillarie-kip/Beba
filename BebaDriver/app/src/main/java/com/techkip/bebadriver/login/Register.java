package com.techkip.bebadriver.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.techkip.bebadriver.DriverHome;
import com.techkip.bebadriver.MainActivity;
import com.techkip.bebadriver.R;
import com.techkip.bebadriver.common.Common;
import com.techkip.bebadriver.model.Driver;
import com.techkip.bebadriver.model.Token;
import com.techkip.bebadriver.remote.IFCMService;
import com.techkip.bebadriver.stupdstuff.TermsActivity;


import java.util.HashMap;

import dmax.dialog.SpotsDialog;

import static com.techkip.bebadriver.common.Constants.URLs.urlRegister;


public class Register extends AppCompatActivity {
    Button register;
    TextView TxtTerms,TxtLogin;
    FirebaseDatabase mDatabase;
    DatabaseReference mUsers;

    EditText National_id, F_name, L_name, Car_plate, Car_seat, Licence;
    TextView TxtMSISDN;
    Spinner CarType,CarSeat;
    //String urlRegister = "http://192.168.43.192/Login/regi.php";
    String Driver_id_Holder, National_id_Holder, F_name_Holder, L_name_Holder, Phone_number_Holder, Car_type_Holder, Car_plate_Holder, Car_seat_Holder, Licence_Holder, Status_Holder, MSISDN_Holder;
    String finalResult;
    Boolean CheckEditText;
    SpotsDialog spotsDialog;
    HashMap<String, String> hashMap = new HashMap<>();
    HttpParse httpParse = new HttpParse();

    CheckBox ChkTerms;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Assign Id'S

        mDatabase = FirebaseDatabase.getInstance();
        mUsers = mDatabase.getReference(Common.user_driver_table);

        National_id = findViewById(R.id.et_nationalId);
        F_name = findViewById(R.id.et_firstname);
        L_name = findViewById(R.id.et_lastname);
        Car_plate = findViewById(R.id.et_carplate);
        Licence = findViewById(R.id.et_driverLicence);
        CarType=findViewById(R.id.sp_cartype);
        CarSeat=findViewById(R.id.sp_carseat);
        ChkTerms=findViewById(R.id.chk_terms);

        TxtTerms = findViewById(R.id.txt_terms);
        TxtLogin = findViewById(R.id.txt_login);

        TxtTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent terms= new Intent(Register.this, TermsActivity.class);
                startActivity(terms);
            }
        });

        TxtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(Register.this,LoginActivity.class);
                startActivity(login);
            }
        });

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                Driver_id_Holder = account.getId();
                Phone_number_Holder = String.valueOf(account.getPhoneNumber());
                String msisdn = String.valueOf(account.getPhoneNumber());
                String strNew = msisdn.replace("+", "");
                MSISDN_Holder = strNew;

            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }


        });

        register = findViewById(R.id.Submit);

        ArrayAdapter<String> industry = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.carType)) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        industry.setDropDownViewResource(R.layout.spinner_item);
        CarType.setAdapter(industry);
        CarType.setSelection(0);

        ArrayAdapter<String> seat = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.carSeat)) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        seat.setDropDownViewResource(R.layout.spinner_item);
        CarSeat.setAdapter(seat);
        CarSeat.setSelection(0);

        //Adding Click Listener on button.
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Checking whether EditText is Empty or Not
                CheckEditTextIsEmptyOrNot();
                if (ChkTerms.isChecked()){


                if (CheckEditText) {

                    // If EditText is not empty and CheckEditText = True then this block will execute.

                    UserRegisterFunction(Driver_id_Holder, National_id_Holder, F_name_Holder, L_name_Holder, Phone_number_Holder, Car_type_Holder, Car_plate_Holder, Car_seat_Holder, Licence_Holder, Status_Holder, MSISDN_Holder);

                } else {

                    // If EditText is empty then this block will execute .
                    Toast.makeText(Register.this, " fill all the fields...", Toast.LENGTH_LONG).show();

                }


            }else
                    Toast.makeText(Register.this, "You need to agree with terms n conditions", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void updateTokenToServer() {
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
                        Log.e("ERROR_TOKEN",e.getMessage());
                        Toast.makeText(Register.this, " unable to solve" +e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.d("TOKEN_ERROR",accountKitError.getUserFacingMessage());
            }
        });



    }

    public void CheckEditTextIsEmptyOrNot() {

                National_id_Holder = National_id.getText().toString();
                F_name_Holder = F_name.getText().toString();
                L_name_Holder = L_name.getText().toString();
                Car_type_Holder = CarType.getSelectedItem().toString();
                Car_plate_Holder = Car_plate.getText().toString();
                Car_seat_Holder = CarSeat.getSelectedItem().toString();
                Licence_Holder = Licence.getText().toString();
                Status_Holder = "Approved";


        if (TextUtils.isEmpty(Car_plate_Holder)||TextUtils.isEmpty(Licence_Holder)||TextUtils.isEmpty(F_name_Holder) || TextUtils.isEmpty(L_name_Holder) || TextUtils.isEmpty(National_id_Holder)||Car_type_Holder.equals("Choose The Car Type ..")||Car_seat_Holder.equals("Choose Number of seats ..")) {

            CheckEditText = false;

        } else {

            CheckEditText = true;
        }

    }

    public void UserRegisterFunction(final String driverid, final String nationalid, final String fname, final String lname, final String phonenumber, final String cartype, final String carplate, final String carseat, final String dlicence, final String dstatus, final String dmsisdn) {

        class UserRegisterFunctionClass extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                spotsDialog = new SpotsDialog(Register.this,R.style.ProgressKali);
                spotsDialog.show();
                spotsDialog.setMessage("Registering Account.....");
                spotsDialog.setCancelable(false);
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {

                super.onPostExecute(httpResponseMsg);

                spotsDialog.dismiss();

                if(httpResponseMsg.equalsIgnoreCase("Registration Successfully")){
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(final Account account) {
                            spotsDialog = new SpotsDialog(Register.this, R.style.ProgressKali);
                            spotsDialog.show();
                            spotsDialog.setMessage("Finalizing.....");
                            spotsDialog.setCancelable(false);
                           final String userId = account.getId();
                            mUsers.orderByKey().equalTo(userId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.child(userId).exists()) {
                                                Driver driver = new Driver();
                                                driver.setPhone(Phone_number_Holder);
                                                driver.setName(F_name_Holder);
                                                driver.setProfilePicUrl("");
                                                driver.setRates("0.0");
                                                driver.setCarType(Car_type_Holder); //Default
                                                driver.setCarPicUrl("");
                                                driver.setAvailability("UnAvailable");
                                                driver.setCarSeats(Car_seat_Holder);
                                                driver.setCarPlate(Car_plate_Holder);
                                                //RegisterFirebase
                                                mUsers.child(userId).setValue(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        mUsers.child(userId)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        Common.currentDriver = dataSnapshot.getValue(Driver.class);
                                                                        updateTokenToServer();
                                                                        spotsDialog.dismiss();
                                                                        Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                                                        Intent homeIntent = new Intent(Register.this, LoginActivity.class);
                                                                        startActivity(homeIntent);
                                                                        finish();




                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });


                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Register.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else { //if user iko login direct
                                                mUsers.child(account.getId())
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                Common.currentDriver = dataSnapshot.getValue(Driver.class);
                                                                updateTokenToServer();
                                                                spotsDialog.dismiss();
                                                                Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                                                Intent homeIntent = new Intent(Register.this, LoginActivity.class);
                                                                startActivity(homeIntent);
                                                                finish();


                                                            }


                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(Register.this, "" + accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

                }
                else{

                    Toast.makeText(Register.this,httpResponseMsg,Toast.LENGTH_LONG).show();

                }

            }

            @Override
            protected String doInBackground(String... params) {

                hashMap.put("driver_id", params[0]);

                hashMap.put("national_id", params[1]);

                hashMap.put("first_name", params[2]);

                hashMap.put("last_name", params[3]);

                hashMap.put("phone_number", params[4]);

                hashMap.put("car_type", params[5]);

                hashMap.put("car_plate", params[6]);

                hashMap.put("car_seat", params[7]);

                hashMap.put("driver_licence", params[8]);

                hashMap.put("status", params[9]);

                hashMap.put("msisdn", params[10]);


                finalResult = httpParse.postRequest(hashMap, urlRegister);

                return finalResult;
            }
        }

        UserRegisterFunctionClass userRegisterFunctionClass = new UserRegisterFunctionClass();

        userRegisterFunctionClass.execute(driverid, nationalid, fname, lname, phonenumber, cartype, carplate, carseat, dlicence, dstatus, dmsisdn);

    }

}