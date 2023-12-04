package com.example.nearby.main.friends;

public class Friend {
    // 친구 리스트 객체
    private String profileUrl;
    private String friendName;
//    private String postCount;
    private String friendId; // 친구의 Firestore에서의 ID

    public Friend(String profileUrl, String friendName, String userID) {
        this.profileUrl = profileUrl;
        this.friendName = friendName;
//        this.postCount = postCount;
        this.friendId = userID;

    }

    public String getProfileUrl() {
        return this.profileUrl;
    }

    public String getFriendName() {
        return this.friendName;
    }

//    public String getPostCount() {
//        return this.postCount;
//    }

    public String getFriendId() {
        return this.friendId;
    }
}
