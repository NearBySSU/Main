package com.example.nearby.main.mainpage;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nearby.R;

import java.util.List;



public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = postList.get(position);
        // 날짜 설정
        holder.date.setText(post.getDate());
        // 메모 설정
        holder.postMemo.setText(post.getText());

        // 프로필 이미지 로드 (Glide 라이브러리 사용)
        List<String> images = post.getImages();
        if (images != null && !images.isEmpty()) {
            String imageUrl = images.get(0); // 첫 번째 이미지 URL
            Glide.with(holder.profile.getContext()).load(imageUrl).into(holder.profile);
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
        TextView postName;
        ImageButton btnMap;
        TextView date;
        TextView place;
        ImageView heart;
        ImageView reply;
        TextView postMemo;
        ImageButton commentButton;



        public ViewHolder(View view) {
            super(view);
            profile = view.findViewById(R.id.img_profile);
            postName = view.findViewById(R.id.tv_post_name);
            btnMap = view.findViewById(R.id.btn_map);
            date = view.findViewById(R.id.tv_post_date);
            place = view.findViewById(R.id.tv_post_place);
            heart = view.findViewById(R.id.ic_empty_heart);
            commentButton = view.findViewById(R.id.ic_reply);
            postMemo = view.findViewById(R.id.tv_post_memo);

            commentButton.setOnClickListener(v -> {
                Post post = postList.get(getAdapterPosition());
                CommentBottomSheetDialogFragment fragment =
                        CommentBottomSheetDialogFragment.newInstance(post.getId());
                fragment.show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), "commentDialog");
            });
        }
    }
}

