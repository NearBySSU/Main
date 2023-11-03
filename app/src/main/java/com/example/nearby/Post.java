package com.example.nearby;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Post implements ClusterItem {
    String id;
    String text;
    double latitude;
    double longitude;

    public Post(String id,String text, double latitude, double longitude) {
        this.id = id;
        this.text = text;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // getters and setters
    String getId(){
        return id;
    }

    String getText(){
        return text;
    }

    //cluster methods
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String getTitle() {
        return text;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}

