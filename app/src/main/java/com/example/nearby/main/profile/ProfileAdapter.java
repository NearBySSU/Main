package com.example.nearby.main.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.nearby.R;
import com.example.nearby.main.SinglePostPageActivity;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    private ArrayList<ProfileItem> profileItemList;
    private ArrayList<String> imageUrlList;
    private Context context;

    public ProfileAdapter(Context context, List<ProfileItem> profileItemList) {
        this.context = context;
//        this.imageUrlList = imageUrlList != null ? imageUrlList : new ArrayList<>();
        this.profileItemList = new ArrayList<>();
    }


    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image2, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        ProfileItem profileItem = profileItemList.get(position);
        holder.date = profileItem.getDate();
        holder.postId = profileItem.getPostId();

//        String imageUrl = imageUrlList.get(position);

//        Glide.with(context)
//                .load(imageUrl)
//                .override(500, 500)  // 이미지 크기 조정. 필요에 따라 숫자를 변경하세요.
//                .centerCrop()  // 이미지를 가운데에서 정사각형으로 잘라냄
//                .into(holder.imageView);
        Glide.with(holder.imageView.getContext())
                .load(profileItem.getImgUrl())
                .override(500, 500)  // 이미지 크기 조정. 필요에 따라 숫자를 변경하세요.
                .centerCrop()  // 이미지를 가운데에서 정사각형으로 잘라냄
                .into(holder.imageView);

        // ViewTreeObserver를 사용하여 뷰의 가로 길이를 측정하고, 이 길이를 뷰의 높이로 설정
        holder.imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                int height = holder.imageView.getMeasuredWidth();  // 가로 길이 측정
                holder.imageView.getLayoutParams().height = height;  // 측정한 길이를 높이로 설정
                holder.imageView.requestLayout();  // 레이아웃 업데이트 요청
                return true;
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SinglePostPageActivity.class);
                intent.putExtra("postId", holder.postId);
                context.startActivity(intent);
//                Log.d("singlepage", holder.postId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profileItemList.size();
    }

//    public void addItem(ProfileItem profileItem) {
//        profileItemList.add(profileItem);
//        notifyDataSetChanged();
//    }

//    public void clearItems() {
//        profileItemList.clear();
//        notifyDataSetChanged();
//    }

    public void setProfileItemList(ArrayList<ProfileItem> profileItemList) {
        this.profileItemList = profileItemList;
        notifyDataSetChanged();
    }

    public class ProfileViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private String date;
        private String postId;

        public ProfileViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.image_view);
        }
    }
}
