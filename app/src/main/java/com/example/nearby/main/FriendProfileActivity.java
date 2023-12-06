package com.example.nearby.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.nearby.R;
import com.example.nearby.databinding.ActivityFriendProfileBinding;
import com.example.nearby.main.profile.ProfileAdapter;
import com.example.nearby.main.profile.ProfileItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendProfileActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;
    private String inputUid;
    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private String currentPostId;
    private String currentImageUrl;
    private Timestamp currentDate;
    private TextView friendNum;
    private TextView postNum;
    private TextView nickNameField;
    private ImageView profileImageView;
    private ArrayList<ProfileItem> profileItemList = new ArrayList<>();
    private static final String TAG = "FriendProfileActivity";
    private ActivityFriendProfileBinding binding;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        inputUid = intent.getStringExtra("inputUid");

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        uid = auth.getUid();
        recyclerView = findViewById(R.id.recyclerView);
        friendNum = findViewById(R.id.tv_friend_num);
        postNum = findViewById(R.id.tv_post_num);
        profileImageView = findViewById(R.id.img_profile);
        nickNameField = findViewById(R.id.tv_profile_name);

        checkFollowing();
        clickFollowBtn();
        clickUnFollowBtn();
        swipeRefresh();
        initAdapter();

        // profile img list 로드 하기
        profileItemList.clear();
        profileAdapter.notifyDataSetChanged();

        addProfileList(inputUid);
        profileAdapter.setProfileItemList(profileItemList);
    }

    private void checkFollowing() {
        if ( inputUid.equals(uid) ) {
            binding.tvHaveToFollow.setVisibility(View.INVISIBLE);
            binding.tvAlreadyFollowing.setVisibility(View.INVISIBLE);
        } else {
            db.collection("users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> followings = (List<String>) document.get("followings");
                            if (followings == null) {
                                followings = new ArrayList<>(); // "followings" 필드가 없을 경우 빈 리스트로 초기화
                            }
                            // 'followings' 리스트에 해당 사용자가 있는지 확인
                            if (followings.contains(inputUid)) {
                                // 팔로우 중인 경우
                                binding.tvHaveToFollow.setVisibility(View.INVISIBLE);
                                binding.tvAlreadyFollowing.setVisibility(View.VISIBLE);
                            } else {
                                // 팔로우 중이 아닌 경우
                                binding.tvHaveToFollow.setVisibility(View.VISIBLE);
                                binding.tvAlreadyFollowing.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            });
        }
    }

    private void swipeRefresh() {
        // 스와이프 이벤트
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                profileItemList.clear();
                profileAdapter.setProfileItemList(profileItemList);
                addProfileList(inputUid);
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void clickFollowBtn() {
        binding.tvHaveToFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 'followings' 필드에 친구를 추가합니다.
                db.collection("users").document(currentUserId)
                        .update("followings", FieldValue.arrayUnion(inputUid))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                Toast.makeText(FriendProfileActivity.this, "팔로우 성공", Toast.LENGTH_SHORT).show();
                                checkFollowing();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                            }
                        });
            }
        });

    }

    private void clickUnFollowBtn() {
        binding.tvAlreadyFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("users").document(currentUserId)
                        .update("followings", FieldValue.arrayRemove(inputUid))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                Toast.makeText(FriendProfileActivity.this, "팔로우 취소", Toast.LENGTH_SHORT).show();
                                checkFollowing();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                            }
                        });
            }
        });
    }

    private void initAdapter() {
        // recyclerView 등록
        profileAdapter = new ProfileAdapter(this, profileItemList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(profileAdapter);
    }

    private void addProfileList(String inputUid) {
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(inputUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                    }
                }
            }
        });
    }
}
