package com.example.nearby.main;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nearby.R;
import com.example.nearby.databinding.ActivityMainPageBinding;
import com.example.nearby.main.mainpage.Post;
import com.example.nearby.main.upload.UploadContentsActivity;
import com.example.nearby.main.friends.FriendsListFragment;
import com.example.nearby.main.mainpage.MainListFragment;
import com.example.nearby.main.maps.MapsFragment;
import com.example.nearby.main.profile.ProfileFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainPageActivity extends AppCompatActivity implements PostLoader {
    FriendsListFragment friendsListFragment;
    public int selectedDistance;
    public int selectedDate;
    MainListFragment mainListFragment;
    MapsFragment mapsFragment;
    ProfileFragment profileFragment;
    private List<Post> postList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    public float pivot_meter = 5000;
    public boolean nullPostId = true;
    private long backKeyPressedTime = 0;
    private Toast toast;
    ActivityMainPageBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public MutableLiveData<List<Post>> livePostList = new MutableLiveData<>();
    public List<String> selectedTags = new ArrayList<>();
    public List<Post> originalPostList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        friendsListFragment = new FriendsListFragment();
        mainListFragment = new MainListFragment();
        mapsFragment = new MapsFragment();
        profileFragment = new ProfileFragment();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getSupportFragmentManager().beginTransaction().add(R.id.containers, new MainListFragment()).commit();
        NavigationBarView navigationBarView = findViewById(R.id.bottom_navigationView);


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("Fetching FCM registration token failed");
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "Channel for My App";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.MainListNav) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, mainListFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.MapNav) {
                    if (nullPostId == true) {
                        mapsFragment.initializePostId();  // postId를 초기화하는 메소드를 호출
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, mapsFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.Upload) {
                    Intent intent = new Intent(MainPageActivity.this, UploadContentsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.FriendNav) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, friendsListFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.ProfileNav) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, profileFragment).commit();
                    return true;
                }
                return false;
            }
        });

        //포스트 로드 시작
        loadNearbyPosts();
    }

    @SuppressLint("MissingPermission")
    private void loadNearbyPosts() {

        //현재 위치 가져오기
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                getPosts(location);
            }
        });
    }

    public void getPosts(Location currentLocation) {
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {

                    double latitude = document.getDouble("latitude");
                    double longitude = document.getDouble("longitude");
                    Location postLocation = new Location("");
                    postLocation.setLatitude(latitude);
                    postLocation.setLongitude(longitude);

                    //거리 재기
                    float distanceInMeters = currentLocation.distanceTo(postLocation);
                    int distanceCategory;

                    if (distanceInMeters <= 1000) {
                        distanceCategory = 1; // 1km 이하면 "가까이"로 분류
                    } else if (distanceInMeters <= 3000) {
                        distanceCategory = 3; // 3km 이하면 "적당히"로 분류
                    } else if (distanceInMeters <= 5000) {
                        distanceCategory = 5; // 5km 이하면 "멀리"로 분류
                    } else {
                        continue; //5km 이상이면 로드하지 않음
                    }
                    //나머지 정보들 로드
                    String uid = document.getString("uid");
                    Timestamp date = document.getTimestamp("date");
                    String bigLocationName = document.getString("bigLocationName");
                    String smallLocationName = document.getString("smallLocationName");
                    List<String> imageUrls = (List<String>) document.get("imageUrls");
                    List<String> likeList = (List<String>) document.get("likes");
                    List<String> tags = (List<String>) document.get("tags");
                    String text = document.getString("text");

                    //날짜 계산 (현재 시간과 게시물의 시간 차이를 월로 변환)
                    long diffInMilli = System.currentTimeMillis() - date.toDate().getTime();
                    long diffInMonth = TimeUnit.MILLISECONDS.toDays(diffInMilli) / 30;
                    Log.e("loadpost", "getPosts: " + diffInMonth);

                    Post post = new Post(document.getId(), text, bigLocationName, smallLocationName, latitude, longitude, date, uid, imageUrls, likeList, tags, distanceCategory, (int) diffInMonth);
                    postList.add(post);
                    originalPostList = postList;
                    livePostList.setValue(postList);
                }
            }
        });
    }

    //포스트 리스트를 리턴하는 메서드
    @Override
    public List<Post> getPostList() {
        return postList;
    }

    //포스트를 다시 로드하는 메서드
    @Override
    public void reloadPostList() {
        postList.clear();
        loadNearbyPosts();
    }

    //뒤로가기 눌렀을때의 이벤트
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
        }
    }
}

