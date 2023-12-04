package com.example.nearby.main.friends;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        recyclerView.setAdapter(friendsListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        btnFriendsEdit = view.findViewById(R.id.btn_friends_edit);

        Log.d("LYB", currentUid);

        // DB에서 users 문서에 followings 필드 체크 후 추가해줌
        addFollowingsField();

        emails.clear();
        friendsList = new ArrayList<>();

        // showDeleteButton을 false로 설정
        friendsListAdapter = new FriendsListAdapter(friendsList, false);

        initAdapter();
        moveToFriendEditBtn();
        swipeRefresh();
        loadFriendsList();
        friendsListAdapter.notifyDataSetChanged();
        return view;
    }

    private void initAdapter(){
        recyclerView.setAdapter(friendsListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
                loadFriendsList();
                friendsListAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

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


    private void loadFriendsList() {
        Log.d("LYB", "로드가 됐음");
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

                        friendsList.clear(); // friendsList는 List<Friend> 타입의 멤버 변수입니다.
                        for (String userID : followings) {
                            Log.d("LYB", "배열에 들어옴");
                            db.collection("users").document(userID)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("LYB", "task가 성공함");
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Log.d("LYB", "문서가 존재함");
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
}