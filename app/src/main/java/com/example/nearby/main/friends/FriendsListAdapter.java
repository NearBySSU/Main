package com.example.nearby.main.friends;

import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder> {
    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClick(int position);
    }

    private OnDeleteButtonClickListener onDeleteButtonClickListener;
    private List<Friend> friendsList;
    private FirebaseFirestore db;
    FirebaseAuth auth;
    private boolean showDeleteButton;
    private String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


    public FriendsListAdapter(List<Friend> friendsList, boolean b) {
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

        if (showDeleteButton) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    db.collection("users")
                            .whereEqualTo("uid", friend.getFriendId())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().isEmpty()) {
                                            // 일치하는 이메일이(이 아니라 uid가) 없는 경우
                                            Log.d("LYB", "No matching uid found.");
                                        } else {
                                            // 일치하는 이메일이 있는 경우
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String findUid = document.getId();

                                                // 자신 삭제가 아니라면
                                                if (!findUid.equals(currentUid)) {
                                                    DocumentReference docRef = db.collection("users").document(currentUid);

                                                    // 이미 followings db에 존재하는지 검사
                                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot document = task.getResult();
                                                                if (document.exists()) {
                                                                    List<String> followings = (List<String>) document.get("followings");
                                                                    if (followings.contains(findUid)) {
                                                                        Log.d("LYB", "입력값이 배열에 존재합니다. 고로 삭제합니다.");
                                                                        onFollowingRemoved(findUid);
                                                                    } else {
                                                                        Log.d("LYB", "입력값이 배열에 존재하지 않아요.");
                                                                        Log.d("LYB", document.getId() + " => " + document.getData());
                                                                    }
                                                                } else {
                                                                    Log.d("LYB", "No such document");
                                                                }
                                                            } else {
                                                                Log.d("LYB", "get failed with ", task.getException());
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    } else {
                                        Log.d("ODG", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }
            });
        } else {
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

        public FriendsViewHolder(View itemView) {
            super(itemView);
            profileUrl = itemView.findViewById(R.id.iv_friend_profile);
            friendName = itemView.findViewById(R.id.tv_friend_name);
            newPost = itemView.findViewById(R.id.tv_friend_new_post);
            postAdd = itemView.findViewById(R.id.tv_friend_new_post_add);
            postCount = itemView.findViewById(R.id.tv_new_post_count);
            btnDelete = itemView.findViewById(R.id.unFollowBtn);
        }

    }

    private void onFollowingRemoved(String inputUid) {
        // 검색된 이메일의 사용자 id를 following DB에서 삭제
        DocumentReference userRef = db.collection("users").document(currentUid);
        userRef.update("followings", FieldValue.arrayRemove(inputUid))
                .addOnSuccessListener(aVoid -> Log.d("LYB", "InputId removed to user followings document"))
                .addOnFailureListener(e -> Log.w("LYB", "Error removing userID to user followings document", e));
        Log.d("LYB", "Following removed with ID: " + inputUid);
    }

}
