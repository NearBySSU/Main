package com.example.nearby.main.friends;

import static androidx.fragment.app.FragmentManager.TAG;

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
import com.example.nearby.main.friends.FollowRequester;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FollowRequesterAdapter extends RecyclerView.Adapter<FollowRequesterAdapter.ViewHolder> {

    private ArrayList<FollowRequester> followRequesterList;

    public FollowRequesterAdapter(ArrayList<FollowRequester> followRequesterList) {
        this.followRequesterList = followRequesterList;
    }

    public void updateData(ArrayList<FollowRequester> newData) {
        this.followRequesterList.clear();
        this.followRequesterList.addAll(newData);
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FollowRequester followRequester = followRequesterList.get(position);
        holder.nicknameTextView.setText(followRequester.getNickname());
        holder.emailTextView.setText(followRequester.getEmail());

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // 현재 유저의 'followings' 목록에 요청자의 아이디를 추가합니다.
                db.collection("users").document(currentUid)
                        .update("followings", FieldValue.arrayUnion(followRequester.getUid()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                // 요청자의 'followings' 목록에 현재 유저의 아이디를 추가하고,
                                // 현재 유저의 'FollowRequester'에서 요청자의 아이디를 삭제합니다.
                                db.collection("users").document(followRequester.getUid())
                                        .update("followings", FieldValue.arrayUnion(currentUid))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                db.collection("users").document(currentUid)
                                                        .update("FollowRequester", FieldValue.arrayRemove(followRequester.getUid()))
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(v.getContext(), "팔로우를 수락했어요",Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
            }
        });


        holder.btn_delete_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // 현재 유저의 'FollowRequester'에서 해당 아이디를 삭제합니다.
                db.collection("users").document(currentUid)
                        .update("FollowRequester", FieldValue.arrayRemove(followRequester.getUid()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(v.getContext(), "팔로우를 거절했어요",Toast.LENGTH_SHORT);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return followRequesterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameTextView;
        TextView emailTextView;
        ImageView profilePicImageView;
        Button btn_follow;
        Button btn_delete_request;

        public ViewHolder(View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.tv_friend_name);
            emailTextView = itemView.findViewById(R.id.tv_friend_email);
            profilePicImageView = itemView.findViewById(R.id.iv_friend_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
            btn_delete_request =itemView.findViewById(R.id.btn_delete_request);
        }

    }
}

