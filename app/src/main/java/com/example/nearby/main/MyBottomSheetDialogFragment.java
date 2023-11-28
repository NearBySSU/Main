package com.example.nearby.main;

import android.content.Context;
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
import com.example.nearby.main.MainPageActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {
    String selectedChipText;
    ChipGroup chipGroupTag;
    private FragmentBottomSheetDialogBinding binding;
    List<String> selectedChipsTag = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBottomSheetDialogBinding.inflate(inflater, container, false);
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);
        chipGroupTag = view.findViewById(R.id.chip_group_tag);


//        chipGroupTag.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(ChipGroup group, int checkedId) {
//                Chip chip = group.findViewById(checkedId);
//                if (chip != null) {
//                    selectedChipText = chip.getText().toString();
//                    listener.onTagSelected(selectedChipText);
//                    Toast.makeText(getContext(), "chip 선택함.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        chipGroupTag.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    String chipText = chip.getText().toString();
                    if (chip.isChecked()) {
                        selectedChipsTag.add(chipText);
                    } else {
                        selectedChipsTag.remove(chipText);
                    }
                    listener.onTagsSelected(selectedChipsTag);
                    Toast.makeText(getContext(), "chip 선택함.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    //인터페이스를 생성하고, MainListFragment 클래스에서 이 인터페이스를 구현하여 선택한 태그 받아오기
    public interface OnTagSelectedListener {
        void onTagSelected(String tag);
        void onTagsSelected(List<String> tags);
    }


    private OnTagSelectedListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnTagSelectedListener) {
            listener = (OnTagSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTagSelectedListener");
        }
    }


    //다이얼로그가 없어졌을때의 동작 정의
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        MainPageActivity activity = (MainPageActivity) getActivity();
        if (activity != null) {
            //activity.updateSelectedTag(selectedChipText); //MainActivity로 변경사항 전달
        }
    }
}

