package com.example.nearby;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostItem postItem = postItemList.get(position);
        holder.title.setText(postItem.getTitle());
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.post_text);
        }
    }

}
