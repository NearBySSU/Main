package com.example.nearby.main;

import com.example.nearby.main.mainpage.Post;

import java.util.List;

public interface PostLoader {
    List<Post> getPostList();

    void reloadPostList();
}
