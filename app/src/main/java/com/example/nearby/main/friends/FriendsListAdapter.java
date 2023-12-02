package com.example.nearby.main.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder> {
    // 객체를 연결할 어댑터
    private ArrayList<String> emails;
    private FirebaseFirestore db;
    FirebaseAuth auth;

    public FriendsListAdapter(ArrayList<String> emails) {
        this.emails = emails;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notifyDataSetChanged();
    }

    public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new FriendsViewHolder(view);
    }

    public void onBindViewHolder(FriendsViewHolder holder, int position) {
        String email = emails.get(position);
        holder.emailTextView.setText(email);
    }

    public int getItemCount() {
        return emails.size();
    }

    public void setFriendsList(ArrayList<String> emails) {
        this.emails = emails;
        notifyDataSetChanged();
    }

    static class FriendsViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            emailTextView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

}
