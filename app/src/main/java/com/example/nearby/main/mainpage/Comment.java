package com.example.nearby.main.mainpage;

public class Comment {
    private String commentText; // 댓글 내용
    private String commenterId; // 댓글 작성자 ID
    private long timestamp; // 댓글 작성 시간
    private String profilePicUrl; // 댓글 작성자의 프로필 사진 URL
    private String nickname;


    // 생성자
    public Comment(String commentText, String commenterId, String profilePicUrl, long timestamp, String nickname) {
        this.commentText = commentText;
        this.commenterId = commenterId;
        this.profilePicUrl = profilePicUrl;
        this.timestamp = timestamp;
        this.nickname = nickname;
    }

    // getter 메소드
    public String getCommentText() {
        return this.commentText;
    }

    public String getCommenterId() {
        return this.commenterId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getProfilePicUrl() {
        return this.profilePicUrl;
    }

    public String getNickname() {
        return nickname;
    }
}
