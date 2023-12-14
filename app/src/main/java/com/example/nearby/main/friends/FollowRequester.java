package com.example.nearby.main.friends;

public class FollowRequester {
    String uid;
    String profilePicUrl;

    public FollowRequester(String uid, String profilePicUrl, String nickname, String email) {
        this.uid = uid;
        this.profilePicUrl = profilePicUrl;
        this.nickname = nickname;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    String nickname;
    String email;

}
