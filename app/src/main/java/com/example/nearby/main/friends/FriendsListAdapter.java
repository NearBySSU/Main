package com.example.nearby.main.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nearby.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder> {
    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClick(int position);
    }

    private OnDeleteButtonClickListener onDeleteButtonClickListener;    private List<Friend> friendsList;
    private FirebaseFirestore db;
    FirebaseAuth auth;
    private boolean showDeleteButton;

    public FriendsListAdapter(List<Friend> friendsList, boolean b){
        if (friendsList == null) {
            this.friendsList = new ArrayList<>();
        } else {
            this.friendsList = friendsList;
        }
        this.showDeleteButton = b;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notifyDataSetChanged();
    }

    public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friends_list, parent, false);
        return new FriendsViewHolder(view);
    }

    public void onBindViewHolder(FriendsViewHolder holder, int position) {
        Friend friend = friendsList.get(position);
        Glide.with(holder.profileUrl.getContext())
                .load(friend.getProfileUrl())
                .circleCrop()
                .into(holder.profileUrl);
        holder.friendName.setText(friend.getFriendName());
        holder.newPost.setText(friend.getNewPost());
        holder.postAdd.setText(friend.getPostAdd());
        holder.postCount.setText(friend.getPostCount());

        if(showDeleteButton){
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDeleteButtonClickListener != null) {
                        onDeleteButtonClickListener.onDeleteButtonClick(position);
                    }
                }
            });
        } else{
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    public void setOnDeleteButtonClickListener(OnDeleteButtonClickListener listener) {
        this.onDeleteButtonClickListener = listener;
    }

    public int getItemCount() {
        return friendsList.size();
    }

    static class FriendsViewHolder extends RecyclerView.ViewHolder {
        ImageView profileUrl;
        TextView friendName;
        TextView newPost;
        TextView postAdd;
        TextView postCount;
        Button btnDelete;

        public FriendsViewHolder(View itemView){
            super(itemView);
            profileUrl = itemView.findViewById(R.id.iv_friend_profile);
            friendName = itemView.findViewById(R.id.tv_friend_name);
            newPost = itemView.findViewById(R.id.tv_friend_new_post);
            postAdd = itemView.findViewById(R.id.tv_friend_new_post_add);
            postCount = itemView.findViewById(R.id.tv_new_post_count);
            btnDelete = itemView.findViewById(R.id.unFollowBtn);
        }

    }

}
