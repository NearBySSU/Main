package com.example.nearby.main;

import static com.example.nearby.Utils.checkLocationPermission;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nearby.R;
import com.example.nearby.main.mainpage.Post;
import com.example.nearby.main.upload.UploadContentsActivity;
import com.example.nearby.databinding.ActivityMainPageBinding;
import com.example.nearby.main.friends.FriendsFragment;
import com.example.nearby.main.mainpage.MainListFragment;
import com.example.nearby.main.maps.MapsFragment;
import com.example.nearby.main.profile.ProfileFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;



public class MainPageActivity extends AppCompatActivity implements PostLoader{
    FriendsFragment friendsFragment;
    MainListFragment mainListFragment;
    MapsFragment mapsFragment;
    ProfileFragment profileFragment;
    private List<Post> postList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    public float pivot_meter = 1000;

    ActivityMainPageBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public MutableLiveData<List<Post>> livePostList = new MutableLiveData<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        friendsFragment = new FriendsFragment();
        mainListFragment = new MainListFragment();
        mapsFragment = new MapsFragment();
        profileFragment = new ProfileFragment();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        mainListFragment = new MainListFragment(); // MainListFragment 인스턴스 생성

        // 생성한 인스턴스를 화면에 추가
        getSupportFragmentManager().beginTransaction().add(R.id.containers, mainListFragment).commit();

        getSupportFragmentManager().beginTransaction().add(R.id.containers, new MainListFragment()).commit();
        NavigationBarView navigationBarView = findViewById(R.id.bottom_navigationView);

        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.MainListNav) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, mainListFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.MapNav) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, mapsFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.Upload) {
                    Intent intent = new Intent(MainPageActivity.this, UploadContentsActivity.class);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.FriendNav) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, friendsFragment).commit();
                    return true;
                }
                else if (item.getItemId() == R.id.ProfileNav) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.containers, profileFragment).commit();
                    return true;
                }
                return false;
            }
        });

        loadNearbyPosts();
    }

    private void loadNearbyPosts() {
        //위치 권한 확인
        if(!checkLocationPermission(this,LOCATION_PERMISSION_REQUEST_CODE)){
            return;
        }
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

                    //거리 비교해서 list에 넣기
                    if (distanceInMeters < pivot_meter) {

                        //나머지 정보들 로드
                        String uid = document.getString("uid");
                        String date = document.getString("date"); // 추가: 날짜 데이터를 읽어옵니다.
                        List<String> imageUrls = (List<String>) document.get("imageUrls");
                        List<String> likeList = (List<String>) document.get("likes");
                        String text = document.getString("text");

                        Post post = new Post(document.getId(), text, latitude, longitude, date, uid, imageUrls, likeList);
                        postList.add(post);
                        livePostList.setValue(postList);
                    }
                }
            }
        });
    }

    @Override
    public List<Post> getPostList() {
        return postList;
    }

    @Override
    public void reloadPostList() {
        postList.clear();
        loadNearbyPosts();
    }
}

