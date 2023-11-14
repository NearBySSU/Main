package com.example.nearby.main.profile;

import static android.app.Activity.RESULT_OK;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.nearby.R;
import com.example.nearby.auth.LogInActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Collections;
import java.util.UUID;


public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE = 1;
    private Button logoutButton;
    private Button uploadProfilePicButton;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    private static final String TAG = "ProfileFragment";



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        logoutButton = view.findViewById(R.id.logoutButton);
//        uploadProfilePicButton = view.findViewById(R.id.btn_profile_pic);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getActivity(), LogInActivity.class));
                getActivity().finish();
            }
        });
//        uploadProfilePicButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
//            }
//        });

        return view;
    }

//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_profile_app_bar, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.btn_logout) {
//            // 로그아웃 코드
//            mAuth.signOut();
//            Log.d("LYB" ,"LOGOUT");
//            startActivity(new Intent(getActivity(), LogInActivity.class));
//            getActivity().finish();
//            return true;
//        } else {
//            return super.onOptionsItemSelected(item);
//        }
//    }
//


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        // ProgressDialog 초기화
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("프로필 이미지 변경 중...");
    }

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
                                                        Toast.makeText(getContext(),"프로필 이미지 변경 성공!",Toast.LENGTH_SHORT).show();

                                                        // ProgressDialog 닫기
                                                        progressDialog.dismiss();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error updating document", e);
                                                        Toast.makeText(getContext(),"프로필 이미지 변경 실패",Toast.LENGTH_SHORT).show();

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
