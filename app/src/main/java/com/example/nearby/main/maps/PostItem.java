package com.example.nearby.main.maps;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostItem {

    private String title;
    private String date; // 날짜를 저장할 필드를 추가
    private String profilePicUrl; // 프로필 사진 URL을 저장할 필드를 추가
    private String postId;

    // 생성자
    public PostItem(String title, String date, String profilePicUrl, String postId) {
        this.title = title;
        this.date = date;
        this.profilePicUrl = profilePicUrl;
        this.postId = postId;
    }

    // Getter and Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getPostId() { return postId; }

    public void setPostId(String postId) { this.postId = postId; }
}

