package com.example.nearby.main.mainpage;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nearby.databinding.FragmentMainListBinding;
import com.example.nearby.main.maps.MyBottomSheetDialogFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.location.Location;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainListFragment extends Fragment {
    private long initTime = 0L;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FirebaseFirestore db;
    private List<Post> postList;
    private RecyclerView recyclerView; //포스트를 위한 어댑터
    private PostAdapter postAdapter; //기준 거리
    public float pivot_meter = 1000;
    private FragmentMainListBinding binding;

    private String selectedTag = null; // 선택된 태그


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainListBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(postAdapter);

        //위치 권한 요청
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }

        //스와이프 이벤트
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //포스트 리스트 초기화
                postList.clear();
                //다시 로드
                postAdapter.setPostList(postList);
                loadNearbyPosts();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

//        this.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                // 3초 이내에 백 버튼을 누르면 앱을 종료합니다.
//                if (System.currentTimeMillis() - initTime <= 3000) {
//                    Intent intent = new Intent(Intent.ACTION_MAIN);
//                    intent.addCategory(Intent.CATEGORY_HOME);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                } else {
//                    // 3초 이내에 백 버튼을 누르지 않았다면, 토스트 메시지를 보여주고 시간을 재설정합니다.
//                    Toast.makeText(getActivity(), "뒤로 가기 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
//                    initTime = System.currentTimeMillis();
//                }
//            }
//        });

        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetDialogFragment bottomSheetDialogFragment = new MyBottomSheetDialogFragment();
                bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
            }
        });

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
                // 현재 위치를 주소로 변환
                try {
                    Geocoder geocoder = new Geocoder(getActivity(), Locale.KOREAN);
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!addresses.isEmpty()) { // 추가: Address 리스트가 비어 있지 않을 때만 처리하도록 합니다.
                        String adminArea = addresses.get(0).getAdminArea(); // '서울특별시'와 같은 정보를 가져옵니다.
                        String locality = addresses.get(0).getSubLocality(); // '성북구'와 같은 정보를 가져옵니다.
                        binding.tvLocationTitle.setText(adminArea);
                        binding.tvLocationCity.setText(locality);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    //현재 위치와 거리를 재서, 일정 위치 안에 있는 포스트만 postList에 추가
    public void getPosts(Location currentLocation) {
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    List<String> tags = (List<String>) document.get("tags"); // 태그 정보를 읽어옵니다.


                    // 선택된 태그가 없거나, 게시물의 태그 중 선택된 태그가 있다면 게시물을 추가합니다.
                    if (selectedTag == null || (tags != null && tags.contains(selectedTag))) {

                        String text = document.getString("text");
                        double latitude = document.getDouble("latitude");
                        double longitude = document.getDouble("longitude");
                        String uid = document.getString("uid");
                        String date = document.getString("date"); // 추가: 날짜 데이터를 읽어옵니다.
                        List<String> imageUrls = (List<String>) document.get("imageUrls");
                        List<String> likeList = (List<String>) document.get("likes");
                        Location postLocation = new Location("");
                        postLocation.setLatitude(latitude);
                        postLocation.setLongitude(longitude);

                        //거리 재기
                        float distanceInMeters = currentLocation.distanceTo(postLocation);

                        //거리 비교해서 list에 넣기
                        if (distanceInMeters < pivot_meter) {
                            Post post = new Post(document.getId(), text, latitude, longitude, date, uid, imageUrls, likeList);
                            postList.add(post);
                        }
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

    public void setSelectedTag(String tag) {
        this.selectedTag = tag;
        if(getActivity() != null) {
            Toast.makeText(getActivity(), "태그 반영 됨!", Toast.LENGTH_SHORT).show();
            loadNearbyPosts(); // 태그가 변경되면 게시물을 다시 로드합니다.
        }
    }

}