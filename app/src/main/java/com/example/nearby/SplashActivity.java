package com.example.nearby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.Toast;

import com.example.nearby.LinearGradientSpan;
import com.example.nearby.R;
import com.example.nearby.auth.LogInActivity;
import com.example.nearby.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {
    private PermissionSupport permission;

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
        permissionCheck();
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

    // 권한 체크
    private void permissionCheck() {

        // PermissionSupport.java 클래스 객체 생성
        permission = new PermissionSupport(this, this);

        // 권한 체크 후 리턴이 false로 들어오면
        if (!permission.checkPermission()){
            //권한 요청
            permission.requestPermission();
            Toast.makeText(this, "Nearby 이용을 위해 위치 권한,알림 권한을 허용해 주세요", Toast.LENGTH_LONG).show();
        }
        else{
            loadingStart();
        }
    }

    // Request Permission에 대한 결과 값 받아와
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //여기서도 리턴이 false로 들어온다면 (사용자가 권한 허용 거부)
        if (!permission.permissionResult(requestCode, permissions, grantResults)) {
            // 다시 permission 요청
            permission.requestPermission();
        }
        else{
            loadingStart();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
