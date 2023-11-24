package com.example.nearby.main.maps;

import static androidx.fragment.app.FragmentManager.TAG;

import static com.example.nearby.Utils.checkLocationPermission;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nearby.databinding.FragmentMapsBinding;
import com.example.nearby.main.MainPageActivity;
import com.example.nearby.main.PostLoader;
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
    private ClusterManager<Post> mClusterManager;
    private PostItemAdapter postItemAdapter;
    private ImageButton btn_filter;
    private static MapsFragment instance;
    private String postId;
    private PostLoader postLoader;


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

        postList = ((MainPageActivity) getActivity()).getPostList();    // MainPageActivity의 postList를 가져옴
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        db = FirebaseFirestore.getInstance();
        RecyclerView post_item_recyclerView = view.findViewById(R.id.post_item_recyclerView);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(post_item_recyclerView);
        postItemAdapter = new PostItemAdapter();
        post_item_recyclerView.setAdapter(postItemAdapter);
        btn_filter = view.findViewById(R.id.btn_filter);

        //지도 로드를 위해 지도 콜백 호출
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        //필터 버튼의 클릭 리스너
        btn_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetDialogFragment bottomSheetDialogFragment = new MyBottomSheetDialogFragment();  // 필터 버튼을 눌렀을 때 MyBottomSheetDialogFragment의 인스턴스를 생성합니다.
                bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
            }
        });
    }

    //지도가 준비 됐을 때의 콜백
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mClusterManager = new ClusterManager<Post>(getActivity(), mMap);
            mClusterManager.setRenderer(new CustomRenderer<>(getActivity(), mMap, mClusterManager));

            //위치 권한 확인
            if(!checkLocationPermission(getActivity(),REQUEST_LOCATION_PERMISSION)){
                return;
            }

            //현재 내 위치 추적 활성화
            mMap.setMyLocationEnabled(true);
            moveToLastKnownLocation();

            // 클러스터링을 위한 맵의 클릭 리스너 설정
            mMap.setOnCameraIdleListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);

            // MainPageActivity의 LiveData 객체를 가져옴
            if (getView() != null) {
                ((MainPageActivity) getActivity()).livePostList.observe(getViewLifecycleOwner(), new Observer<List<Post>>() {
                    @Override
                    public void onChanged(List<Post> posts) {
                        //포스트 리스트 가져오기
                        postList = posts;
                        // 마커를 추가합니다.
                        if (mClusterManager != null) {
                            mClusterManager.clearItems();
                            mClusterManager.addItems(postList);
                            mClusterManager.cluster();
                        }
                    }
                });
            }

            //마커 하나를 클릭했을 때 이벤트
            mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Post>() {
                @Override
                public boolean onClusterItemClick(Post post) {
                    postItemAdapter.clearItems();  // 아이템을 초기화합니다.
                    //포스트의 uid이용해 유저의 ProfilePicUrl 로드하고 adapter에 추가
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

            // 지도의 마커 영역 밖을 클릭 했을때 이벤트
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    postItemAdapter.clearItems();  // 아이템을 초기화합니다.
                    postItemAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @SuppressLint("MissingPermission")
    private void moveToLastKnownLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                //현재 위치로 카메라 세팅
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                if (postId != null) {
                    // postId가 있을 경우, postId에 해당하는 post를 찾습니다.
                    Post post = findPostById(postId);
                    if (post != null) {
                        // postId에 해당하는 post를 찾았으면 그 위치로 지도의 카메라를 이동합니다.
                        Toast.makeText(getContext(),postId+"로드 성공", Toast.LENGTH_SHORT).show();
                        LatLng postLocation = new LatLng(post.getLatitude(), post.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 30));
                    }
                    else{
                        Toast.makeText(getContext(),postId+"로드 실패", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //아이디로 특정 포스트 찾는 메서드
    public Post findPostById(String postId) {
        for (Post post : postList) {
            if (post.getPostId().equals(postId)) {
                return post;
            }
        }
        return null;  // postId에 해당하는 post를 찾지 못한 경우 null을 반환합니다.
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof PostLoader) {
            postLoader = (PostLoader) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement PostLoader");
        }
    }

    public static MapsFragment getInstance() {
        if (instance == null) {
            instance = new MapsFragment();
        }
        return instance;
    }

    //post에서 전달 받은 postId를 설정
    public void setPostId(String postId) {
        this.postId = postId;
    }
}