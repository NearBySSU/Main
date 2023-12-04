package com.example.nearby;

import static com.example.nearby.Utils.checkLocationPermission;
import static com.example.nearby.Utils.checkNotifiPermission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.example.nearby.LinearGradientSpan;
import com.example.nearby.R;
import com.example.nearby.auth.LogInActivity;
import com.example.nearby.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //로고 그라데이션을 위한 코드
        String text = "NearBy";
        int purple = ContextCompat.getColor(this, R.color.firstColor);
        int teal = ContextCompat.getColor(this, R.color.lastColor);
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        spannable.setSpan(new LinearGradientSpan(text, text, purple, teal), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvSplash.setText(spannable);

        // 권한 확인 및 요청
        if (!checkLocationPermission(this, 1000) || !checkNotifiPermission(this, 2000)) {
            return;
        }

        //로딩 스타트
        loadingStart();
    }

    private void loadingStart() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }

    // 권한이 승인되었는지 확인 후 로딩 시작
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (checkLocationPermission(this, 1000)) {
                loadingStart();
            }
        } else if (requestCode == 2000) {
            if (checkNotifiPermission(this, 2000)) {
                loadingStart();
            }
        }
    }
}
