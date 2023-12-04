package com.example.nearby.main.friends;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    // 친구 편집할 수 있는 액티비티
    // 여기서는 버튼 보이게 해야 함

    private FriendsListAdapter friendsListAdapter;

    private List<Friend> friendsList;
    private ActivityFriendEditBinding binding;
    private String inputEmail;
    private String currentUid;

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

        // Create an adapter with the list of friends and show the delete button
        friendsListAdapter = new FriendsListAdapter(friendsList, true);

        // 어댑터랑 리사이클러뷰 연결하기
        binding.rvFriendsEditList.setAdapter(friendsListAdapter);
        binding.rvFriendsEditList.setLayoutManager(new LinearLayoutManager(this));

        // friends 리스트 불러오기
        loadFriendsList();

        clickFollowBtn();
        clickDelteBtn();

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
                .addOnSuccessListener(aVoid -> Log.d("ODG", "InputId added to user followings document"))
                .addOnFailureListener(e -> Log.w("ODG", "Error adding userID to user followings document", e));

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
                        // 이제 'followings' 배열 리스트를 원하는대로 사용할 수 있습니다.

                        // List 채우기
                        friendsList.clear();
                        for (String userID : followings) {
                            db.collection("users").document(userID)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    String profileUrl = document.getString("profileUrl");
                                                    String friendName = document.getString("friendName");
                                                    String newPost = document.getString("newPost");
                                                    String postAdd = document.getString("postAdd");
                                                    String postCount = document.getString("postCount");
                                                    Friend friend = new Friend(profileUrl, friendName, newPost, postAdd, postCount, userID);
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

    private void clickDelteBtn(){
        friendsListAdapter.setOnDeleteButtonClickListener(new FriendsListAdapter.OnDeleteButtonClickListener() {
            @Override
            public void onDeleteButtonClick(int position) {
                String friendId = friendsList.get(position).getFriendId();

                // Firestore에서 해당 친구를 바로 삭제합니다.
                db.collection("users").document(friendId)
                        .update("followings", FieldValue.arrayRemove(currentUid))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Firestore에서 성공적으로 삭제했다면, 로컬 리스트에서도 삭제합니다.
                                friendsList.remove(position);
                                friendsListAdapter.notifyItemRemoved(position);
                                Toast.makeText(FriendsEditActivity.this, "친구 삭제에 성공했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // 삭제에 실패했을 때 에러 메시지를 보여줍니다.
                                Toast.makeText(FriendsEditActivity.this, "친구 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}