package com.example.cuthere;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.arsy.maps_library.MapRadar;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapsActivity extends /*AppCompatActivity*/FragmentActivity implements OnMapReadyCallback {

    static public boolean geofencesAlreadyRegistered = false;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final long MIN_TIME = 1000;
    private static final float MIN_DISTANCE = 10;

    MapRadar mapRadar;
    protected Circle currentMarker;

    SeekBar zoomBar;
    int zoom = 15;
    int km = 1200;

    String locations;
    ArrayList<cstLocation> locationlist = new ArrayList<>();

    ProgressDialog dialog;

    GoogleApiClient mClient;
    private GeofencingClient mGeofencingClient;
    private List<Geofence> mgeofenceList = new ArrayList<>();
    private PendingIntent mGeofencePendingIntent;


    public class cstLocation{
        String lName;
        double lLatitude;
        double lLongitude;

        public cstLocation(String name, double latitude, double longitude){
            lName = name;
            lLatitude = latitude;
            lLongitude = longitude;
        }

    }

    static public Context mContext;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mContext = getApplicationContext();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        startGeolocationService(getApplicationContext());

        zoomBar = (SeekBar) findViewById(R.id.seekBar);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        /*try {
            JSONObject allLocations = new JSONObject(readFromFile("koor1.json", this));
            JSONArray jsonArray = allLocations.getJSONArray("1");
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject location = jsonArray.getJSONObject(i);
                locationlist.add(new cstLocation(location.getString("name"), location.getDouble("lat"), location.getDouble("long")));

                mgeofenceList.add(geofence(location.getDouble("lat"), location.getDouble("long")));
                Log.i("geofence", String.valueOf(mgeofenceList));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        /*mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });*/
    }

    /*public Geofence geofence(double latitude, double longitude){
        String id = UUID.randomUUID().toString();
        return new Geofence.Builder()
                .setRequestId(id)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(latitude, longitude, 50)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mgeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent(){
        if (mGeofencePendingIntent != null){
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }*/

    private void startGeolocationService(Context context){
        Intent geoService = new Intent(context, GeofenceTransitionsIntentService.class);
        PendingIntent piGeoService = PendingIntent.getService(context, 0, geoService, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(piGeoService);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 2 * 60 * 1000, piGeoService);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);



        for (int i = 0; i < locationlist.size(); i++){

        }

        //addMyMarker(mMap);
        displayGeofences();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });

       mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
           @Override
           public void onInfoWindowClick(Marker marker) {
               Toast.makeText(MapsActivity.this, marker.getTitle() + " clicked", Toast.LENGTH_SHORT).show();

           }
       });

        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                zoom = i + 10;
                mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
                km = (int) ((40000 / Math.pow(2, i)) );
                mapRadar.withDistance(km);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mapRadar.stopRadarAnimation();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mapRadar.startRadarAnimation();
            }
        });

    }

    /*@Override
    public void onLocationChanged(Location location){
        Toast.makeText(this, location.getLatitude() + " || " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        Log.v("onLocationChanged", "lat = " + location.getLatitude() + ", lon = " + location.getLongitude());

        if (dialog.isShowing()){
            dialog.cancel();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(cameraUpdate);

        if (currentMarker != null) {
            currentMarker.remove();
        }
        CircleOptions currentLoc = new CircleOptions();
        currentLoc.center(latLng).radius(5).strokeColor(Color.CYAN);
        currentMarker = mMap.addCircle(currentLoc);

        mapRadar.withLatLng(latLng);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }*/

    @Override
    public void onStop() {
        super.onStop();

        if (dialog.isShowing()){
            dialog.cancel();
        }
        //locationManager.removeUpdates(this);
    }

    public void addMyMarker(GoogleMap map){
        for (int i = 0; i < locationlist.size(); i++){
            cstLocation aktMarker = locationlist.get(i);
            LatLng latLng = new LatLng(aktMarker.lLatitude, aktMarker.lLongitude);
            map.addMarker(new MarkerOptions().position(latLng).title(aktMarker.lName).snippet("mehr..."));
        }
    }

    protected void displayGeofences(){
        HashMap<String, SimpleGeofence> geofences = SimpleGeofenceStore.getInstance().getSimpleGeofences();

        for (Map.Entry<String, SimpleGeofence>item : geofences.entrySet()){
            SimpleGeofence sg = item.getValue();

            MarkerOptions geofenceMarker = new MarkerOptions()
                    .position(new LatLng(sg.getLatitude(), sg.getLongitude()))
                    .title(sg.getId())
                    .snippet("mehr...");
            mMap.addMarker(geofenceMarker);
        }
    }

    /*public String readFromFile(String fileName, Context context) {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets()
                    .open(fileName, Context.MODE_WORLD_READABLE);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return returnString.toString();
    }*/

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null){
                int resultCode = bundle.getInt("done");
                if (resultCode == 1){
                    Double latitude = bundle.getDouble("latitude");
                    Double longitude = bundle.getDouble("longitude");

                    updateMarker(latitude, longitude);
                }
            }
        }
    };

    @Override
    public void onPause(){
        super.onPause();
        this.unregisterReceiver(receiver);
    }

    @Override
    public void onResume(){
        super.onResume();
        this.registerReceiver(receiver, new IntentFilter("com.example.cuthere.geolocation.service"));
    }

    protected void createMarker(Double latitude, Double longitude){
        LatLng latLng = new LatLng(latitude, longitude);
        currentMarker = mMap.addCircle(new CircleOptions().center(latLng).radius(5).strokeColor(Color.CYAN));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mapRadar = new MapRadar(mMap, new LatLng(latitude, longitude), this);
        mapRadar.withDistance(km);
        mapRadar.withOuterCircleStrokeColor(0x13C200);
        mapRadar.withRadarColors(0x0013C200, 0xff13C200);
        mapRadar.startRadarAnimation();

        if (dialog.isShowing()){
            dialog.cancel();
        }
    }

    protected void updateMarker(Double latitude, Double longitude){
        if (currentMarker == null){
            createMarker(latitude, longitude);
        }

        LatLng latLng = new LatLng(latitude, longitude);
        currentMarker.setCenter(latLng);
        mapRadar.withLatLng(latLng);
    }

}
