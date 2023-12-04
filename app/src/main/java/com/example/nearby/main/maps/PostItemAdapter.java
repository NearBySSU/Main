package com.example.nearby.main.maps;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nearby.R;
import com.example.nearby.main.FriendProfileActivity;
import com.example.nearby.main.SinglePostPageActivity;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostItemAdapter extends RecyclerView.Adapter<PostItemAdapter.ViewHolder> {

    private Context context;

    //포스트가 저장된 리스트
    private List<PostItem> postItemList;

    //생성자,postItemList를 초기화 해줌
    public PostItemAdapter(Context context) {
        this.context = context;
        this.postItemList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_item, parent, false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostItem postItem = postItemList.get(position);
        holder.title.setText(postItem.getTitle());
        Timestamp timestamp = postItem.getDate();
        if (timestamp != null) {
            Date date = timestamp.toDate(); // Date 형식으로 변환
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA); // 원하는 형식으로 날짜를 포맷
            String dateString = sdf.format(date);
            holder.date.setText(dateString);
        }

        holder.postId = postItem.getPostId();
        holder.uid = postItem.getUid();


        holder.seeItBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SinglePostPageActivity.class);
                intent.putExtra("postId", holder.postId);
                context.startActivity(intent);
//                Log.d("singlepage", holder.postId);
            }
        });

        // 프로필 사진을 가져오지 못한 경우에 기본 프로필 사진을 보여주기
        Glide.with(holder.profilePic.getContext())
                .load(postItem.getProfilePicUrl())
                .circleCrop()
                .error(R.drawable.stock_profile)
                .into(holder.profilePic);

        holder.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FriendProfileActivity.class);
                intent.putExtra("inputUid", holder.uid);
                context.startActivity(intent);
//                Log.d("singlepage", holder.postId);
            }
        });


    }

    //포스트의 개수 리턴
    @Override
    public int getItemCount() {
        return postItemList.size();
    }

    public void addItem(PostItem postItem) {
        postItemList.add(postItem);
        notifyDataSetChanged();
    }

    //포스트 리스트 초기화
    public void clearItems() {
        postItemList.clear();
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView date;
        private ImageView profilePic;
        private String postId;
        private String uid;
        private Button seeItBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.post_text);
            date = itemView.findViewById(R.id.post_date);
            profilePic = itemView.findViewById(R.id.profile_pic);
            seeItBtn = itemView.findViewById(R.id.see_it_btn);
        }
    }
}
