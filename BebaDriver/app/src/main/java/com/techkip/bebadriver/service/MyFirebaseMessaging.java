package com.techkip.bebadriver.service;


import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.techkip.bebadriver.CustomerCall;
import com.techkip.bebadriver.common.Common;
import com.techkip.bebadriver.model.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * ilitengenezwa na hillarie on 3/17/18.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
       updateTokenToServer(s);
        super.onNewToken(s);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //bcoz i will send f message wich has lat n lng frm rider app
        if (remoteMessage.getData() != null) {

            Map<String,String> data = remoteMessage.getData();
            String customer = data.get("customer");
            String lat = data.get("lat");
            String lng = data.get("lng");

            //  Intent intent = new Intent(MyFirebaseMessaging.this, CustomerCall.class);
            Intent intent = new Intent(getBaseContext(), CustomerCall.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            intent.putExtra("customer", customer);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);


        }
    }

    private void updateTokenToServer(String refreshToken) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference tokens = database.getReference(Common.token_table);

        final Token token = new Token(refreshToken);



        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                tokens.child(account.getId()).setValue(token);

            }

            @Override
            public void onError(AccountKitError accountKitError) {

               // Log.d("TOKEN_ERROR",accountKitError.getUserFacingMessage());
            }
        });



    }

}
