package com.example.nearby.main.mainpage;

import static com.example.nearby.Utils.getLocationName;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nearby.MyBottomSheetDialogFragment;
import com.example.nearby.databinding.FragmentMainListBinding;
import com.example.nearby.main.MainPageActivity;
import com.example.nearby.main.PostLoader;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainListFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private FragmentMainListBinding binding;
    private PostLoader postLoader;
    private List<Post> postList;
    ImageButton btn_filter;


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
        postAdapter = new PostAdapter(postList, getContext());
        binding.recyclerView.setAdapter(postAdapter);
        btn_filter = binding.btnFilter;

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

        //필터 버튼의 클릭 리스너
        btn_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBottomSheetDialogFragment bottomSheetDialogFragment = new MyBottomSheetDialogFragment();  // 필터 버튼을 눌렀을 때 MyBottomSheetDialogFragment의 인스턴스를 생성합니다.
                bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
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
                        // MainPageActivity의 selectedChips 초기화
                        ((MainPageActivity) getActivity()).selectedTags.clear();
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

        //tv에 이름을 set
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                String[] locationNames = getLocationName(getActivity(), location);
                if (locationNames != null) {
                    binding.tvLocationTitle.setText(locationNames[0]);
                    binding.tvLocationCity.setText(locationNames[1]);
                }
            }
        });
    }
}
