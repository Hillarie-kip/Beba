package com.techkip.bebarider;

import android.app.ProgressDialog;
import android.content.Intent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.ThemeUIManager;
import com.facebook.accountkit.ui.UIManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.techkip.bebarider.common.Common;
import com.techkip.bebarider.common.Constants;
import com.techkip.bebarider.login.HttpParse;
import com.techkip.bebarider.login.LoginActivity;
import com.techkip.bebarider.login.Register;
import com.techkip.bebarider.model.Rider;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1000;
    Button ContinueBtn;
    TextView TxtForgotPassword;
    MaterialEditText txtForgotPass;


    FirebaseDatabase mDatabase;
    DatabaseReference mUsers;

    AnimationDrawable animationDrawable;
    RelativeLayout RootLayout;


    String finalResult;


    ProgressDialog progressDialog;
    SpotsDialog spotsDialog;
    HashMap<String, String> hashMap = new HashMap<>();
    HttpParse httpParse = new HttpParse();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RootLayout = findViewById(R.id.rootlayout);
        animationDrawable = (AnimationDrawable) RootLayout.getBackground();
        animationDrawable.setEnterFadeDuration(5000);
        animationDrawable.setExitFadeDuration(2000);


        printKeyHash(); //used for fb
        mDatabase = FirebaseDatabase.getInstance();
        mUsers = mDatabase.getReference(Common.user_rider_table);

        Paper.init(this);
        final String email = Paper.book().read(Constants.Params.RIDER_ID_KEY);
        final String password = Paper.book().read(Constants.Params.STATUS_KEY);

        ContinueBtn = findViewById(R.id.btn_continue);



        ContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (email != null && password != null) {
                    if (!email.isEmpty() && !password.isEmpty())
                        UserLoginFunction(email, password);
                }
                else
                    SignInWithNumber();
            }
        });


        //Auto Login Number
        if (AccountKit.getCurrentAccessToken() != null) {
            spotsDialog = new SpotsDialog(this,R.style.ProgressKali);
            spotsDialog.show();
            spotsDialog.setMessage("authenticating.....");
            spotsDialog.setCancelable(true);
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(final Account account) {

                    mUsers.child(account.getId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //after assignre values
                                    Common.currentUser = dataSnapshot.getValue(Rider.class);

                                    //start activity
                                  /*  if (email != null && password != null) {
                                        if (!email.isEmpty() && !password.isEmpty())
                                            UserLoginFunction(email, password);
                                    }*/
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                    finish();
                                    spotsDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    //  finish();


                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });

        }


    }

    public void UserLoginFunction(final String email, final String password) {

        class UserLoginClass extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                spotsDialog = new SpotsDialog(MainActivity.this,R.style.ProgressKali);
                spotsDialog.show();
                spotsDialog.setMessage("re-authenticating.....");
                spotsDialog.setCancelable(false);

            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {

                super.onPostExecute(httpResponseMsg);

                spotsDialog.dismiss();

                if (httpResponseMsg.equalsIgnoreCase("Data Matched")) {



                    Intent intent = new Intent(MainActivity.this, Home.class);
                    startActivity(intent);
                    finish();

                } else {

                    // Toast.makeText(MainActivity.this, httpResponseMsg, Toast.LENGTH_LONG).show();
                    Toast.makeText(MainActivity.this, "Your Account isn't approved..", Toast.LENGTH_LONG).show();

                }

            }

            @Override
            protected String doInBackground(String... params) {

                hashMap.put("rider_id", params[0]);

                hashMap.put("status", params[1]);

                finalResult = httpParse.postRequest(hashMap, Constants.URLs.LOGIN);

                return finalResult;
            }
        }

        UserLoginClass userLoginClass = new UserLoginClass();

        userLoginClass.execute(email, password);
    }

    private void SignInWithNumber() {

        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intent, REQUEST_CODE);
        UIManager themeManager = new ThemeUIManager(R.style.AppTheme);
        configurationBuilder.setUIManager(themeManager);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {

            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
            finish();

        }
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.techkip.bebarider", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                //GET KEYHASH FROM LOGCAT
                Log.d("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (animationDrawable != null && !animationDrawable.isRunning())
            animationDrawable.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animationDrawable != null && animationDrawable.isRunning())
            animationDrawable.stop();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();

    }
}
