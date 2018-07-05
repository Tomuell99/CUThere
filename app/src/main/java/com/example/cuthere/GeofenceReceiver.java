package com.example.cuthere;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class GeofenceReceiver extends IntentService {
    public GeofenceReceiver() {
        super("GeofenceReceiver");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geoEvent = GeofencingEvent.fromIntent(intent);
        if (geoEvent.hasError()){
            Log.d("Receiver1", "Error onHandleIntent");
        }else{
            Log.d("Receiver1", "Receiver: Transition -> " + geoEvent.getGeofenceTransition());

            int transitionType = geoEvent.getGeofenceTransition();

            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    transitionType == Geofence.GEOFENCE_TRANSITION_DWELL ||
                    transitionType == Geofence.GEOFENCE_TRANSITION_EXIT){
                List<Geofence> triggerList = geoEvent.getTriggeringGeofences();

                for (Geofence geofence : triggerList){
                    SimpleGeofence sg = SimpleGeofenceStore.getInstance().getSimpleGeofences()
                            .get(geofence.getRequestId());

                    String transitionName = "";
                    switch(transitionType){
                        case Geofence.GEOFENCE_TRANSITION_DWELL:
                            transitionName = "dwell";
                            break;
                        case Geofence.GEOFENCE_TRANSITION_ENTER:
                            transitionName = "enter";
                            break;
                        case Geofence.GEOFENCE_TRANSITION_EXIT:
                            transitionName = "exit";
                            break;
                    }
                    String date1 = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date()).toString();

                    EventDataSource eds = new EventDataSource(getApplicationContext());
                    eds.create(transitionName, date1, geofence.getRequestId());
                    eds.close();

                    GeofenceNotification geofenceNotification = new GeofenceNotification(this);
                    geofenceNotification.displayNotification(sg, transitionType);
                }
            }
        }
    }
}
