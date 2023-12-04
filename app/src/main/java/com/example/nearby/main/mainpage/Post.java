package com.example.nearby.main.mainpage;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.maps.android.clustering.ClusterItem;

import java.util.List;

public class Post implements ClusterItem {
    String postId;
    String text;
    double latitude;
    double longitude;
    Timestamp date;
    String userId;
    String bigLocationName;
    String smallLocationName;

    List<String> images;
    List<String> likes;
    List<String> tags;
    private float distance;
    private int monthsAgo;


    public Post(String postId, String text, String bigLocationName, String smallLocationName, double latitude, double longitude, Timestamp date, String userId, List<String> images, List<String> likes, List<String> tags,float distance, int monthsAgo) {
        this.postId = postId;
        this.text = text;
        this.bigLocationName = bigLocationName;
        this.smallLocationName = smallLocationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.userId = userId;
        this.images = images;
        this.likes = likes;
        this.tags = tags;
        this.distance = distance;
        this.monthsAgo = monthsAgo;
    }

    // getters and setters
    public String getPostId() {
        return postId;
    }

    public String getText() {
        return text;
    }

    public String getBigLocationName() {
        return bigLocationName;
    }

    public String getSmallLocationName() {
        return smallLocationName;
    }


    // 추가: 날짜 getter
    public Timestamp getDate() {
        return date;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getImages() {
        return images;
    }

    public List<String> getLikes() {
        return likes;
    }

    public List<String> getTags() {
        return tags;
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    // Getter and Setter
    public float getDistance() {
        return distance;
    }

    public int getMonthsAgo() {
        return monthsAgo;
    }

    //cluster methods
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String getTitle() {
        if (text.length() > 30) {
            return text.substring(0, 30) + "...";
        } else {
            return text;
        }
    }


    @Override
    public String getSnippet() {
        return null;
    }
}
