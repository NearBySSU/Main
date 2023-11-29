package com.example.nearby.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nearby.R;
import com.example.nearby.databinding.FragmentBottomSheetDialogBinding;
import com.example.nearby.main.MainPageActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {
    String selectedChipText;
    ChipGroup chipGroupTag;
    private FragmentBottomSheetDialogBinding binding;
    List<String> selectedChipsTag = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBottomSheetDialogBinding.inflate(inflater, container, false);
        View view = binding.getRoot(); // 수정된 부분
        chipGroupTag = binding.chipGroupTag;

        getSelectedChipTexts(chipGroupTag);


        return view;
    }

    //인터페이스를 생성하고, MainListFragment 클래스에서 이 인터페이스를 구현하여 선택한 태그 받아오기

    public interface OnChipSelectedListener {

        void onChipSelected(List<String> selectedChips);
    }


    //다이얼로그가 없어졌을때의 동작 정의
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        MainPageActivity activity = (MainPageActivity) getActivity();
        if (activity != null) {
            Log.d("LYB" , "다이얼로그 닫음");
            // 선택한 칩들을 전달
            notifyChipSelected(selectedChipsTag);
        }
    }

    // 칩이 선택되었을 때 이 메서드를 호출합니다
    private void notifyChipSelected(List<String> selectedChips) {

        Log.d("LYB", "선택한 칩들을 선택했을 때 이 메소드 호출");
        if (chipSelectedListener != null && !selectedChips.isEmpty()) {
            Log.d("LYB", "리스너 호출");
            chipSelectedListener.onChipSelected(selectedChips);
        }
    }


    private OnChipSelectedListener chipSelectedListener;
    public void setChipSelectedListener(OnChipSelectedListener listener) {
        this.chipSelectedListener = listener;
    }
    private void getSelectedChipTexts(ChipGroup chipGroup) {
        int childCount = chipGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = chipGroup.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                if(chip.isChecked()){
                    Log.d("LYB", "태그 칩 선택함");
                    selectedChipsTag.add(chip.getText().toString());
                }
            }
        }
    }


}

