package com.example.cuthere;

import com.google.gson.annotations.SerializedName;

public class SerilizedLocation {
    @SerializedName("name")
    private String name;
    @SerializedName("lat")
    private String lat;
    @SerializedName("lon")
    private String lon;

    public SerilizedLocation(String name, String lat, String lon){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName(){
        return name;
    }
    public String getLat(){
        return lat;
    }
    public String getLon(){
        return lon;
    }
}
