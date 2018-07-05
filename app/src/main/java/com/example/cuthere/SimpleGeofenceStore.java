package com.example.cuthere;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class SimpleGeofenceStore {
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;
    protected HashMap<String, SimpleGeofence> geofences = new HashMap<String, SimpleGeofence>();
    private Context context;
    private static SimpleGeofenceStore instance = new SimpleGeofenceStore();

    private ReadJsonFromFile readJson = new ReadJsonFromFile();

    public static SimpleGeofenceStore getInstance(){
        return instance;
    }

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

    private SimpleGeofenceStore(){
        try {
            JSONObject allLocations = new JSONObject(readJson.read("koor1.json", MapsActivity.mContext));
            JSONArray jsonArray = allLocations.getJSONArray("1");
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject location = jsonArray.getJSONObject(i);
                String name = location.getString("name");
                double latitude = location.getDouble("lat");
                double longitude = location.getDouble("long");
                geofences.put(name, new SimpleGeofence(name, latitude, longitude, 50,
                        GEOFENCE_EXPIRATION_IN_MILLISECONDS, Geofence.GEOFENCE_TRANSITION_ENTER
                                                                    | Geofence.GEOFENCE_TRANSITION_DWELL
                                                                    | Geofence.GEOFENCE_TRANSITION_EXIT));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("JSONException", e.toString());
        }
        /*geofences.put("Hbf", new SimpleGeofence("Hbf", 49.801476, 9.935772,
                100, GEOFENCE_EXPIRATION_IN_MILLISECONDS, Geofence.GEOFENCE_TRANSITION_ENTER
                | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT));*/
    }

    public HashMap<String, SimpleGeofence> getSimpleGeofences() {
        return this.geofences;
    }

    public String readLocFromFile(String fileName, Context context) {
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
    }

    public ArrayList<cstLocation> locList(Context context){
        ArrayList<cstLocation> locationlist = new ArrayList<>();
        try {
            JSONObject allLocations = new JSONObject(readLocFromFile("koor1.json", context));
            JSONArray jsonArray = allLocations.getJSONArray("1");
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject location = jsonArray.getJSONObject(i);
                locationlist.add(new cstLocation(location.getString("name"), location.getDouble("lat"), location.getDouble("long")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return locationlist;
    }
}
