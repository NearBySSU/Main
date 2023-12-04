package com.example.nearby.main.friends;

public class Friend {
    // 친구 리스트 객체
    private String profileUrl;
    private String friendName;
    private String newPost;
    private String postAdd;
    private String postCount;
    private String friendId; // 친구의 Firestore에서의 ID

    // 아직 버튼은 연결 못함

    public Friend(String profileUrl, String friendName, String newPost, String postAdd, String postCount, String userID){
        this.profileUrl = profileUrl;
        this.friendName = friendName;
        this.newPost = newPost;
        this.postAdd = postAdd;
        this.postCount = postCount;
        this.friendId = userID;
    }

    public String getProfileUrl() {return this.profileUrl;}
    public String getFriendName() {return this.friendName;}
    public String getNewPost(){return this.newPost;}
    public String getPostAdd(){return this.postAdd;}
    public String getPostCount(){return this.postCount;}
    public String getFriendId() {return this.friendId;}
}
