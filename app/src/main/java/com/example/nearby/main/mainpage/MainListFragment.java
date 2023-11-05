package com.example.nearby.main.mainpage;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nearby.databinding.FragmentMainListBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.location.Location;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainListFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FirebaseFirestore db;
    private List<Post> postList;
    private RecyclerView recyclerView;
    //포스트를 위한 어댑터
    private PostAdapter postAdapter;
    //기준 거리
    public float pivot_meter = 1000;
    //스와이프를 위한 객체
    private SwipeRefreshLayout swipeRefreshLayout;
    private FragmentMainListBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainListBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        // swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        // recyclerView = view.findViewById(R.id.recyclerView);
        postAdapter = new PostAdapter(postList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(postAdapter);
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();
                postAdapter.setPostList(postList);
                loadNearbyPosts();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        //권한 요청
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }

        //포스트 로드하기
        loadNearbyPosts();
        return rootView;
    }



    //근처 포스트를 로드하는 함수
    public void loadNearbyPosts() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(),"포스트 로드 실패! 위치권한을 허용해 주세요",Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                getPosts(location);
            }
        });
    }

    //현재 위치와 거리를 재서, 일정 위치 안에 있는 포스트만 postList에 추가
    public void getPosts(Location currentLocation) {
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {

                    String text = document.getString("text");
                    double latitude = document.getDouble("latitude");
                    double longitude = document.getDouble("longitude");
                    String date = document.getString("date"); // 추가: 날짜 데이터를 읽어옵니다.
                    String profilePicUrl = document.getString("profilePicUrl"); // 추가: 프로필 사진 URL 데이터를 읽어옵니다.

                    Location postLocation = new Location("");
                    postLocation.setLatitude(latitude);
                    postLocation.setLongitude(longitude);

                    //거리 재기
                    float distanceInMeters = currentLocation.distanceTo(postLocation);

                    //거리 비교해서 list에 넣기
                    if (distanceInMeters < pivot_meter) {
                        Post post = new Post(document.getId(), text, latitude, longitude, date, profilePicUrl);
                        postList.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }
        });
    }

    //위치 권한 요청 함수
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }
}
