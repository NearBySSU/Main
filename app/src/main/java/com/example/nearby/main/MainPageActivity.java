package com.example.nearby.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nearby.R;
import com.example.nearby.main.upload.UploadContentsActivity;
import com.example.nearby.databinding.ActivityMainPageBinding;
import com.example.nearby.main.friends.FriendsFragment;
import com.example.nearby.main.mainpage.MainListFragment;
import com.example.nearby.main.maps.MapsFragment;
import com.example.nearby.main.profile.ProfileFragment;
import com.google.android.material.navigation.NavigationBarView;

public class MainPageActivity extends AppCompatActivity implements MapsFragment.OnDataPass{
    FriendsFragment friendsFragment;
    MainListFragment mainListFragment;
    MapsFragment mapsFragment;
    ProfileFragment profileFragment;
    private String selectedTag;
    ActivityMainPageBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        friendsFragment = new FriendsFragment();
        mainListFragment = new MainListFragment();
        mapsFragment = new MapsFragment();
        profileFragment = new ProfileFragment();

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
    }

    // 선택된 태그 정보를 업데이트하는 메소드
    public void updateSelectedTag(String tag) {
        this.selectedTag = tag;
        mainListFragment.setSelectedTag(tag); // 태그 업데이트
    }

    //데이터 전송 인터페이스를 구현
    @Override
    public void onDataPass(String data) {
        // 여기에 data를 처리하는 코드를 작성하세요.
    }


}