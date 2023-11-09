package com.example.nearby.main.mainpage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearby.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment_bottom_sheet_dialog, container, false);
        EditText etComment = view.findViewById(R.id.etComment);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 댓글 제출 버튼 클릭 리스너
        btnSubmit.setOnClickListener(v -> {
            String commentText = etComment.getText().toString();
            if (!commentText.isEmpty()) {
                Map<String, Object> comment = new HashMap<>();
                comment.put("commentText", commentText);
                comment.put("commenterId", "User ID"); // 여기에 실제 사용자 ID를 넣어야 함

                db.collection("posts").document(postId).collection("comments")
                        .add(comment)
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
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Comment> comments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String commentText = document.getString("commentText");
                            String commenterId = document.getString("commenterId");
                            Comment comment = new Comment(commentText, commenterId);
                            comments.add(comment);
                        }
                        commentAdapter = new CommentAdapter(comments);
                        recyclerView.setAdapter(commentAdapter);
                    } else {
                        // 에러 처리
                    }
                });
    }

}
