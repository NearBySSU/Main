package com.example.nearby.main.profile;

public class ProfileItem {
    private String date;
    private String imgUrl;
    private String postId;

    public ProfileItem(String date, String imgUrl, String postId) {
        this.date = date;
        this.imgUrl = imgUrl;
        this.postId = postId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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
