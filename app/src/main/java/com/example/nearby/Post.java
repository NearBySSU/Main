package com.example.nearby;

public class Post {
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
}

