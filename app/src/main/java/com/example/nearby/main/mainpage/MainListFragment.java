package com.example.nearby.main.mainpage;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

import static com.example.nearby.Utils.checkLocationPermission;
import static com.example.nearby.Utils.getLocationName;
import static com.example.nearby.Utils.requestLocationPermission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nearby.databinding.FragmentMainListBinding;
import com.example.nearby.main.MainPageActivity;
import com.example.nearby.main.PostLoader;
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
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private FragmentMainListBinding binding;
    private PostLoader postLoader;
    private List<Post> postList;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //PostLoader를 초기화
        if (context instanceof PostLoader) {
            postLoader = (PostLoader) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement PostLoader");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainListBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postAdapter = new PostAdapter(postList);
        binding.recyclerView.setAdapter(postAdapter);

        //현재 위치 띄우기
        setLocationTv();

        // MainPageActivity의 LiveData 객체를 가져옴
        ((MainPageActivity) getActivity()).livePostList.observe(getViewLifecycleOwner(), new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {
                // 포스트 리스트가 변경되면 RecyclerView를 업데이트
                postList = posts;
                postAdapter.setPostList(postList);
                postAdapter.notifyDataSetChanged();
            }
        });


        //스와이프 이벤트 : 포스트 다시 로드
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        postLoader.reloadPostList();
                        binding.swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        return rootView;
    }

    //현재위치의 이름을 set하는 함수
    @SuppressLint("MissingPermission")
    private void setLocationTv() {
        //권한 체크
        if (!checkLocationPermission(getActivity(), REQUEST_LOCATION_PERMISSION)) {
            return;
        }
        //tv에 이름을 set
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                String[] locationNames = getLocationName(getActivity(),location);
                if (locationNames != null) {
                    binding.tvLocationTitle.setText(locationNames[0]);
                    binding.tvLocationCity.setText(locationNames[1]);
                }
            }
        });
    }
}
