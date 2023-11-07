package com.example.nearby.main.maps;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.nearby.databinding.FragmentMapsBinding;
import com.example.nearby.main.mainpage.Post;
import com.example.nearby.main.mainpage.PostAdapter;
import com.example.nearby.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;


import java.util.ArrayList;
import java.util.List;


public class MapsFragment extends Fragment {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private FirebaseFirestore db;

    private List<Post> postList;
    //포스트를 위한 어댑터
    private PostAdapter postAdapter;
    //기준 거리
    public float pivot_meter = 1000;
    private ClusterManager<Post> mClusterManager;
    private PostItemAdapter postItemAdapter;
    private Button filterButton;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mClusterManager = new ClusterManager<Post>(getActivity(), mMap);
            mClusterManager.setRenderer(new CustomRenderer<>(getActivity(), mMap, mClusterManager));


            //위치권한 확인
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
            } else {
                mMap.setMyLocationEnabled(true);
                moveToLastKnownLocation();
            }

            // 클러스터링을 위한 맵의 클릭 리스너 설정
            mMap.setOnCameraIdleListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);

            //마커 하나를 클릭했을 때 이벤트
            mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Post>() {
                @Override
                public boolean onClusterItemClick(Post post) {
                    postItemAdapter.clearItems();  // 아이템을 초기화합니다.
                    getProfilePicUrl(post.getUserId(), profilePicUrl -> {
                        postItemAdapter.addItem(new PostItem(post.getTitle(), post.getDate(), profilePicUrl));
                        postItemAdapter.notifyDataSetChanged();
                    });
                    return false;
                }
            });


            //클러스터 덩어리를 클릭했을때 이벤트
            mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Post>() {
                @Override
                public boolean onClusterClick(Cluster<Post> cluster) {
                    postItemAdapter.clearItems();  // 아이템을 초기화합니다.
                    for (Post post : cluster.getItems()) {
                        getProfilePicUrl(post.getUserId(), profilePicUrl -> {
                            postItemAdapter.addItem(new PostItem(post.getTitle(), post.getDate(), profilePicUrl));

                        });
                    }
                    postItemAdapter.notifyDataSetChanged();
                    return false;
                }
            });


            // 지도의 qls 마커 영역을 클릭했을 때 이벤트
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    postItemAdapter.clearItems();  // 아이템을 초기화합니다.
                    postItemAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(),"현위치 가져오기 실패! 위치권한을 허용해 주세요",Toast.LENGTH_SHORT).show();
            requestLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        RecyclerView recyclerViewBottom = view.findViewById(R.id.bottom_sheet);
        postAdapter = new PostAdapter(postList);
        postItemAdapter = new PostItemAdapter();
        recyclerViewBottom.setAdapter(postItemAdapter);
        filterButton = view.findViewById(R.id.filterButton);
        MyBottomSheetDialogFragment bottomSheetDialogFragment = MyBottomSheetDialogFragment.newInstance();

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
            }
        });


        // SnapHelper를 생성하고 recyclerViewBottom에 붙입니다.
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewBottom);



        loadNearbyPosts();

    }



    //위치권한 요청 함수
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }


    private void moveToLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            }
        });
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

    // 현재 위치와 거리를 재서, 일정 위치 안에 있는 포스트만 postList에 추가
    public void getPosts(Location currentLocation) {
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Post> allPosts = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String text = document.getString("text");
                    double latitude = document.getDouble("latitude");
                    double longitude = document.getDouble("longitude");
                    String date = document.getString("date");
                    String uid = document.getString("uid");
                    List<String> imagesList = (List<String>) document.get("images");

                    Post post = new Post(document.getId(), text, latitude, longitude, date, uid, imagesList);
                    allPosts.add(post);
                }

                for (Post post : allPosts) {
                    Location postLocation = new Location("");
                    postLocation.setLatitude(post.getLatitude());
                    postLocation.setLongitude(post.getLongitude());

                    float distanceInMeters = currentLocation.distanceTo(postLocation);

                    if (distanceInMeters < pivot_meter) {
                        getProfilePicUrl(post.getUserId(), profilePicUrl -> {
                            mClusterManager.addItem(post);
                            postItemAdapter.addItem(new PostItem(post.getTitle(), post.getDate(), profilePicUrl));
                        });
                        postList.add(post);
                    }
                }

                postAdapter.notifyDataSetChanged();
            }
        });
    }



    //user id로 부터 프로필 사진 얻기
    @SuppressLint("RestrictedApi")
    private void getProfilePicUrl(String userId, final OnProfilePicUrlReceivedListener listener) {
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String profilePicUrl = document.getString("profilePicUrl");
                    listener.onProfilePicUrlReceived(profilePicUrl);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    interface OnProfilePicUrlReceivedListener {
        void onProfilePicUrlReceived(String profilePicUrl);
    }

}
