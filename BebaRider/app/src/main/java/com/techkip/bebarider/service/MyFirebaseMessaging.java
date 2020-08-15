package com.techkip.bebarider.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.techkip.bebarider.Helper.NotificationHelper;
import com.techkip.bebarider.R;
import com.techkip.bebarider.RateActivity;
import com.techkip.bebarider.Tracking;
import com.techkip.bebarider.common.Common;
import com.techkip.bebarider.model.Token;

import java.util.Map;


/**
 * ilitengenezwa na hillarie on 3/17/18.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        updateTokenToServer(s);
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
                //Log.d("TOKEN_ERROR",accountKitError.getUserFacingMessage());
            }
        });



    }
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        if (remoteMessage.getData() != null) {
            Map<String,String> data = remoteMessage.getData();
            String title = data.get("title");
            final String message = data.get("message");
            final String time = data.get("time");
            final String driverPic = data.get("driverPic");
            final String carPic = data.get("carPic");
            final String driverName = data.get("driverName");
            final String driverPhone = data.get("driverPhone");
            final String carPlate = data.get("carPlate");

            if (title.equals("Cancel")) {

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(MyFirebaseMessaging.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
                LocalBroadcastManager.getInstance(MyFirebaseMessaging.this)
                        .sendBroadcast(new Intent(Common.CANCEL_BROADCAST_STRING));

            }
                else if (title.equals("Accept")) {

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(MyFirebaseMessaging.this, message, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getBaseContext(), Tracking.class);
                            intent.putExtra("time", time);
                            intent.putExtra("driverPic", driverPic);
                            intent.putExtra("carPic", carPic);
                            intent.putExtra("driverName", driverName);
                            intent.putExtra("driverPhone", driverPhone);
                            intent.putExtra("carPlate", carPlate);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });


            } else if (title.equals("Arrived")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    showArrivedNotificationAPI26(message);
                else
                    showArrivedNotification(message);
            } else if(title.equals("DropOff")) { //from driver tracking

                OpenRatesActivity(message);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String body) {
        //works for api 26 above
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getUberNotification("Arrived", body, contentIntent, defaultSound);

        notificationHelper.getManager().notify(1, builder.build());




    }

    private void OpenRatesActivity(String body) {

        Intent intent = new Intent(this, RateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        LocalBroadcastManager.getInstance(MyFirebaseMessaging.this)
                .sendBroadcast(new Intent(Common.DROPOFF_BROADCAST_STRING));

    }

    private void showArrivedNotification(String body) {
        //works for aaonly api 25 n below
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),
                0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)

                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(getApplicationContext().getResources().getColor(R.color.yellow))
                .setContentTitle("Arrived")
                .setContentInfo(body)
                .setContentIntent(contentIntent);
        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());


    }
}
