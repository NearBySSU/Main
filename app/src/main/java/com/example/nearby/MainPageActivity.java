package com.example.nearby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.example.nearby.databinding.ActivityMainPageBinding;
import com.example.nearby.mainpagefragment.FriendsFragment;
import com.example.nearby.mainpagefragment.MainListFragment;
import com.example.nearby.mainpagefragment.MapsFragment;
import com.example.nearby.mainpagefragment.ProfileFragment;
import com.google.android.material.navigation.NavigationBarView;

public class MainPageActivity extends AppCompatActivity {
    FriendsFragment friendsFragment;
    MainListFragment mainListFragment;
    MapsFragment mapsFragment;
    ProfileFragment profileFragment;
    ActivityMainPageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        friendsFragment = new FriendsFragment();
        mainListFragment = new MainListFragment();
        mapsFragment = new MapsFragment();
        profileFragment = new ProfileFragment();

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
}