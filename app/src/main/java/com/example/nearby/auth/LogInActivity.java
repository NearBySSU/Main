package com.example.nearby.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nearby.LinearGradientSpan;
import com.example.nearby.R;
import com.example.nearby.main.MainPageActivity;
import com.example.nearby.databinding.ActivityLogInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class LogInActivity extends AppCompatActivity {
    private Button loginBtn;
    private EditText EmailEdit;
    private EditText passwordEdit;
    private ActivityLogInBinding binding;
    private FirebaseFirestore db;

    String TAG = "login";
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        loginBtn = binding.logInButton;
        EmailEdit = binding.emailText;
        passwordEdit = binding.passwordText;

        //그라데이션을 위한 처리
        String text = "NearBy";
        int purple = ContextCompat.getColor(this, R.color.firstColor);
        int teal = ContextCompat.getColor(this, R.color.lastColor);
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        spannable.setSpan(new LinearGradientSpan(text, text, purple, teal), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvLoginImage.setText(spannable);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LogInActivity.this, MainPageActivity.class));
            finish();
        }

        //로그인 버튼
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.emailText.getText().toString().trim();
                String pwd = binding.passwordText.getText().toString().trim();

                // 입력값 검증
                if (email.isEmpty() || pwd.isEmpty()) {
                    Toast.makeText(LogInActivity.this, "이메일 또는 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 형식 검증
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(LogInActivity.this, "올바른 이메일 형식을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                //로그인 하기
                mAuth.signInWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {//성공했을때
                                    updateFcmToken();
                                    Intent intent = new Intent(LogInActivity.this, MainPageActivity.class);
                                    startActivity(intent);
                                } else {//실패했을때
                                    Toast.makeText(LogInActivity.this, "로그인 오류", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        //가입하기로 이동
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    // 배경화면 눌렀을 때 키보드 내려가는 기능
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        return super.dispatchTouchEvent(ev);
    }

    //토큰을 업데이트 하는 함수
    private void updateFcmToken() {
        FirebaseUser user = mAuth.getCurrentUser();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String fcmToken = task.getResult();

                        db.collection("users")
                                .document(user.getUid())
                                .update("fcmToken", fcmToken)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "FCM token updated for ID: " + user.getUid());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error updating FCM token", e);
                                    }
                                });
                    }
                });
    }

}