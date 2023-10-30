package com.example.nearby;

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


        String text = "NearBy";
        int purple = ContextCompat.getColor(this, R.color.firstColor);
        int teal = ContextCompat.getColor(this, R.color.lastColor);
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        spannable.setSpan(new LinearGradientSpan(text, text, purple, teal), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvSplash.setText(spannable);


        loadingStart();
    }

    private void loadingStart(){
        Handler handler=new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
                Intent intent=new Intent(getApplicationContext(), LogInActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);
    }
}