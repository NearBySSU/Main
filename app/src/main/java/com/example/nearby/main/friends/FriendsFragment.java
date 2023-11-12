package com.example.nearby.main.friends;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nearby.R;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
    private EditText findEmailEdit;
    private Button followBtn;
    private Button unfollowBtn;

    private FirebaseAuth auth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DatabaseReference ref;

    private String inputEmail;
    private String currentUid;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        findEmailEdit = view.findViewById(R.id.findEmail);
        followBtn = view.findViewById(R.id.followBtn);
        unfollowBtn = view.findViewById(R.id.unFollowBtn);

        Log.d("ODG", currentUid);

        // DB에서 users 문서에 followings 필드 체크 후 추가해줌
        addFollowingsField();

        // 팔로우 버튼
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputEmail = findEmailEdit.getText().toString();
                Log.d("ODG", inputEmail);

                db.collection("users")
                        .whereEqualTo("email", inputEmail)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().isEmpty()) {
                                        // 일치하는 이메일이 없는 경우
                                        Log.d("ODG", "No matching email found.");
                                        Toast.makeText(getContext(), "이 친구는 우리 앱에 없어요. 친구에게 추천해주세요.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // 일치하는 이메일이 있는 경우
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String findUid = document.getId();

                                            // 자신 추가가 아니라면
                                            if ( !findUid.equals(currentUid) ) {
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
                                                                    Log.d("ODG", "입력값이 배열에 존재합니다.");
                                                                    Toast.makeText(getContext(), "이 친구는 이미 팔로우 되어 있어요.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Log.d(TAG, "입력값이 배열에 존재하지 않습니다. 친구추가할게요");
                                                                    onFollowingAdded(findUid);
                                                                    Log.d("ODG", document.getId() + " => " + document.getData());
                                                                    Toast.makeText(getContext(), "지금부터 이 친구를 팔로잉해요.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                Log.d(TAG, "No such document");
                                                            }
                                                        } else {
                                                            Log.d(TAG, "get failed with ", task.getException());
                                                        }
                                                    }
                                                });
                                            } else {
                                                // 자추 는 안돼요
                                                Toast.makeText(getContext(), "나 자신을 아무리 사랑해도 나를 팔로우할 순 없어요.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                } else {
                                    Log.d("ODG", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });



    // 언팔로우 버튼
        unfollowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputEmail = findEmailEdit.getText().toString();
                Log.d("ODG", inputEmail);

                db.collection("users")
                        .whereEqualTo("email", inputEmail)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().isEmpty()) {
                                        // 일치하는 이메일이 없는 경우
                                        Log.d("ODG", "No matching email found.");
                                        Toast.makeText(getContext(), "이 친구는 우리 앱에 없어요. 친구에게 추천해주세요.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // 일치하는 이메일이 있는 경우
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String findUid = document.getId();

                                            // 자신 삭제가 아니라면
                                            if ( !findUid.equals(currentUid) ) {
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
                                                                    Log.d("ODG", "입력값이 배열에 존재합니다. 고로 삭제합니다.");
                                                                    onFollowingRemoved(findUid);
                                                                    Toast.makeText(getContext(), "지금부터 이 친구를 팔로잉하지 않아요.", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Log.d(TAG, "입력값이 배열에 존재하지 않아요.");
                                                                    Log.d("ODG", document.getId() + " => " + document.getData());
                                                                    Toast.makeText(getContext(), "이 친구는 이미 팔로우 되어 있지 않아요.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                Log.d(TAG, "No such document");
                                                            }
                                                        } else {
                                                            Log.d(TAG, "get failed with ", task.getException());
                                                        }
                                                    }
                                                });
                                            } else {
                                                // 자삭 은 안돼요
                                                Toast.makeText(getContext(), "나를 팔로우 취소할 순 없어요. 나 자신을 사랑해주세요.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                } else {
                                    Log.d("ODG", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });
        return view;
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
                                        Log.d("ODG", "DocumentSnapshot successfully updated!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("ODG", "Error updating document", e);
                                    }
                                });
                    }
                } else {
                    Log.d("ODG", "get failed with ", task.getException());
                }
            }
        });
    }

    private void onFollowingAdded(String inputUid) {
        // 팔로잉 + 1 해줘야지
        // 검색된 이메일의 사용자 id를 following DB에 추가

        DocumentReference userRef = db.collection("users").document(currentUid);
        userRef.update("followings", FieldValue.arrayUnion(inputUid))
                .addOnSuccessListener(aVoid -> Log.d("ODG", "InputId added to user followings document"))
                .addOnFailureListener(e -> Log.w("ODG", "Error adding userID to user followings document", e));

        Log.d("ODG", "Following added with ID: " + inputUid);
        Toast.makeText(getContext(), "팔로잉 성공!", Toast.LENGTH_SHORT).show();
    }

    private void onFollowingRemoved(String inputUid) {
        // 검색된 이메일의 사용자 id를 following DB에서 삭제

        DocumentReference userRef = db.collection("users").document(currentUid);
        userRef.update("followings", FieldValue.arrayRemove(inputUid))
                .addOnSuccessListener(aVoid -> Log.d("ODG", "InputId removed to user followings document"))
                .addOnFailureListener(e -> Log.w("ODG", "Error removing userID to user followings document", e));

        Log.d("ODG", "Following removed with ID: " + inputUid);
        Toast.makeText(getContext(), "언팔로잉 성공!", Toast.LENGTH_SHORT).show();
    }
}