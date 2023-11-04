package com.example.nearby;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Post implements ClusterItem {
    String id;
    String text;
    double latitude;
    double longitude;
    String date; // 추가: 날짜를 저장할 필드
    String userId;

    public Post(String id, String text, double latitude, double longitude, String date, String userId) {
        this.id = id;
        this.text = text;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date; // 추가: 날짜를 초기화
        this.userId = userId;
    }

    // getters and setters
    public String getId(){
        return id;
    }

    public String getText(){
        return text;
    }

    // 추가: 날짜 getter
    public String getDate() {
        return date;
    }

    public String getuserId() {
        return userId;
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
