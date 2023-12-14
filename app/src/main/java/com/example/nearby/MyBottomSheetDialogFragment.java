package com.example.nearby;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;

import com.example.nearby.main.MainPageActivity;
import com.example.nearby.main.SinglePostPageActivity;
import com.example.nearby.main.mainpage.Post;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {
    ChipGroup chipGroupTag;
    ChipGroup chipGroupDistance;
    ChipGroup chipGroupDate;
    List<Post> filteredPosts;
    List<String> manuallyAddedTags = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);
        chipGroupTag = view.findViewById(R.id.chipGroupTag);
        chipGroupDistance = view.findViewById(R.id.chipGroupDistance);
        chipGroupDate = view.findViewById(R.id.chipGroupDate);
        MainPageActivity activity = (MainPageActivity) getActivity();

        Chip inputChip = view.findViewById(R.id.tagChip04);

        // 복원할 추가 태그가 있는 경우 Chip 상태 업데이트
        for (String tag : activity.manuallyAddedTags) {
            Chip newChip = new Chip(new ContextThemeWrapper(getContext(), R.style.AppTheme));
            newChip.setText(tag);
            newChip.setTextSize(16);
            newChip.setChipEndPadding(10);
            newChip.setChipStartPadding(10);
            newChip.setCheckable(true);

            chipGroupTag.addView(newChip, chipGroupTag.getChildCount() - 1); // 마지막에서 두 번째 위치에 추가
        }

        inputChip.setOnClickListener(v -> {
            LayoutInflater inflater2 = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater2.inflate(R.layout.tag_self, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            dialog.show();

            EditText input = dialogView.findViewById(R.id.edit_text);
            Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
            Button btnConfirm = dialogView.findViewById(R.id.btn_dialog_confirm);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = input.getText().toString();

                    // 중복을 체크하여 새로운 태그인 경우에만 추가합니다.
                    if (!manuallyAddedTags.contains(text)) {
                        // 새 칩을 생성합니다.
                        Chip newChip = new Chip(new ContextThemeWrapper(getContext(), R.style.AppTheme));
                        newChip.setText(text);
                        newChip.setTextSize(16);
                        newChip.setChipEndPadding(20);
                        newChip.setChipStartPadding(20);
                        newChip.setCheckable(true);

                        // 추가한 태그를 리스트에 저장
                        manuallyAddedTags.add(text);

                        // ChipGroup에 새 칩을 추가합니다.
                        chipGroupTag.addView(newChip, chipGroupTag.getChildCount() - 1); // 마지막에서 두 번째 위치에 추가
                    } else {
                        // 이미 추가된 태그인 경우 사용자에게 알림을 표시합니다.
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                        alertDialog.setTitle("중복된 태그");
                        alertDialog.setMessage("이미 추가된 태그입니다.");
                        alertDialog.setPositiveButton("확인", (dialog1, which1) -> dialog1.dismiss());
                        alertDialog.show();
                    }
                    dialog.dismiss();
                }
            });
        });


        // 복원할 태그가 있는 경우 Chip 상태 업데이트
        for (int i = 0; i < chipGroupTag.getChildCount(); i++) {
            View v = chipGroupTag.getChildAt(i);
            if (v instanceof Chip) {
                Chip chip = (Chip) v;
                if (activity.selectedTags.contains(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }

        // 복원할 거리가 있는 경우 Chip 상태 업데이트
        int selectedDistance = activity.selectedDistance;
        if (selectedDistance != -1) {
            String distanceStr;
            switch (selectedDistance) {
                case 1:
                    distanceStr = "가까이";
                    break;
                case 3:
                    distanceStr = "적당히";
                    break;
                case 5:
                    distanceStr = "멀리";
                    break;
                default:
                    distanceStr = "멀리"; // 기본값을 "멀리"로 설정
            }

            for (int i = 0; i < chipGroupDistance.getChildCount(); i++) {
                View v = chipGroupDistance.getChildAt(i);
                if (v instanceof Chip) {
                    Chip chip = (Chip) v;
                    if (chip.getText().toString().equals(distanceStr)) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        } else {
            Chip defaultChip = chipGroupDistance.findViewById(R.id.areaChip03); //  복원할게 없는 경우 "멀리"에 해당하는 Chip 선택
            defaultChip.setChecked(true);
        }

        // 복원할 날짜 범위가 있는 경우 Chip 상태 업데이트
        int selectedDate = activity.selectedDate;
        if (selectedDate != Integer.MAX_VALUE) {
            String dateStr;
            switch (selectedDate) {
                case 6:
                    dateStr = "6달 이내";
                    break;
                case 12:
                    dateStr = "1년 이내";
                    break;
                case 36:
                    dateStr = "3년 이내";
                    break;
                default:
                    dateStr = "전체"; // 기본값을 "전체"로 설정
            }

            for (int i = 0; i < chipGroupDate.getChildCount(); i++) {
                View v = chipGroupDate.getChildAt(i);
                if (v instanceof Chip) {
                    Chip chip = (Chip) v;
                    if (chip.getText().toString().equals(dateStr)) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        } else {
            Chip defaultChip = chipGroupDate.findViewById(R.id.dateChip01); // 복원할게 없는 경우 "전체"에 해당하는 Chip 선택
            defaultChip.setChecked(true);
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


        // 추가된 태그들을 저장
        activity.manuallyAddedTags.addAll(manuallyAddedTags);

        filterPostListAndSaveToLiveData(activity);
    }

    //선택된 칩의 리스트를 저장
    private void saveSelectedChipsToActivity(MainPageActivity activity) {
        activity.selectedTags.clear();

        //태그들을 리스트에 저장
        for (int i = 0; i < chipGroupTag.getChildCount(); i++) {
            View v = chipGroupTag.getChildAt(i);
            if (v instanceof Chip) {
                Chip chip = (Chip) v;
                if (chip.isChecked()) {
                    activity.selectedTags.add(chip.getText().toString());
                }
            }
        }

        // 거리 선택 상태 저장
        int checkedDistanceChipId = chipGroupDistance.getCheckedChipId();
        if (checkedDistanceChipId != -1) {
            Chip checkedChip = chipGroupDistance.findViewById(checkedDistanceChipId);
            String distanceStr = checkedChip.getText().toString();
            int selectedDistance;
            switch (distanceStr) {
                case "가까이":
                    selectedDistance = 1;
                    break;
                case "적당히":
                    selectedDistance = 3;
                    break;
                case "멀리":
                    selectedDistance = 5;
                    break;
                default:
                    selectedDistance = -1;
            }
            activity.selectedDistance = selectedDistance;
        } else {
            activity.selectedDistance = -1; // 선택된 칩이 없는 경우 -1로 설정
        }

        //날짜를 저장
        int checkedChipId = chipGroupDate.getCheckedChipId();
        if (checkedChipId != -1) {
            Chip checkedChip = chipGroupDate.findViewById(checkedChipId);
            String dateStr = checkedChip.getText().toString();
            int selectedDate;
            switch (dateStr) {
                case "전체":
                    selectedDate = Integer.MAX_VALUE;
                    break;
                case "6달 이내":
                    selectedDate = 6;
                    break;
                case "1년 이내":
                    selectedDate = 12;
                    break;
                case "3년 이내":
                    selectedDate = 36;
                    break;
                default:
                    selectedDate = Integer.MAX_VALUE;
            }
            activity.selectedDate = selectedDate;
        } else {
            activity.selectedDate = Integer.MAX_VALUE; // 선택된 칩이 없는 경우 최대값으로 설정
        }
    }

    //
    public void filterPostListAndSaveToLiveData(MainPageActivity activity) {
        // postList 필터링
        List<Post> postList = new ArrayList<>(activity.originalPostList);
        if (postList != null) {
            filteredPosts = postList.stream()
                    .filter(post -> post.getTags().containsAll(activity.selectedTags))
                    .filter(post -> activity.selectedDistance == -1 || post.getDistance() <= activity.selectedDistance) // 선택된 거리보다 작은 post만 필터링
                    .filter(post -> post.getMonthsAgo() <= activity.selectedDate) // 선택된 날짜 범위보다 작거나 같은 post만 필터링
                    .collect(Collectors.toList());

            // 필터링된 리스트를 LiveData의 값으로 설정
            activity.livePostList.setValue(filteredPosts);
        }
    }
}

