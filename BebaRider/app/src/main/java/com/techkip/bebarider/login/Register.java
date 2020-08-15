package com.techkip.bebarider.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
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
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.techkip.bebarider.Home;
import com.techkip.bebarider.R;
import com.techkip.bebarider.common.Common;
import com.techkip.bebarider.model.Rider;
import com.techkip.bebarider.stupidstuff.TermsActivity;


import java.util.HashMap;

import dmax.dialog.SpotsDialog;

import static com.techkip.bebarider.common.Constants.URLs.urlRegister;


public class Register extends AppCompatActivity {
    Button register;
    TextView login,terms;
    FirebaseDatabase mDatabase;
    DatabaseReference mUsers;

    EditText National_id, F_name, L_name, Car_plate, Car_seat, Licence;
    TextView TxtMSISDN;
    Spinner CarType, CarSeat;
    //String urlRegister = "http://192.168.43.192/Login/regi.php";
    String Rider_id_Holder, F_name_Holder, L_name_Holder, Phone_number_Holder, Car_type_Holder, Car_plate_Holder, Car_seat_Holder, Licence_Holder, Status_Holder, MSISDN_Holder;
    String finalResult;
    Boolean CheckEditText;
    HashMap<String, String> hashMap = new HashMap<>();
    HttpParse httpParse = new HttpParse();

    CheckBox ChkTerms;
    SpotsDialog spotsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Assign Id'S

        mDatabase = FirebaseDatabase.getInstance();
        mUsers = mDatabase.getReference(Common.user_rider_table);

        F_name = findViewById(R.id.et_firstname);
        L_name = findViewById(R.id.et_lastname);
        ChkTerms = findViewById(R.id.chk_terms);

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                Rider_id_Holder = account.getId();
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
        login = findViewById(R.id.txt_login);

        terms = findViewById(R.id.txt_terms);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(Register.this, LoginActivity.class);
                startActivity(login);
                finish();
            }
        });
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent terms = new Intent(Register.this, TermsActivity.class);
                startActivity(terms);

            }
        });

        //Adding Click Listener on button.
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Checking whether EditText is Empty or Not
                CheckEditTextIsEmptyOrNot();
                if (ChkTerms.isChecked()) {


                    if (CheckEditText) {

                        // If EditText is not empty and CheckEditText = True then this block will execute.

                        UserRegisterFunction(Rider_id_Holder, F_name_Holder, L_name_Holder, Phone_number_Holder, Status_Holder, MSISDN_Holder);

                    } else {

                        // If EditText is empty then this block will execute .
                        StyleableToast.makeText(Register.this, "please fill all the fields...", Toast.LENGTH_SHORT, R.style.error).show();

                    }


                } else
                    StyleableToast.makeText(Register.this, "You need to agree with terms n conditions", Toast.LENGTH_SHORT, R.style.upapproved).show();

            }
        });


    }

    public void CheckEditTextIsEmptyOrNot() {
        F_name_Holder = F_name.getText().toString();
        L_name_Holder = L_name.getText().toString();
        Status_Holder = "Approved";


        if (TextUtils.isEmpty(F_name_Holder)) {

            CheckEditText = false;

        } else {

            CheckEditText = true;
        }

    }

    public void UserRegisterFunction(final String riderid, final String fname, final String lname, final String phonenumber, final String dstatus, final String dmsisdn) {

        class UserRegisterFunctionClass extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                spotsDialog = new SpotsDialog(Register.this, R.style.ProgressKali);
                spotsDialog.show();
                spotsDialog.setMessage("Registering Account.....");
                spotsDialog.setCancelable(false);


            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {

                super.onPostExecute(httpResponseMsg);
                spotsDialog.dismiss();
                if (httpResponseMsg.equalsIgnoreCase("Registration Successfully")) {

                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(final Account account) {
                            spotsDialog = new SpotsDialog(Register.this, R.style.ProgressKali);
                            spotsDialog.show();
                            spotsDialog.setMessage("Finalizing.....");
                            spotsDialog.setCancelable(false);
                            final String userId = account.getId();
                            mUsers.orderByKey().equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.child(userId).exists()) {
                                                Rider rider = new Rider();
                                                rider.setPhone(Phone_number_Holder);
                                                rider.setName(F_name_Holder);
                                                rider.setProfilePicUrl("");
                                                rider.setRates("0.0");

                                                //RegisterFirebase
                                                mUsers.child(userId).setValue(rider).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        mUsers.child(userId)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        spotsDialog.dismiss();
                                                                        Common.currentUser = dataSnapshot.getValue(Rider.class);
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
                                                mUsers.child(userId)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                spotsDialog.dismiss();
                                                                Common.currentUser = dataSnapshot.getValue(Rider.class);
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

                } else {

                    Toast.makeText(Register.this, httpResponseMsg, Toast.LENGTH_LONG).show();

                }


            }

            @Override
            protected String doInBackground(String... params) {

                hashMap.put("rider_id", params[0]);

                hashMap.put("first_name", params[1]);

                hashMap.put("last_name", params[2]);

                hashMap.put("phone_number", params[3]);

                hashMap.put("status", params[4]);

                hashMap.put("msisdn", params[5]);


                finalResult = httpParse.postRequest(hashMap, urlRegister);

                return finalResult;
            }
        }

        UserRegisterFunctionClass userRegisterFunctionClass = new UserRegisterFunctionClass();

        userRegisterFunctionClass.execute(riderid, fname, lname, phonenumber, dstatus, dmsisdn);

    }

}