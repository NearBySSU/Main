package com.example.nearby.main.mainpage;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.example.nearby.R;
import com.example.nearby.main.CommentBottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> postList;
    private FirebaseFirestore db;
    private static final String TAG = "PostAdapter";

    FirebaseAuth auth;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = postList.get(position); //포스트 한개의 객체 얻기
        holder.date.setText(post.getDate()); //날짜 설정
        holder.mainText.setText(post.getText()); //메인 텍스트 설정
        String postUid = post.getUserId(); //포스트 주인의 아이디
        Log.d(TAG, postUid);

        //post의 uid로 부터 프로필 사진 가져오기
        db.collection("users").document(postUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            String profilePicUrl = document.getString("profilePicUrl");

                            // 프로필 이미지 로드 (Glide 라이브러리 사용)
                                Glide.with(holder.images.getContext())
                                        .load(profilePicUrl)
                                        .circleCrop()
                                        .error(R.drawable.stock_profile)
                                        .into(holder.profile);
                        }
                    }
                    else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });

        //post의 uid로 부터 닉네임 가져오기
        db.collection("users").document(postUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document != null){
                            String nickname = document.getString("nickname");
                            holder.nickName.setText(nickname);
                        }
                        else{
                            Log.d(TAG, "failed to get nickname ", task.getException());
                        }
                    }
                    else{
                        Log.d(TAG, "failed to get nickname ", task.getException());
                    }
                });

        //게시물 이미지 로드
        LinearLayoutManager layoutManager = new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
        holder.images.setLayoutManager(layoutManager);
        ImageAdapter imageAdapter = new ImageAdapter(holder.itemView.getContext(), post.getImages());
        holder.images.setAdapter(imageAdapter);


        // 좋아요 상태 불러오기
        String uid = auth.getUid();
        List<String> likes = post.getLikes();
        if (likes != null && likes.contains(uid)) {
            // 좋아요를 이미 눌렀으면 꽉 찬 하트 이미지 로드
            holder.likeButton.setImageResource(R.drawable.ic_full_heart);
        } else {
            // 좋아요를 누르지 않았으면 빈 하트 이미지 로드
            holder.likeButton.setImageResource(R.drawable.ic_empty_heart);
        }
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void setPostList(List<Post> postList) {
        this.postList = postList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView nickName;
        ImageButton btnMap;
        TextView date;
        TextView place;
        TextView mainText;
        ImageButton commentButton;
        ImageButton likeButton;
        RecyclerView images;
        SnapHelper snapHelper; // SnapHelper를 ViewHolder에 추가


        public ViewHolder(View view) {
            super(view);
            profile = view.findViewById(R.id.img_profile);
            nickName = view.findViewById(R.id.tv_nick_name);
            btnMap = view.findViewById(R.id.btn_map);
            date = view.findViewById(R.id.tv_post_date);
            place = view.findViewById(R.id.tv_post_place);
            commentButton = view.findViewById(R.id.ic_reply);
            mainText = view.findViewById(R.id.tv_post_mainText);
            likeButton = view.findViewById(R.id.ic_empty_heart);
            images = view.findViewById(R.id.img_post_recyclerView);

            SnapHelper snapHelper = new PagerSnapHelper(); //SnapHelper를 생성하고 recyclerViewBottom에 붙임
            snapHelper.attachToRecyclerView(images);


            commentButton.setOnClickListener(v -> {
                Post post = postList.get(getAdapterPosition());
                CommentBottomSheetDialogFragment fragment =
                        CommentBottomSheetDialogFragment.newInstance(post.getPostId());
                fragment.show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), "commentDialog");
            });

            likeButton.setOnClickListener(v -> {
                Post post = postList.get(getAdapterPosition());
                String uid = auth.getUid();
                checkLikeStatus(post.getPostId(), uid, likeButton);
            });
        }
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
}
