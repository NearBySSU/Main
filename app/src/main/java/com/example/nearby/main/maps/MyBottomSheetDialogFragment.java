package com.example.nearby.main.maps;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nearby.R;
import com.example.nearby.databinding.FragmentBottomSheetDialogBinding;
import com.example.nearby.databinding.FragmentMainListBinding;
import com.example.nearby.main.MainPageActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private FragmentBottomSheetDialogBinding binding;
    String selectedChipText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBottomSheetDialogBinding.inflate(inflater, container, false);

        ChipGroup chipGroupTag = binding.chipGroupTag;

        chipGroupTag.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    selectedChipText = chip.getText().toString();
                }
            }
        });

        return binding.getRoot();
    }

    //다이얼로그가 없어졌을때의 동작 정의
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        MainPageActivity activity = (MainPageActivity) getActivity();
        if (activity != null) {
        //    activity.updateSelectedTag(selectedChipText); //MainActivity로 변경사항 전달
        }
    }
}

