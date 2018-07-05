package com.example.cuthere;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeofenceTransitionsIntentService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    private PendingIntent mPendingIntent;

    protected void registerGeofences() {
        if (MapsActivity.geofencesAlreadyRegistered) {
            return;
        }

        Log.d("geolocation", "Registering Geofences");

        //android.os.Debug.waitForDebugger();

        HashMap<String, SimpleGeofence> geofences = SimpleGeofenceStore.getInstance().getSimpleGeofences();

        GeofencingRequest.Builder geofencingRequestBuilder = new GeofencingRequest.Builder();
        for (Map.Entry<String, SimpleGeofence> item : geofences.entrySet()) {
            SimpleGeofence sg = item.getValue();

            Log.d("SimpleGeofence", sg.getId());

            geofencingRequestBuilder.addGeofence(sg.toGeofence());

            Log.d("SimpleGeofence", geofencingRequestBuilder.toString());
        }

        //GeofencingRequest geofencingRequest = geofencingRequestBuilder.build();

        mPendingIntent = requestPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequestBuilder.build(), mPendingIntent)
                .setResultCallback(this);

        MapsActivity.geofencesAlreadyRegistered = true;
    }

    private PendingIntent requestPendingIntent() {
        if (null != mPendingIntent) {
            return mPendingIntent;
        } else {
            Log.d("PendingIntent", "inService");
            Intent intent = new Intent(this, GeofenceReceiver.class);
            return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        buildGoogleApiClient();

        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void broadcastLocationFound(Location location) {
        Intent intent = new Intent("com.example.cuthere.geolocation.service");
        intent.putExtra("latitude", location.getLatitude());
        intent.putExtra("longitude", location.getLongitude());
        intent.putExtra("done", 1);

        sendBroadcast(intent);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("geolocation", "Connected to GoogleApiClient");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("geolocation", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("geolocation", "Connection failed: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("geolocation", "new location: " + location.getLatitude() +
        " || " + location.getLongitude() + " || " + location.getAccuracy());
        broadcastLocationFound(location);

        if (!MapsActivity.geofencesAlreadyRegistered){
            registerGeofences();
        }

        /*if (location.getAccuracy() <= 50){
            Log.d("geolocation", "Stopping geolocation service");
            stopSelf();
        }*/
    }

    protected synchronized void buildGoogleApiClient(){
        Log.i("geolocation", "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        createLocationRequest();
    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void onResult(Status status){
        if (status.isSuccess()){
            Toast.makeText(this, "onResult", Toast.LENGTH_SHORT).show();
        }else{
            MapsActivity.geofencesAlreadyRegistered = false;
            String errorMessage = getErrorString(this, status.getStatusCode());
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public static String getErrorString(Context context, int errorCode){
        switch (errorCode){
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "too many geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "too many pending intents";
            default:
                return "unknown error";
        }
    }
}
