package com.example.nearby.main.friends;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nearby.R;
import com.example.nearby.databinding.FragmentFriendsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsListFragment extends Fragment {
    // 친구 리스트 프래그먼트
    // 이 친구 리스트에서는 삭제 버튼이 안 보이게 하면 될 듯

    private FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseReference ref;
    private String currentUid;
    ArrayList<String> followings = new ArrayList<>(); // 팔로잉 사용자의 userID들을 담습니다.
    ArrayList<String> emails = new ArrayList<>(); // 검색된 이메일들을 담을 예정입니다.
    private RecyclerView recyclerView;
    private FriendsListAdapter friendsListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button btnFriendsEdit;
    private List<Friend> friendsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        recyclerView = view.findViewById(R.id.recyclerView); // RecyclerView의 id가 'recyclerView'라고 가정했습니다.

        // 수정 필요
        recyclerView.setAdapter(friendsListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        btnFriendsEdit = view.findViewById(R.id.btn_friends_edit);

        Log.d("LYB", currentUid);

        // DB에서 users 문서에 followings 필드 체크 후 추가해줌
        addFollowingsField();

        // FriendsList 로드
        emails.clear();
//        friendsListAdapter.setFriendsList(emails);

        // 이유빈이 추가함
        friendsList = new ArrayList<>();

        friendsListAdapter = new FriendsListAdapter(friendsList, false);

        moveToFriendEditBtn();
        swipeRefresh();
//        clickUnfollowBtn();

        loadFriendsList();
        friendsListAdapter.notifyDataSetChanged();
        return view;
    }

    private void moveToFriendEditBtn() {
        btnFriendsEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveToFriendEdit = new Intent(getActivity(), FriendsEditActivity.class);
                moveToFriendEdit.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(moveToFriendEdit);
            }
        });
    }

    private void swipeRefresh() {
        // 스와이프 이벤트
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                friendsList.clear();
                //                friendsListAdapter.setFriendsList(emails);
                loadFriendsList();
                friendsListAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

//
//    private void clickUnfollowBtn() {
//        // 언팔로우 버튼
//        unfollowBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                inputEmail = findEmailEdit.getText().toString();
//                Log.d("ODG", inputEmail);
//
//                db.collection("users")
//                        .whereEqualTo("email", inputEmail)
//                        .get()
//                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                if (task.isSuccessful()) {
//                                    if (task.getResult().isEmpty()) {
//                                        // 일치하는 이메일이 없는 경우
//                                        Log.d("ODG", "No matching email found.");
//                                        Toast.makeText(getContext(), "이 친구는 우리 앱에 없어요. 친구에게 추천해주세요.", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        // 일치하는 이메일이 있는 경우
//                                        for (QueryDocumentSnapshot document : task.getResult()) {
//                                            String findUid = document.getId();
//
//                                            // 자신 삭제가 아니라면
//                                            if (!findUid.equals(currentUid)) {
//                                                DocumentReference docRef = db.collection("users").document(currentUid);
//
//                                                // 이미 followings db에 존재하는지 검사
//                                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                                        if (task.isSuccessful()) {
//                                                            DocumentSnapshot document = task.getResult();
//                                                            if (document.exists()) {
//                                                                List<String> followings = (List<String>) document.get("followings");
//                                                                if (followings.contains(findUid)) {
//                                                                    Log.d("ODG", "입력값이 배열에 존재합니다. 고로 삭제합니다.");
//                                                                    onFollowingRemoved(findUid);
//                                                                    Toast.makeText(getContext(), "지금부터 이 친구를 팔로잉하지 않아요.", Toast.LENGTH_SHORT).show();
//                                                                } else {
//                                                                    Log.d(TAG, "입력값이 배열에 존재하지 않아요.");
//                                                                    Log.d("ODG", document.getId() + " => " + document.getData());
//                                                                    Toast.makeText(getContext(), "이 친구는 이미 팔로우 되어 있지 않아요.", Toast.LENGTH_SHORT).show();
//                                                                }
//                                                            } else {
//                                                                Log.d(TAG, "No such document");
//                                                            }
//                                                        } else {
//                                                            Log.d(TAG, "get failed with ", task.getException());
//                                                        }
//                                                    }
//                                                });
//                                            } else {
//                                                // 자삭 은 안돼요
//                                                Toast.makeText(getContext(), "나를 팔로우 취소할 순 없어요. 나 자신을 사랑해주세요.", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    Log.d("ODG", "Error getting documents: ", task.getException());
//                                }
//                            }
//                        });
//            }
//        });
//    }

    private void addFollowingsField() {
        DocumentReference docRef = db.collection("users").document(currentUid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists() && !document.contains("followings")) {
                        // Document가 존재하고 'followings' 필드가 없을 때만 필드를 추가합니다.
                        db.collection("users").document(currentUid)
                                .update("followings", new ArrayList<>())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("LYB", "DocumentSnapshot successfully updated!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("LYB", "Error updating document", e);
                                    }
                                });
                    }
                } else {
                    Log.d("ODG", "get failed with ", task.getException());
                }
            }
        });
    }


//    private void onFollowingRemoved(String inputUid) {
//        // 검색된 이메일의 사용자 id를 following DB에서 삭제
//
//        DocumentReference userRef = db.collection("users").document(currentUid);
//        userRef.update("followings", FieldValue.arrayRemove(inputUid))
//                .addOnSuccessListener(aVoid -> Log.d("ODG", "InputId removed to user followings document"))
//                .addOnFailureListener(e -> Log.w("ODG", "Error removing userID to user followings document", e));
//
//        Log.d("ODG", "Following removed with ID: " + inputUid);
//        Toast.makeText(getContext(), "언팔로잉 성공!", Toast.LENGTH_SHORT).show();
//    }

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
                    } else {
                        Log.d("LYB", "해당 문서를 찾을 수 없습니다.");
                    }
                } else {
                    Log.d("LYB", "문서 가져오기에 실패했습니다.", task.getException());
                }
            }
        });

        Log.d("LYB", "실행됨메롱 ");

        // List 채우기

        friendsList.clear(); // friendsList는 List<Friend> 타입의 멤버 변수입니다.
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
    }
}