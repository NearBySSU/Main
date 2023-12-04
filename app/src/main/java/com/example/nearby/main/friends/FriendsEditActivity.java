package com.example.nearby.main.friends;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.nearby.R;
import com.example.nearby.databinding.ActivityFriendEditBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsEditActivity extends AppCompatActivity {

    private FriendsListAdapter friendsListAdapter;

    private List<Friend> friendsList;
    private ActivityFriendEditBinding binding;
    private String inputEmail;
    private String currentUid;
    private SwipeRefreshLayout swipeRefreshLayout;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<String> followings; // 팔로잉 사용자의 userID들을 담습니다.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (friendsList == null) {
            friendsList = new ArrayList<>();
        }
        friendsListAdapter = new FriendsListAdapter(friendsList, true);

        // showDeleteButton을 true로 설정
        friendsListAdapter = new FriendsListAdapter(friendsList, true);

        // 어댑터랑 리사이클러뷰 연결하기
        binding.rvFriendsEditList.setAdapter(friendsListAdapter);
        binding.rvFriendsEditList.setLayoutManager(new LinearLayoutManager(this));

        loadFriendsList();
        swipeRefresh();
        clickFollowBtn();

    }

    private void clickFollowBtn() {
        // 팔로우 버튼
        binding.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputEmail = binding.findEmail.getText().toString();
                Log.d("LYB", inputEmail);

                db.collection("users")
                        .whereEqualTo("email", inputEmail)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().isEmpty()) {
                                        // 일치하는 이메일이 없는 경우
                                        Log.d("LYB", "No matching email found.");
                                        Toast.makeText(FriendsEditActivity.this, "이 친구는 우리 앱에 없어요. 친구에게 추천해주세요.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // 일치하는 이메일이 있는 경우
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String findUid = document.getId();

                                            // 자신 추가가 아니라면
                                            if (!findUid.equals(currentUid)) {
                                                DocumentReference docRef = db.collection("users").document(currentUid);

                                                // 이미 followings db에 존재하는지 검사
                                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()) {
                                                                List<String> followings = (List<String>) document.get("followings");
                                                                if (followings.contains(findUid)) {
                                                                    Log.d("LYB", "입력값이 배열에 존재합니다.");
                                                                    Toast.makeText(FriendsEditActivity.this, "이 친구는 이미 팔로우 되어 있어요.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Log.d("LYB", "입력값이 배열에 존재하지 않습니다. 친구추가할게요");
                                                                    onFollowingAdded(findUid);
                                                                    Log.d("LYB", document.getId() + " => " + document.getData());
                                                                    Toast.makeText(FriendsEditActivity.this, "지금부터 이 친구를 팔로잉해요.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                Log.d("LYB", "No such document");
                                                            }
                                                        } else {
                                                            Log.d("LYB", "get failed with ", task.getException());
                                                        }
                                                    }
                                                });
                                            } else {
                                                // 자추 는 안돼요
                                                Toast.makeText(FriendsEditActivity.this, "나 자신을 아무리 사랑해도 나를 팔로우할 순 없어요.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                } else {
                                    Log.d("LYB", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });
    }

    private void onFollowingAdded(String inputUid) {
        // 검색된 이메일의 사용자 id를 following DB에 추가

        DocumentReference userRef = db.collection("users").document(currentUid);
        userRef.update("followings", FieldValue.arrayUnion(inputUid))
                .addOnSuccessListener(aVoid -> Log.d("LYB", "InputId added to user followings document"))
                .addOnFailureListener(e -> Log.w("LYB", "Error adding userID to user followings document", e));

        Log.d("LYB", "Following added with ID: " + inputUid);
        Toast.makeText(FriendsEditActivity.this, "팔로잉 성공!", Toast.LENGTH_SHORT).show();
    }

    private void loadFriendsList() {
        // db에서 user -> followings 가져오기
        DocumentReference userRef = db.collection("users").document(currentUid);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        followings = (ArrayList<String>) document.get("followings");

                        // List 채우기
                        friendsList.clear();
                        for (String userID : followings) {
                            Log.d("LYB", "배열에 들어옴");
                            db.collection("users").document(userID)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    String profileUrl = document.getString("profilePicUrl");
                                                    String friendName = document.getString("nickname");
//                                                    String postCount = document.getString("postCount");
                                                    Friend friend = new Friend(profileUrl, friendName, userID);
                                                    friendsList.add(friend);
                                                    friendsListAdapter.notifyDataSetChanged();
                                                } else {
                                                    Log.d("LYB", "No such document");
                                                }
                                            } else {
                                                Log.d("LYB", "get failed with ", task.getException());
                                            }
                                        }
                                    });
                        }
                    } else {
                        Log.d("LYB", "해당 문서를 찾을 수 없습니다.");
                    }
                } else {
                    Log.d("LYB", "문서 가져오기에 실패했습니다.", task.getException());
                }
            }
        });
    }

    private void swipeRefresh() {
        // 스와이프 이벤트
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                friendsList.clear();
                loadFriendsList();
                friendsListAdapter.notifyDataSetChanged();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

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