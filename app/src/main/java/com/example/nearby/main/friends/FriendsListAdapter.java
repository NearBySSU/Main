package com.example.nearby.main.friends;

import android.app.AlertDialog;
import android.content.Context;
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
import com.google.android.gms.tasks.OnCompleteListener;
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

    private Context context;  // Context 변수 추가

    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClick(int position);
    }

    // 아이템 클릭을 처리하는 리스너
    public interface OnItemClickListener {
        void onItemClick(Friend friend);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private OnDeleteButtonClickListener onDeleteButtonClickListener;
    private List<Friend> friendsList;
    private FirebaseFirestore db;
    FirebaseAuth auth;
    public boolean showDeleteButton;
    private String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


    public FriendsListAdapter(List<Friend> friendsList, boolean b, Context context) {
        this.context = context;
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
//        holder.postCount.setText(friend.getPostCount());

        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(friend);
                }
            }
        });

        if (showDeleteButton) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View dialogView = inflater.inflate(R.layout.follower_delete, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(dialogView);

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
                    Button btnConfirm = dialogView.findViewById(R.id.btn_dialog_confirm);

                    // '아니오' 버튼 클릭 시 다이얼로그 닫기
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    // '예' 버튼 클릭 시 취소 처리 진행
                    btnConfirm.setOnClickListener(new View.OnClickListener() {
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
                                                    // 일치하는 uid가 없는 경우
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
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                            dialog.dismiss();
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