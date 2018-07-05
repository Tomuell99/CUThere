package com.example.cuthere;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;

public class GeofenceNotification {
    public static final int NOTIFICATION_ID = 20;

    protected Context context;

    protected NotificationManager notificationManager;
    protected Notification notification;

    public GeofenceNotification(Context context){
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    protected void buildNotification(SimpleGeofence simpleGeofence, int transitionType){
        String notificationText = "";
        Object[] notificationTextParams = new Object[]{simpleGeofence};

        switch (transitionType){
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                notificationText = String.format("dwelling", notificationTextParams);
                break;
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                notificationText = String.format("entering", notificationTextParams);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                notificationText = String.format("exiting", notificationTextParams);
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("my_channel_01", "channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(simpleGeofence.getId())
                .setContentText(simpleGeofence.getLatitude() + " || " + simpleGeofence.getLongitude()
                        + " || " + notificationText)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setChannelId("my_channel_id");

        notification = notificationBuilder.build();
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
    }

    public void displayNotification(SimpleGeofence simpleGeofence, int transitionType){
        Log.d("Receiver1", String.valueOf(transitionType));
        buildNotification(simpleGeofence, transitionType);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
