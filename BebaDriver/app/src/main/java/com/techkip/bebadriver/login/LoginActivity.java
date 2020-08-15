package com.techkip.bebadriver.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.techkip.bebadriver.DriverHome;
import com.techkip.bebadriver.MainActivity;
import com.techkip.bebadriver.R;
import com.techkip.bebadriver.common.Constants;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {
    AnimationDrawable animationDrawable;
    RelativeLayout relativeLayout;

    EditText DriverId, Status;
    Button LogIn ;
    String StatusHolder, DriverIdHolder;
    String finalResult ;
    TextView TxtSignUp;

    Boolean CheckEditText ;
    SpotsDialog spotsDialog;
    HashMap<String,String> hashMap = new HashMap<>();
    HttpParse httpParse = new HttpParse();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        relativeLayout =  findViewById(R.id.lay);
        animationDrawable = (AnimationDrawable) relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(5000);
        animationDrawable.setExitFadeDuration(2000);

        DriverId = findViewById(R.id.driverid);
        Status = findViewById(R.id.status);
        LogIn = findViewById(R.id.btn_login);
        TxtSignUp = findViewById(R.id.tvreg);


        TxtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reg = new Intent(LoginActivity.this,Register.class);
                startActivity(reg);
            }
        });

        Paper.init(this);
        String email = Paper.book().read(Constants.Params.USERMAIL_KEY);
        String password =Paper.book().read(Constants.Params.PWD_KEY);

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                DriverId.setText(account.getId());
                Status.setText("Approved");
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });

        if (email!=null && password !=null){
            if (!email.isEmpty() && !password.isEmpty())
                UserLoginFunction(email,password);
        }


        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CheckEditTextIsEmptyOrNot();

                if(CheckEditText){

                    UserLoginFunction(DriverIdHolder, StatusHolder);
                    Paper.book().write(Constants.Params.USERMAIL_KEY, DriverId.getText().toString());
                    Paper.book().write(Constants.Params.PWD_KEY, Status.getText().toString());
                }
                else {

                    Toast.makeText(LoginActivity.this, "Please fill all form fields.", Toast.LENGTH_LONG).show();

                }

            }
        });
    }

    public void CheckEditTextIsEmptyOrNot(){

        DriverIdHolder = DriverId.getText().toString();
        StatusHolder = Status.getText().toString();

        if(TextUtils.isEmpty(DriverIdHolder) || TextUtils.isEmpty(StatusHolder))
        {
            CheckEditText = false;
        }
        else {

            CheckEditText = true ;
        }
    }

    public void UserLoginFunction(final String email, final String password){

        class UserLoginClass extends AsyncTask<String,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                spotsDialog = new SpotsDialog(LoginActivity.this,R.style.ProgressKali);
                spotsDialog.show();
                spotsDialog.setMessage("Accessing Wait!.....");
                spotsDialog.setCancelable(false);
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {

                super.onPostExecute(httpResponseMsg);

                spotsDialog.dismiss();

                if(httpResponseMsg.equalsIgnoreCase("Data Matched")){

                    Intent intent = new Intent(LoginActivity.this, DriverHome.class);
                    startActivity(intent);
                    finish();
                }
                else{

                    Toast.makeText(LoginActivity.this,httpResponseMsg,Toast.LENGTH_LONG).show();
                    /// StyleableToast.makeText(LoginActivity.this, "Cant connect to your account re-try", Toast.LENGTH_SHORT, R.style.error).show();
                    /// Toast.makeText(LoginActivity.this, "Your Account isn't approved yet..", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            protected String doInBackground(String... params) {

                hashMap.put("driver_id",params[0]);

                hashMap.put("status",params[1]);

                finalResult = httpParse.postRequest(hashMap, Constants.URLs.LOGIN);

                return finalResult;
            }
        }

        UserLoginClass userLoginClass = new UserLoginClass();

        userLoginClass.execute(email,password);
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
}
