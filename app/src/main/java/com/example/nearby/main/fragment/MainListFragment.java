package com.example.nearby.main.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearby.Post;
import com.example.nearby.PostAdapter;
import com.example.nearby.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.location.Location;
import java.util.ArrayList;
import java.util.List;

public class MainListFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private List<Post> postList;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    public float pivot_meter = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_list, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView);
        postAdapter = new PostAdapter(postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(postAdapter);

        loadNearbyPosts();

        return view;
    }


    //근처 포스트를 로드하는 함수
    private void loadNearbyPosts() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                getPosts(location);
            }
        });
    }

    //현재 위치와 거리를 재서, 일정 위치 안에 있는 포스트만 postList에 추가
    private void getPosts(Location currentLocation) {
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {

                    String text = document.getString("text");
                    double latitude = document.getDouble("latitude");
                    double longitude = document.getDouble("longitude");

                    Location postLocation = new Location("");
                    postLocation.setLatitude(latitude);
                    postLocation.setLongitude(longitude);

                    float distanceInMeters = currentLocation.distanceTo(postLocation);

                    if (distanceInMeters < pivot_meter) {
                        Post post = new Post(document.getId(),text, latitude, longitude);
                        postList.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }
        });
    }
}
