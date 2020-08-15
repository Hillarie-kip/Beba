package com.techkip.bebarider.Helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.techkip.bebarider.R;


/**
 * Created by hillarie on 29/03/2018.
 */

public class NotificationHelper extends ContextWrapper {
    private static final String CHANNEL_ID = "com.techkip.bebarider.Rider";
    private static final String CHANNEL_Name = "Rider";
    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannels();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {

        NotificationChannel mychannel = new NotificationChannel(CHANNEL_ID, CHANNEL_Name, NotificationManager.IMPORTANCE_DEFAULT);
        mychannel.enableLights(true);
        mychannel.enableVibration(true);
        mychannel.setLightColor(Color.GREEN);
        mychannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(mychannel);

    }
    public NotificationManager getManager(){
        if (manager == null)
            manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getUberNotification(String title, String content, PendingIntent contentIntent, Uri soundUri){

        return  new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentText(content)
               // .setContentTitle(title)
                .setSound(soundUri)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(getApplicationContext().getResources().getColor(R.color.colorAccent));
    }
}
