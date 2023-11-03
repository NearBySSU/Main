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
    public String getId(){
        return id;
    }

    public String getText(){
        return text;
    }

    //cluster methods
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String getTitle() {
        if (text.length() > 20) {
            return text.substring(0, 20);
        } else {
            return text;
        }
    }

    @Override
    public String getSnippet() {
        return null;
    }
}

