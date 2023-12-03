package com.example.nearby.main.profile;

import com.google.firebase.Timestamp;

public class ProfileItem {
    private Timestamp date;
    private String imgUrl;
    private String postId;

    public ProfileItem(Timestamp date, String imgUrl, String postId) {
        this.date = date;
        this.imgUrl = imgUrl;
        this.postId = postId;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
