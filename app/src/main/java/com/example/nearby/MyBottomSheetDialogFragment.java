package com.example.nearby;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.Observer;
import com.example.nearby.main.MainPageActivity;
import com.example.nearby.main.mainpage.Post;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {
    ChipGroup chipGroup;
    List<Post> filteredPosts;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);
        chipGroup = view.findViewById(R.id.chipGroupTag);

        MainPageActivity activity = (MainPageActivity) getActivity();
        // 복원할 태그가 있는 경우 Chip 상태 업데이트
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View v = chipGroup.getChildAt(i);
            if (v instanceof Chip) {
                Chip chip = (Chip) v;
                if (activity.selectedChips.contains(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }

        // MainPageActivity의 LiveData 객체를 가져옴
        ((MainPageActivity) getActivity()).livePostList.observe(getViewLifecycleOwner(), new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> posts) {

            }
        });
        return view;
    }

    //다이얼로그가 없어졌을때의 동작 정의
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        MainPageActivity activity = (MainPageActivity) getActivity();
        saveSelectedChipsToActivity(activity);
        filterPostListAndSaveToLiveData(activity);
    }

    //선택된 칩의 리스트를 저장
    private void saveSelectedChipsToActivity(MainPageActivity activity) {
        activity.selectedChips.clear();

        //태그들을 리스트에 저장
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View v = chipGroup.getChildAt(i);
            if (v instanceof Chip) {
                Chip chip = (Chip) v;
                if (chip.isChecked()) {
                    activity.selectedChips.add(chip.getText().toString());
                }
            }
        }
    }

    //
    public  void filterPostListAndSaveToLiveData(MainPageActivity activity) {
        // postList 필터링
        List<Post> postList = new ArrayList<>(activity.originalPostList);
        if (postList != null) {
            filteredPosts = postList.stream()
                    .filter(post -> post.getTags().containsAll(activity.selectedChips))
                    .collect(Collectors.toList());

            // 필터링된 리스트를 LiveData의 값으로 설정
            activity.livePostList.setValue(filteredPosts);
        }
    }
}

