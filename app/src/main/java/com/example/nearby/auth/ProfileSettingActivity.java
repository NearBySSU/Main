package com.example.nearby.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.example.nearby.databinding.ActivityProfileSettingBinding;
import com.example.nearby.main.MainPageActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.UUID;

public class ProfileSettingActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    // 사진 업로드를 위한 필드 추가
    private static final int PICK_IMAGE_REQUEST = 2222;
    private Uri selectedImageUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityProfileSettingBinding binding = ActivityProfileSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 사진 업로드 버튼 처리 추가
        binding.tvProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        binding.btnCheckFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nicknameToCheck = binding.etProfileNickname.getText().toString(); // EditText에서 닉네임을 가져옴

                if(nicknameToCheck.isEmpty()){
                    Toast.makeText(ProfileSettingActivity.this, "닉네임 중복 확인을 먼저 해주세요", Toast.LENGTH_SHORT).show();
                }else{
                    db.collection("users")
                            .whereEqualTo("nickname", nicknameToCheck)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().isEmpty()) {
                                            // 동일한 닉네임이 이미 존재함
                                            Log.d("ProfileSettingActivity", "Nickname already exists.");
                                            Toast.makeText(ProfileSettingActivity.this, "이미 사용중인 닉네임입니다.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // 동일한 닉네임이 존재하지 않음. 새 닉네임을 저장할 수 있음
                                            binding.tvNicknameIsChecked.setVisibility(View.VISIBLE);
                                            binding.tvNicknameHaveToCheck.setVisibility(View.INVISIBLE);
                                            Toast.makeText(ProfileSettingActivity.this, "닉네임 사용이 가능합니다.", Toast.LENGTH_SHORT).show();
                                            Log.d("ProfileSettingActivity", "Nickname is available.");
                                            Intent intent = new Intent(ProfileSettingActivity.this, MainPageActivity.class);
                                            startActivity(intent);
                                        }
                                    } else {
                                        Log.d("ProfileSettingActivity", "Error checking nickname.", task.getException());
                                    }
                                }
                            });
                }

            }
        });
    }

    // 사진 선택 메소드 추가
    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // 선택된 사진 처리와 업로드 메소드 추가
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            selectedImageUri = data.getData();
            uploadImage(selectedImageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("profile_images/" + UUID.randomUUID().toString());
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d("ProfileSettingActivity", "Image uploaded. URL: " + uri.toString());
                                    // 여기서 uri를 사용하여 사용자 프로필에 이미지 URL을 저장할 수 있습니다.
                                    // 예: db.collection("users").document(uid).update("profileImageUrl", uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("ProfileSettingActivity", "Image upload failure.", e);
                        }
                    });
        }
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
}
