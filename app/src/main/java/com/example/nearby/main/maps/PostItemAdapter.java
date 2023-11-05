package com.example.nearby.main.maps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nearby.R;

import java.util.ArrayList;
import java.util.List;

public class PostItemAdapter extends RecyclerView.Adapter<PostItemAdapter.ViewHolder> {
    private List<PostItem> postItemList;

    public PostItemAdapter() {
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
        holder.date.setText(postItem.getDate());

        // 프로필 사진을 가져오지 못한 경우에는 기본 프로필 사진을 보여줍니다.
        Glide.with(holder.profilePic.getContext())
                .load(postItem.getProfilePicUrl())
                .error(R.drawable.stock_profile)
                .into(holder.profilePic);
    }


    @Override
    public int getItemCount() {
        return postItemList.size();
    }

    public void addItem(PostItem postItem) {
        postItemList.add(postItem);
        notifyDataSetChanged();
    }

    public void clearItems() {
        postItemList.clear();
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView date;
        private ImageView profilePic; // 추가: 프로필 사진을 표시할 ImageView

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.post_text);
            date = itemView.findViewById(R.id.post_date);
            profilePic = itemView.findViewById(R.id.profile_pic); // 추가: 프로필 사진을 표시할 ImageView
        }
    }


}
