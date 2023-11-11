package com.example.nearby.main.mainpage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nearby.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList; // 댓글 리스트

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.tvCommentText.setText(comment.getCommentText());
        holder.tvTimestamp.setText(getFormattedTimestamp(comment.getTimestamp())); // 시간 정보 설정
        Glide.with(holder.ivProfilePic.getContext())
                .load(comment.getProfilePicUrl())
                .into(holder.ivProfilePic); // 프로필 사진 설정
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentText; // 댓글 내용 텍스트뷰
        TextView tvTimestamp; // 댓글 작성 시간 텍스트뷰
        ImageView ivProfilePic; // 댓글 작성자의 프로필 사진 이미지뷰


        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp); // 뷰 바인딩
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic); // 뷰 바인딩
        }
    }

    // 시간 정보를 보기 좋게 변환하는 메소드
    private String getFormattedTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        return formatter.format(date);
    }
}


