package com.example.nearby;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import com.example.nearby.auth.SignUpActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadContentsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "UploadContentsActivity";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    EditText editText;
    Button uploadButton, pickDateButton;
    TextView showDateTextView;
    Uri imageUri;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_contents);

        editText = findViewById(R.id.mainText);
        uploadButton = findViewById(R.id.upload_button);
        pickDateButton = findViewById(R.id.pick_date_button);
        showDateTextView = findViewById(R.id.show_date_textView);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null && !editText.getText().toString().trim().isEmpty() && !showDateTextView.getText().equals("Selected date: ") ){
                    uploadPost(editText.getText().toString().trim(), imageUri);
                }
                else{
                    Toast.makeText(UploadContentsActivity.this, "항목을 모두 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });

        // 위치 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
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
                selectedDate = year+"년" + dayOfMonth + "월" + (month + 1) + "일" ;
                showDateTextView.setText("Selected date: " + selectedDate);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    /*------------------------------------------------------------------------------이미지를 갤러리에서 가져오는 함수-------------------------------------------------------------------------------------*/
    public void pickImageFromGallery(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }

    /*------------------------------------------------------------------------------포스트 업로드 함수-------------------------------------------------------------------------------------*/
    private void uploadPost(String text, Uri imageUri) {
        // 위치 정보를 가져오는 데 사용할 FusedLocationProviderClient 객체 생성
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // 위치 정보 얻기
                                if (ActivityCompat.checkSelfPermission(UploadContentsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    fusedLocationClient.getLastLocation()
                                            .addOnSuccessListener(UploadContentsActivity.this, new OnSuccessListener<Location>() {
                                                @Override
                                                public void onSuccess(Location location) {
                                                    if (location != null) {
                                                        double latitude = location.getLatitude();
                                                        double longitude = location.getLongitude();

                                                        Map<String, Object> post = new HashMap<>();
                                                        post.put("text", text);
                                                        post.put("imageUrl", uri.toString());
                                                        post.put("date", selectedDate); // 선택된 날짜 업로드
                                                        post.put("latitude", latitude); // 위도 업로드
                                                        post.put("longitude", longitude); // 경도 업로드

                                                        db.collection("posts")
                                                                .add(post)
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                        Log.d(TAG, "Post added with ID: " + documentReference.getId());
                                                                        //업로드 성공 메시지
                                                                        Toast.makeText(UploadContentsActivity.this, "업로드 성공!", Toast.LENGTH_SHORT).show();
                                                                        // 액티비티 종료
                                                                        finish();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override //업로드 실패
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.w(TAG, "Error adding post", e);
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error uploading image", e);
                    }
                });
    }


}
