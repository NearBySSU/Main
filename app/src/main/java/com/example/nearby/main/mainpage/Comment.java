package com.example.nearby.main.mainpage;

public class Comment {
    private String commentText;
    private String commenterId;

    // 생성자
    public Comment(String commentText, String commenterId) {
        this.commentText = commentText;
        this.commenterId = commenterId;
    }

    // getter 메소드
    public String getCommentText() {
        return this.commentText;
    }

    public String getCommenterId() {
        return this.commenterId;
    }

    // 필요에 따라 setter 메소드도 추가할 수 있습니다.
}
