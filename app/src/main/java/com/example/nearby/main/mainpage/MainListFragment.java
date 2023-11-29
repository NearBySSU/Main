package com.example.nearby.main.mainpage;

import static com.example.nearby.Utils.checkLocationPermission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nearby.databinding.FragmentMainListBinding;
import com.example.nearby.main.MainPageActivity;
import com.example.nearby.main.PostLoader;
import com.example.nearby.main.MyBottomSheetDialogFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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


        //스와이프 이벤트
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postLoader.reloadPostList();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        //필터 버튼의 클릭 리스너
        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetDialogFragment bottomSheetDialogFragment = new MyBottomSheetDialogFragment();
                bottomSheetDialogFragment.setChipSelectedListener(selectedChips -> {
                    // 여기에 선택된 칩들에 대한 처리를 추가하세요.
                    Log.d("LYB", "MapsFragment에서 선택한 칩들: " + selectedChips.toString());
                });
                bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
            }
        });

        return binding.getRoot();
    }

    public PostAdapter getPostAdapter() {
        return postAdapter;
    }



    @SuppressLint("MissingPermission")
    private void setLocationTv(){
        //권한 체크
        if(!checkLocationPermission(getActivity(),REQUEST_LOCATION_PERMISSION)){
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
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

}