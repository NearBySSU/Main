package com.example.nearby.main.mainpage;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearby.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class CommentBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private FirebaseFirestore db;
    private String postId; // 포스트 ID
    private RecyclerView recyclerView; // 댓글 리스트를 보여줄 RecyclerView
    private CommentAdapter commentAdapter; // 댓글 리스트를 관리할 Adapter
    FirebaseAuth mAuth;


    public CommentBottomSheetDialogFragment() {
    }

    public static CommentBottomSheetDialogFragment newInstance(String postId) {
        CommentBottomSheetDialogFragment fragment = new CommentBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setOnShowListener(dialog -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment_bottom_sheet_dialog, container, false);
        EditText etComment = view.findViewById(R.id.etComment);
        ImageButton btnSubmit = view.findViewById(R.id.btnSubmit);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();

        // 댓글 제출 버튼 클릭 리스너
        btnSubmit.setOnClickListener(v -> {
            String commentText = etComment.getText().toString();
            if (!commentText.isEmpty()) {
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("commentText", commentText);
                commentMap.put("commenterId", mAuth.getUid());
                commentMap.put("timestamp", System.currentTimeMillis()); // 현재 시간 기록

                db.collection("posts").document(postId).collection("comments")
                        .add(commentMap)
                        .addOnSuccessListener(documentReference -> {
                            etComment.setText(""); // 댓글 입력창 초기화
                            loadComments(); // 댓글 목록 갱신
                        })
                        .addOnFailureListener(e -> {
                            // 에러 처리
                        });
            }
        });
        loadComments(); // 댓글 목록 로드
        return view;
    }

    private void loadComments() {
        db.collection("posts").document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Comment> comments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String commentText = document.getString("commentText");
                            String commenterId = document.getString("commenterId");
                            long timestamp = document.getLong("timestamp"); // 시간 정보를 long 타입으로 가져옴

                            // 댓글 작성자의 프로필 사진을 가져오기 위해 댓글 작성자의 정보를 가져옴
                            db.collection("users").document(commenterId).get()
                                    .addOnSuccessListener(userDocument -> {
                                        String profilePicUrl = userDocument.getString("profilePicUrl"); // 프로필 사진 URL 가져옴
                                        Comment comment = new Comment(commentText, commenterId, profilePicUrl, timestamp);
                                        comments.add(comment);
                                        commentAdapter = new CommentAdapter(comments);
                                        recyclerView.setAdapter(commentAdapter);
                                    })
                                    .addOnFailureListener(e -> {
                                        // 에러 처리
                                    });
                        }
                    } else {
                        // 에러 처리
                    }
                });
    }
}
