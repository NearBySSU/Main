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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

public class FriendsFragment extends Fragment {

//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_friends, container, false);
//    }

    private EditText findEmailEdit;
    private Button followBtn;
    private Button unfollowBtn;

    private FirebaseAuth auth;
    private FirebaseStorage storage;
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

        String currentUserId = auth.getCurrentUser().getUid();
//        DocumentReference txDocTargetUser = firestore.collection("users").document(currentUserId);

        Log.d("ODG", currentUserId);

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
                                    } else {
                                        // 일치하는 이메일이 있는 경우
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String findUid = document.getId();
                                            onFollowingAdded(findUid);
                                            Log.d("ODG", document.getId() + " => " + document.getData());
                                        }
                                    }
                                } else {
                                    Log.d("ODG", "Error getting documents: ", task.getException());
                                }
                            }
                        });



            }
        });





//    public class FollowDto {
//        // 이 사람을 팔로잉하는 사람들
//        private Map<String, Boolean> followers = new HashMap<>();
//
//        // 이 사람이 팔로잉 중인 사람들
//        private Map<String, Boolean> followings = new HashMap<>();
//
//        // getters and setters

//    }

        return view;

    }


    private void onFollowingAdded(String inputUid) {
        // 시발 뭐야
        // 팔로잉 + 1 해줘야지
        // 검색된 이메일의 사용자 id를 following DB에 추가

        DocumentReference userRef = db.collection("users").document(currentUid);
        userRef.update("followings", FieldValue.arrayUnion(inputUid))
                .addOnSuccessListener(aVoid -> Log.d("ODG", "InputId added to user followings document"))
                .addOnFailureListener(e -> Log.w("ODG", "Error adding userID to user followings documnet", e));

        Log.d("ODG", "Following added with ID: " + inputUid);
        Toast.makeText(getContext(), "팔로잉 성공!", Toast.LENGTH_SHORT).show();


    }
}
