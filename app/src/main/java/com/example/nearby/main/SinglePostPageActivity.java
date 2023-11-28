package com.example.nearby.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nearby.databinding.ActivityLogInBinding;
import com.example.nearby.databinding.ActivitySinglePostPageBinding;
import com.example.nearby.main.mainpage.Post;
import com.example.nearby.main.mainpage.PostAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SinglePostPageActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ActivitySinglePostPageBinding binding;
//    private PostLoader postLoader;
    private List<Post> postList = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySinglePostPageBinding.inflate(getLayoutInflater());
        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");

        Log.d("singlepage", postId);
//        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        // recyclerView 등록
//        postAdapter = new PostAdapter(postList);
//        binding.recyclerView.setAdapter(postAdapter);
//
//        // 스와이프 이벤트
//        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                postList.clear();
//                postAdapter.setPostList(postList);
//                getPost(postId);
//                binding.swipeRefreshLayout.setRefreshing(false);
//            }
//        });
//
//        // post list 로드 하기
//        postList.clear();
//        postAdapter.notifyDataSetChanged();
//
//        getPost(postId);
//
//    }
//
//    private void getPost(String postId) {
//        db.collection("posts").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//
//                        // 데이터 로드
//                        double latitude = document.getDouble("latitude");
//                        double longitude = document.getDouble("longitude");
//                        String uid = document.getString("uid");
//                        String date = document.getString("date");
//                        String bigLocationName = document.getString("bigLocationName");
//                        String smallLocationName = document.getString("smallLocationName");
//                        List<String> imageUrls = (List<String>) document.get("imageUrls");
//                        List<String> likeList = (List<String>) document.get("likes");
//                        List<String> tags = (List<String>) document.get("tags");
//                        String text = document.getString("text");
//
//                        // Post 객체 생성
//                        Post post = new Post(document.getId(), text, bigLocationName, smallLocationName, latitude, longitude, date, uid, imageUrls, likeList,tags);
//                        postList.add(post);
//                        postAdapter.notifyDataSetChanged();  // 데이터가 추가될 때마다 UI 갱신
//
//                        Log.d("SinglePost", "DocumentSnapshot data: " + document.getData());
//
//                    } else {
//                        Log.d("SinglePost", "No such document");
//                    }
//                } else {
//                    Log.d("SinglePost", "get failed with ", task.getException());
//                }
//            }
//        });
    }
}
