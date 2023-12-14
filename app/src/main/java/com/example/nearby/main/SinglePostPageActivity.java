package com.example.nearby.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.nearby.R;
import com.example.nearby.databinding.ActivityLogInBinding;
import com.example.nearby.databinding.ActivitySinglePostPageBinding;
import com.example.nearby.main.mainpage.CommentBottomSheetDialogFragment;
import com.example.nearby.main.mainpage.ImageAdapter;
import com.example.nearby.main.mainpage.Post;
import com.example.nearby.main.mainpage.PostAdapter;
import com.example.nearby.main.mainpage.TagsAdapter;
import com.example.nearby.main.maps.MapsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class SinglePostPageActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String profileImageUrl;
    private String nickName;
    private Timestamp date;
    private String bigLocation;
    private String smallLocation;
    private String text;
    private ImageAdapter imageAdapter;
    private TagsAdapter tagsAdapter;

    private ActivitySinglePostPageBinding binding;
    private String postId;
    private String postUid;
    private String uid;
    private List<String> imageUrls = new ArrayList<>();
    private List<String> likeList = new ArrayList<>();
    private List<String> tags = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySinglePostPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        uid = auth.getUid();
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");


        SnapHelper snapHelper = new PagerSnapHelper();

        snapHelper.attachToRecyclerView(binding.imgPostRecyclerView);

        Log.d("singlepage", postId);


        // tool bar 세팅
        setToolBar();


        // 스와이프 이벤트
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                imageUrls.clear();
                tags.clear();
                likeList.clear();
                imageAdapter.notifyDataSetChanged();
                tagsAdapter.notifyDataSetChanged();
                getPostData(postId);
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Single post 로드 하기
        imageUrls.clear();
        tags.clear();
        likeList.clear();
        getPostData(postId);

        // button onclick 선언
        binding.icReply.setOnClickListener(v -> {
            CommentBottomSheetDialogFragment fragment =
                    CommentBottomSheetDialogFragment.newInstance(postId);
            fragment.show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), "commentDialog");
        });

        binding.icEmptyHeart.setOnClickListener(v -> {
            checkLikeStatus(postId, uid, binding.icEmptyHeart);
        });
    }
    
    // 수정본
    private void getPostData(String postId) {
        db.collection("posts").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        // 인스턴스 초기화
                        postUid = document.contains("uid") ? document.getString("uid") : ""; // nickname, profileimage 설정예정
                        Timestamp defaultTimestamp = new Timestamp(new Date(90, 0, 1));
                        date = document.contains("date") ? document.getTimestamp("date") : defaultTimestamp;
                        bigLocation = document.contains("bigLocationName") ? document.getString("bigLocationName") : "";
                        smallLocation = document.contains("smallLocationName") ? document.getString("smallLocationName") : "";
                        text = document.contains("text") ? document.getString("text") : "";

                        // recyclerView list 초기화
                        imageUrls = document.contains("imageUrls") ? (List<String>) document.get("imageUrls") : new ArrayList<>();
                        likeList = document.contains("likes") ? (List<String>) document.get("likes") : new ArrayList<>();
                        tags = document.contains("tags") ? (List<String>) document.get("tags") : new ArrayList<>();

                        if (!postUid.equals(uid)) {
                            binding.topAppBar.setVisibility(View.GONE);
                        }

                        registerPostData();

                        getUserData(postUid);

                        Log.d("SinglePost", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("SinglePost", "No such document");
                    }
                } else {
                    Log.d("SinglePost", "get failed with ", task.getException());
                }
            }
        });
    }

    private void getUserData(String postUid) {
        db.collection("users").document(postUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            profileImageUrl = document.getString("profilePicUrl");
                            nickName = document.getString("nickname");

                            registerUserData();
                        }
                    } else {
                        Log.d("SinglePost", "get failed with ", task.getException());
                    }
                });
    }

    private void registerPostData() {
        if (date != null) {
            Date dateObject = date.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
            String dateString = sdf.format(dateObject);
            binding.tvPostDate.setText(dateString);
        }
        binding.tvBigLocationName.setText(bigLocation);
        binding.tvSmallLocationName.setText(smallLocation);
        binding.tvPostMainText.setText(text);

        // Image Post recyclerView 등록
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.imgPostRecyclerView.setLayoutManager(layoutManager);
        imageAdapter = new ImageAdapter(this, imageUrls);
        binding.imgPostRecyclerView.setAdapter(imageAdapter);
        imageAdapter.notifyDataSetChanged();

        // Tag recyclerView 등록
        LinearLayoutManager tagsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.tagRecyclerview.setLayoutManager(tagsLayoutManager);
        List<String> uniqueTags = new ArrayList<>(new LinkedHashSet<>(tags));
        tagsAdapter = new TagsAdapter(uniqueTags);
        binding.tagRecyclerview.setAdapter(tagsAdapter);
        tagsAdapter.notifyDataSetChanged();


        // 좋아요 상태 불러오기
        if (likeList != null && likeList.contains(uid)) {
            // 좋아요를 이미 눌렀으면 꽉 찬 하트 이미지 로드
            binding.icEmptyHeart.setImageResource(R.drawable.ic_full_heart);
        } else {
            // 좋아요를 누르지 않았으면 빈 하트 이미지 로드
            binding.icEmptyHeart.setImageResource(R.drawable.ic_empty_heart);
        }
    }

    private void registerUserData() {
        Glide.with(binding.imgProfile.getContext())
                .load(profileImageUrl)
                .circleCrop()
                .error(R.drawable.stock_profile)
                .into(binding.imgProfile);

        binding.tvNickName.setText(nickName);
    }

    private void checkLikeStatus(String postId, String userId, ImageButton likeButton) {
        db.collection("posts").document(postId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        List<String> likes = (List<String>) document.get("likes");
                        if (likes != null && likes.contains(userId)) {
                            // 좋아요를 이미 눌렀으면 제거
                            removeLike(postId, userId, likeButton);
                        } else {
                            // 좋아요를 누르지 않았으면 추가
                            submitLike(postId, userId, likeButton);
                        }
                    } else {
                        // 에러 처리
                    }
                });
    }

    private void submitLike(String postId, String userId, ImageButton likeButton) {
        db.collection("posts").document(postId)
                .update("likes", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    // 좋아요 추가 성공
                    loadLikes(postId, userId, likeButton);
                })
                .addOnFailureListener(e -> {
                    // 에러 처리
                });
    }

    private void removeLike(String postId, String userId, ImageButton likeButton) {
        db.collection("posts").document(postId)
                .update("likes", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> {
                    // 좋아요 제거 성공
                    loadLikes(postId, userId, likeButton);
                })
                .addOnFailureListener(e -> {
                    // 에러 처리
                });
    }

    private void loadLikes(String postId, String userId, ImageButton likeButton) {
        db.collection("posts").document(postId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        List<String> likes = (List<String>) document.get("likes");
                        int likesCount = likes != null ? likes.size() : 0;
                        // 좋아요 수에 따라 UI 업데이트
                        if (likes != null && likes.contains(userId)) {
                            // 좋아요를 이미 눌렀으면 꽉 찬 하트 이미지 로드
                            likeButton.setImageResource(R.drawable.ic_full_heart);
                        } else {
                            // 좋아요를 누르지 않았으면 빈 하트 이미지 로드
                            likeButton.setImageResource(R.drawable.ic_empty_heart);
                        }
                    } else {
                        // 에러 처리
                    }
                });
    }

    private void setToolBar() {
        binding.topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.post_delete_btn) {

                    LayoutInflater inflater = (LayoutInflater) SinglePostPageActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View dialogView = inflater.inflate(R.layout.post_delete, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(SinglePostPageActivity.this);

                    builder.setView(dialogView);

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
                    Button btnConfirm = dialogView.findViewById(R.id.btn_dialog_confirm);

                    // '아니오' 버튼 클릭 시 다이얼로그 닫기
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    btnConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deletePost(postId);
                            finish();
                            dialog.dismiss();
                            }
                    });

                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void deletePost(String postId) {
        // users 안의 postIds 필드 안의 postId 삭제
        db.collection("users").document(uid).update("postIds", FieldValue.arrayRemove(postId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("PostDelete", "삭제됐음");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("PostDelete", "삭제안됨 오류뜸");
                    }
                });

        // posts 안의 post 문서 삭제
        db.collection("posts").document(postId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("PostDelete", "삭제됐음");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("PostDelete", "삭제안됨 오류뜸");
                    }
                });
    }
}
