package com.example.nearby.main.upload;

import static com.example.nearby.Utils.checkLocationPermission;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import com.example.nearby.R;

import com.example.nearby.databinding.ActivityUploadContentsBinding;
import com.example.nearby.main.MainPageActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UploadContentsActivity extends AppCompatActivity {

    private static final String TAG = "UploadContentsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    ArrayList<Uri> uriList = new ArrayList<>();     // 이미지의 uri를 담을 ArrayList 객체
    ArrayList<String> checkedTags = new ArrayList<>();    // 체크된 Chip들의 ID를 저장할 ArrayList 생성

    RecyclerView recyclerView;  // 이미지를 보여줄 리사이클러뷰
    MultiImageAdapter adapter;  // 리사이클러뷰에 적용시킬 어댑터
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    StorageReference storageRef = storage.getReference();
    ActivityUploadContentsBinding binding;
    Uri imageUri;
    String selectedDate;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadContentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recyclerView = binding.recyclerView;
        uriList.add(null); // 마지막에 null 항목 추가
        adapter = new MultiImageAdapter(uriList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
        uid = user.getUid();
        recyclerView = binding.recyclerView;
        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        chipGroup.setSingleSelection(false);

        //위치권한 확인
        checkLocationPermission(this,LOCATION_PERMISSION_REQUEST_CODE);

        //업로드 버튼
        binding.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //체크된 태그들을 배열에 담기
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroup.getChildAt(i);
                    if (chip.isChecked()) {
                        checkedTags.add(chip.getText().toString());

                    }
                }
                if (!checkedTags.isEmpty()&&!uriList.isEmpty() && !binding.mainText.getText().toString().trim().isEmpty() && !binding.showDateTextView.getText().equals("Selected date: ") ){
                    uploadPost();
                }
                else{
                    Toast.makeText(UploadContentsActivity.this, "항목을 모두 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 백 버튼 처리
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                Intent intent = new Intent(UploadContentsActivity.this, MainPageActivity.class);
                startActivity(intent);
            }
        });

        //날짜 선택 버튼
        binding.pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });
    }

    /*------------------------------------------------------------------------------날짜 선택 함수-------------------------------------------------------------------------------------*/
    public void pickDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(UploadContentsActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDate = year+"년" + (month+1) + "월" + dayOfMonth + "일" ;
                binding.showDateTextView.setText("Selected date: " + selectedDate);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    /*------------------------------------------------------------------------------이미지를 갤러리에서 가져오고 썸네일을 만드는 함수-------------------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2222){
            if(data == null){   // 어떤 이미지도 선택하지 않은 경우
                Toast.makeText(getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
            }
            else{   // 이미지를 하나라도 선택한 경우
                if(data.getClipData() == null){     // 이미지를 하나만 선택한 경우
                    Log.e("single choice: ", String.valueOf(data.getData()));
                    Uri imageUri = data.getData();
                    uriList.add(imageUri);


                    adapter = new MultiImageAdapter(uriList, UploadContentsActivity.this);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
                }
                else{      // 이미지를 여러장 선택한 경우
                    ClipData clipData = data.getClipData();
                    Log.e("clipData", String.valueOf(clipData.getItemCount()));

                    if(clipData.getItemCount() > 10){   // 선택한 이미지가 11장 이상인 경우
                        Toast.makeText(getApplicationContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                    }
                    else{   // 선택한 이미지가 1장 이상 10장 이하인 경우
                        Log.e(TAG, "multiple choice");

                        for (int i = 0; i < clipData.getItemCount(); i++){
                            Uri imageUri = clipData.getItemAt(i).getUri();  // 선택한 이미지들의 uri를 가져온다.
                            try {
                                uriList.add(imageUri);  //uri를 list에 담는다.

                            } catch (Exception e) {
                                Log.e(TAG, "File select error", e);
                            }
                        }

                        if (!uriList.contains(null)) {
                            uriList.add(null); // 마지막에 null 항목 추가
                        }


                        adapter = new MultiImageAdapter(uriList, UploadContentsActivity.this);
                        recyclerView.setAdapter(adapter);   // 리사이클러뷰에 어댑터 세팅
                        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));     // 리사이클러뷰 수평 스크롤 적용
                    }
                }
            }
        }
    }
    /*------------------------------------------------------------------------------포스트 업로드 함수-------------------------------------------------------------------------------------*/
    private void uploadPost() {
        Toast.makeText(UploadContentsActivity.this, "이미지 업로드 중 ...", Toast.LENGTH_SHORT).show();
        List<Task<Uri>> tasks = uploadImagesToStorage();

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(this::onImagesUploaded)
                .addOnFailureListener(this::onImageUploadFailure);
    }

    private List<Task<Uri>> uploadImagesToStorage() {
        List<Task<Uri>> tasks = new ArrayList<>();
        for (Uri uri : uriList) {
            if (uri != null) {
                StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());
                tasks.add(imageRef.putFile(uri).continueWithTask(task -> imageRef.getDownloadUrl()));
            }
        }
        return tasks;
    }


    private void onImagesUploaded(List<Object> urls) {
        if (isLocationPermissionGranted()) {
            uploadPostWithLocation(urls);
        } else {
            showLocationPermissionError();
            requestLocationPermission();
        }
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(UploadContentsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void uploadPostWithLocation(List<Object> urls) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(UploadContentsActivity.this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        Map<String, Object> post = createPostMap(urls, latitude, longitude);

                        db.collection("posts")
                                .add(post)
                                .addOnSuccessListener(this::onPostUploaded)
                                .addOnFailureListener(this::onPostUploadFailure);
                    } else {
                        showLocationError();
                        requestLocationPermission();
                    }
                });
    }

    private Map<String, Object> createPostMap(List<Object> urls, double latitude, double longitude) {
        Map<String, Object> post = new HashMap<>();
        post.put("text", binding.mainText.getText().toString());
        post.put("imageUrls", urls);
        post.put("date", selectedDate);
        post.put("latitude", latitude);
        post.put("longitude", longitude);
        post.put("uid", uid);
        post.put("tags",checkedTags);

        return post;
    }

    private void onPostUploaded(DocumentReference documentReference) {

        // 포스트의 ID를 사용자 db에 추가
        String postId = documentReference.getId();
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.update("postIds", FieldValue.arrayUnion(postId))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "PostId added to user document"))
                .addOnFailureListener(e -> Log.w(TAG, "Error adding postId to user document", e));

        Log.d(TAG, "Post added with ID: " + documentReference.getId());
        Toast.makeText(UploadContentsActivity.this, "업로드 성공!", Toast.LENGTH_SHORT).show();
        //MainList로 돌아가기
        Intent intent = new Intent(this, MainPageActivity.class);
        startActivity(intent);
    }

    private void onPostUploadFailure(@NonNull Exception e) {
        Log.w(TAG, "Error adding post", e);
        Toast.makeText(UploadContentsActivity.this, "업로드 실패", Toast.LENGTH_SHORT).show();
    }

    private void showLocationError() {
        Toast.makeText(UploadContentsActivity.this, "위치 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
    }

    private void showLocationPermissionError() {
        Toast.makeText(UploadContentsActivity.this, "업로드 실패! 위치 권한을 허용해 주세요", Toast.LENGTH_SHORT).show();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(UploadContentsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    private void onImageUploadFailure(@NonNull Exception e) {
        Log.w(TAG, "Error uploading images", e);
    }

    /*------------------------------------------------------------------------------------------------------------------------------------------------------------*/
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

