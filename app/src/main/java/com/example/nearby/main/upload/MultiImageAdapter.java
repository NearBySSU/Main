package com.example.nearby.main.upload;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nearby.R;

import java.util.ArrayList;

public class MultiImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Uri> mData = null;
    private Activity mActivity = null;

    private static final int VIEW_TYPE_IMAGE = 0;
    private static final int VIEW_TYPE_BUTTON = 1;
    private static final int PICK_IMAGE_REQUEST = 2222;

    public MultiImageAdapter(ArrayList<Uri> list, Activity activity) {
        mData = list;
        mActivity = activity;
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        ImageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }

    public class ButtonViewHolder extends RecyclerView.ViewHolder {
        ImageButton myButton;

        ButtonViewHolder(View itemView) {
            super(itemView);
            myButton = itemView.findViewById(R.id.myButton);
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    mActivity.startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (mData.get(position) == null) ? VIEW_TYPE_BUTTON : VIEW_TYPE_IMAGE;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.multi_image_item, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_layout, parent, false);
            return new ButtonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            Uri image_uri = mData.get(position);
            if (image_uri != null) { // null 체크를 추가했습니다.
                Glide.with(mActivity)
                        .load(image_uri)
                        .into(((ImageViewHolder) holder).image);
            }
        } else if (holder instanceof ButtonViewHolder) {
            // 이미지 추가 버튼에 대한 동작을 설정합니다.
        }
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

}
