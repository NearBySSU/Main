package com.example.nearby;

public class Post {
    String id;
    double latitude;
    double longitude;

    public Post(String id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // getters and setters
    String getId(){
        return id;
    }
}

