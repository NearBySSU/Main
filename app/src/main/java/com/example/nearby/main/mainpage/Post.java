package com.example.nearby.main.mainpage;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.List;

public class Post implements ClusterItem {
    String postId;
    String text;
    double latitude;
    double longitude;
    String date;
    String userId;
    List<String> images;
    List<String> likes;
    List<String> tags;


    public Post(String postId, String text, double latitude, double longitude, String date, String userId, List<String> images,List<String> likes,List<String> tags) {
        this.postId = postId;
        this.text = text;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.userId = userId;
        this.images = images;
        this.likes = likes;
        this.tags = tags;
    }

    // getters and setters
    public String getPostId(){
        return postId;
    }

    public String getText(){
        return text;
    }

    // 추가: 날짜 getter
    public String getDate() {
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
    List<String> getTags(){ return tags;}


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    //cluster methods
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String getTitle() {
        if (text.length() > 20) {
            return text.substring(0, 20)+"...";
        } else {
            return text;
        }
    }


    @Override
    public String getSnippet() {
        return null;
    }
}
