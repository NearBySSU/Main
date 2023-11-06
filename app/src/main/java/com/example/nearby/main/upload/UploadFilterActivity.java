package com.example.nearby.main.upload;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.nearby.R;
import com.example.nearby.databinding.ActivityUploadFilterBinding;

public class UploadFilterActivity extends AppCompatActivity {
    private ActivityUploadFilterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityUploadFilterBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
    }
}