package com.example.nearby.main.profile;

import static android.app.Activity.RESULT_OK;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nearby.R;
import com.example.nearby.auth.LogInActivity;
import com.example.nearby.main.mainpage.Post;
import com.example.nearby.main.maps.PostItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE = 1;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private String uid;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String currentPostId;
    private String currentImageUrl;
    private Timestamp currentDate;
    private static final String TAG = "ProfileFragment";
    private TextView friendNum;
    private TextView postNum;
    private TextView nickNameField;
    private ImageView profileImageView;

    private Toolbar toolbar;

    private ArrayList<ProfileItem> profileItemList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        setHasOptionsMenu(true);
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        // ProgressDialog 초기화
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("프로필 이미지 변경 중...");

        toolbar = rootView.findViewById(R.id.top_app_bar);
        mAuth = FirebaseAuth.getInstance();
        recyclerView = rootView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        friendNum = rootView.findViewById(R.id.tv_friend_num);
        postNum = rootView.findViewById(R.id.tv_post_num);
        profileImageView = rootView.findViewById(R.id.img_profile);
        nickNameField = rootView.findViewById(R.id.tv_profile_name);


        // 상단 바 눌렀을 때의 버튼 처리
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.btn_logout) {
                    // 로그아웃 버튼
                    mAuth.signOut();
                    Log.d("LYB", "LOGOUT");
                    startActivity(new Intent(getActivity(), LogInActivity.class));
                    getActivity().finish();
                    return true;
                } else if (id == R.id.btn_profile_pic) {
                    // 프로필 변경 버튼
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                    return true;
                } else {
                    return false;
                }
            }
        });


        // recyclerView 등록
        profileAdapter = new ProfileAdapter(getContext(), profileItemList);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(profileAdapter);

        // 스와이프 이벤트
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                profileItemList.clear();
                profileAdapter.setProfileItemList(profileItemList);
                addProfileList();
//                Log.d("ODG", imageUrlList.get(1));
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        // profile img list 로드 하기
        profileItemList.clear();
        profileAdapter.notifyDataSetChanged();

        addProfileList();
        profileAdapter.setProfileItemList(profileItemList);

        return rootView;
    }

//    private void addProfileList() {
//        db = FirebaseFirestore.getInstance();
//        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        List<String> postIds = (List<String>) document.get("postIds");
//                        for (String postId : postIds) {
//                            db.collection("posts").document(postId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                    ArrayList<String> imageUrls = (ArrayList<String>) documentSnapshot.get("imageUrls");
//                                    if (imageUrls == null) {
//                                        // If "imageUrls" field doesn't exist, create it as an empty list
//                                        imageUrls = new ArrayList<>();
//                                        documentSnapshot.getReference().update("imageUrls", imageUrls);
//                                    }
//                                    if (imageUrls.size() > 0) {
//                                        imageUrlList.add(imageUrls.get(0));
//                                        Log.d("ODG", imageUrlList.get(0));
//                                        profileAdapter.notifyDataSetChanged();  // 데이터가 추가될 때마다 UI 갱신
//                                    }
//                                }
//                            });
//                        }
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
//    }

    //수정본
    private void addProfileList() {
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String nickname = (String) document.get("nickname");
                        List<String> followings = (List<String>) document.get("followings");
                        int numOfFollowings, numOfPosts;
                        if (followings == null) {
                            followings = new ArrayList<>(); // "followings" 필드가 없을 경우 빈 리스트로 초기화
                            numOfFollowings = 0;
                        } else {
                            numOfFollowings = followings.size();
                        }
                        String profilePicUrl = (String) document.get("profilePicUrl");

                        List<String> postIds = (List<String>) document.get("postIds");
                        if (postIds == null) {
                            postIds = new ArrayList<>(); // "postIds" 필드가 없을 경우 빈 리스트로 초기화
                            numOfPosts = 0;
                        } else {
                            numOfPosts = postIds.size();
                        }

                        friendNum.setText(String.valueOf(numOfFollowings));
                        postNum.setText(String.valueOf(numOfPosts));
                        nickNameField.setText(nickname);

                        Glide.with(profileImageView.getContext())
                                .load(profilePicUrl)
                                .circleCrop()
                                .error(R.drawable.stock_profile)
                                .into(profileImageView);


                        for (String postId : postIds) {
                            db.collection("posts").document(postId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    // img
                                    ArrayList<String> imageUrls = (ArrayList<String>) documentSnapshot.get("imageUrls");
                                    if (imageUrls == null) {
                                        // "imageUrls" 필드가 없을 경우 빈 리스트로 초기화
                                        imageUrls = new ArrayList<>();
                                        documentSnapshot.getReference().update("imageUrls", imageUrls);
                                    }
                                    if (imageUrls.size() > 0) {
                                        currentImageUrl = imageUrls.get(0);
                                    }

                                    currentPostId = postId;
                                    // date
                                    Timestamp dates = (Timestamp) documentSnapshot.get("date");

                                    currentDate = dates;




                                    ProfileItem profileItem = new ProfileItem(currentDate, currentImageUrl, currentPostId);
                                    profileItemList.add(profileItem);
                                    profileAdapter.notifyDataSetChanged();  // 데이터가 추가될 때마다 UI 갱신

                                }

                            });
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile_app_bar, menu);
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
//        storage = FirebaseStorage.getInstance();
//        db = FirebaseFirestore.getInstance();
//        // ProgressDialog 초기화
//        progressDialog = new ProgressDialog(getActivity());
//        progressDialog.setMessage("프로필 이미지 변경 중...");
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                StorageReference storageRef = storage.getReference();
                StorageReference imageRef = storageRef.child("users/" + UUID.randomUUID().toString());

                // ProgressDialog 표시
                progressDialog.show();

                imageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        DocumentReference userRef = db.collection("users").document(mAuth.getUid());
                                        userRef.set(Collections.singletonMap("profilePicUrl", uri.toString()), SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                                        Toast.makeText(getContext(), "프로필 이미지 변경 성공!", Toast.LENGTH_SHORT).show();

                                                        // ProgressDialog 닫기
                                                        progressDialog.dismiss();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error updating document", e);
                                                        Toast.makeText(getContext(), "프로필 이미지 변경 실패", Toast.LENGTH_SHORT).show();

                                                        // ProgressDialog 닫기
                                                        progressDialog.dismiss();

                                                    }
                                                });
                                    }
                                });
                            }
                        });

            }
        }
    }
}
