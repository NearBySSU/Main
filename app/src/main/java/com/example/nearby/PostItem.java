package com.example.nearby;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostItem {
    private String title;

    // 생성자
    public PostItem(String title) {
        this.title = title;
    }

    // getter
    public String getTitle() {
        return title;
    }

    // setter
    public void setTitle(String title) {
        this.title = title;
    }
}

