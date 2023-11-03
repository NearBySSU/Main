    package com.example.nearby;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import android.annotation.SuppressLint;
    import android.app.DatePickerDialog;
    import android.content.ClipData;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.location.Location;
    import android.net.Uri;
    import android.os.Bundle;
    import android.provider.MediaStore;
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
    import com.google.android.gms.tasks.Task;
    import com.google.android.gms.tasks.Tasks;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;
    import com.google.firebase.storage.UploadTask;

    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.UUID;

    public class UploadContentsActivity extends AppCompatActivity {

        private static final int PICK_IMAGE_REQUEST = 1;
        private static final String TAG = "UploadContentsActivity";
        ArrayList<Uri> uriList = new ArrayList<>();     // 이미지의 uri를 담을 ArrayList 객체

        RecyclerView recyclerView;  // 이미지를 보여줄 리사이클러뷰
        MultiImageAdapter adapter;  // 리사이클러뷰에 적용시킬 어댑터


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        EditText editText;
        Button uploadButton, pickDateButton,pickImageButton;
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
            pickImageButton = findViewById(R.id.pick_image_button);

            // 위치 권한 확인 및 요청
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "업로드를 위해 위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }

            //이미지 선택 버튼
            pickImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2222);
                }
            });

            recyclerView = findViewById(R.id.recyclerView);

            //업로드 버튼
            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!uriList.isEmpty() && !editText.getText().toString().trim().isEmpty() && !showDateTextView.getText().equals("Selected date: ") ){
                        uploadPost();
                    }
                    else{
                        Toast.makeText(UploadContentsActivity.this, "항목을 모두 입력해 주세요", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //날짜 선택 버튼
            pickDateButton.setOnClickListener(new View.OnClickListener() {
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
                    selectedDate = year+"년" + dayOfMonth + "월" + (month + 1) + "일" ;
                    showDateTextView.setText("Selected date: " + selectedDate);
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

                        adapter = new MultiImageAdapter(uriList, getApplicationContext());
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

                            adapter = new MultiImageAdapter(uriList, getApplicationContext());
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
                StorageReference imageRef = storageRef.child("images/" + UUID.randomUUID().toString());
                tasks.add(imageRef.putFile(uri).continueWithTask(task -> imageRef.getDownloadUrl()));
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
                        }
                    });
        }

        private Map<String, Object> createPostMap(List<Object> urls, double latitude, double longitude) {
            Map<String, Object> post = new HashMap<>();
            post.put("text", editText.getText().toString());
            post.put("imageUrls", urls);
            post.put("date", selectedDate);
            post.put("latitude", latitude);
            post.put("longitude", longitude);
            return post;
        }

        private void onPostUploaded(DocumentReference documentReference) {
            Log.d(TAG, "Post added with ID: " + documentReference.getId());
            Toast.makeText(UploadContentsActivity.this, "업로드 성공!", Toast.LENGTH_SHORT).show();
            onBackPressed();
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

    }

