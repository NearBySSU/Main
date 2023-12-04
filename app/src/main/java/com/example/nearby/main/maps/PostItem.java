package com.example.nearby.main.maps;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.List;

public class PostItem {

    private String title;
    private Timestamp date; // 날짜를 저장할 필드를 추가
    private String profilePicUrl; // 프로필 사진 URL을 저장할 필드를 추가
    private String postId;
    private String uid;

    // 생성자
    public PostItem(String title, Timestamp date, String profilePicUrl, String postId, String uid) {
        this.title = title;
        this.date = date;
        this.profilePicUrl = profilePicUrl;
        this.postId = postId;
        this.uid = uid;
    }

    // Getter and Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }
}

