"use strict";

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

exports.sendLikeNotification = functions.region("asia-northeast3").firestore.document("/posts/{postid}").onWrite(async (change, context) => {
  const beforeLikes = change.before.data().likes;
  const afterLikes = change.after.data().likes;

  if (JSON.stringify(beforeLikes) !== JSON.stringify(afterLikes)) {
    if (afterLikes.length > beforeLikes.length) {
      const postOwnerUid = change.after.data().uid;
      const newLikeUserId = afterLikes.find((id) => !beforeLikes.includes(id));

      // 자신의 게시물일 경우 리턴
      if (newLikeUserId === postOwnerUid) {
        return;
      }

      // 좋아요를 누른 사용자의 닉네임을 가져옵니다.
      const newLikeUserSnapshot = await db.collection("users").doc(newLikeUserId).get();
      const newLikeUserNickName = newLikeUserSnapshot.data().nickname;

      // 게시물 작성자의 FCM 토큰을 가져옵니다.
      const userSnapshot = await db.collection("users").doc(postOwnerUid).get();
      const fcmToken = userSnapshot.data().fcmToken;

      const message = {
        notification: {
          title: "새로운 좋아요!",
          body: `${newLikeUserNickName}님이 회원님의 게시물을 좋아합니다.`,
        },
        token: fcmToken, // 실제 FCM 토큰으로 변경됩니다.
      };

      messaging.send(message)
          .then((response) => {
            console.log("Successfully sent message:", response);
          })
          .catch((error) => {
            console.log("Error sending message:", error);
          });
    }
  }
});

exports.sendCommentNotification = functions.region("asia-northeast3").firestore.document("/posts/{postid}/comments/{commentid}").onCreate(async (snapshot, context) => {
  const newCommentUserId = snapshot.data().commenterId;
  const postId = context.params.postid;

  // 게시글 작성자의 uid를 가져옵니다.
  const postSnapshot = await db.collection("posts").doc(postId).get();
  const postOwnerUid = postSnapshot.data().uid;

  // 댓글 작성자와 포스트 주인이 같으면 리턴
  if (newCommentUserId === postOwnerUid) {
    return;
  }

  // 댓글을 남긴 사용자의 닉네임을 가져옵니다.
  const newCommentUserSnapshot = await db.collection("users").doc(newCommentUserId).get();
  const newCommentUserNickName = newCommentUserSnapshot.data().nickname;

  // 게시물 작성자의 FCM 토큰을 가져옵니다.
  const userSnapshot = await db.collection("users").doc(postOwnerUid).get();
  const fcmToken = userSnapshot.data().fcmToken;

  const message = {
    notification: {
      title: "새로운 댓글!",
      body: `${newCommentUserNickName}님이 회원님의 게시물에 댓글을 남겼습니다.`,
    },
    token: fcmToken, // 실제 FCM 토큰으로 변경됩니다.
  };

  messaging.send(message)
      .then((response) => {
        console.log("Successfully sent message:", response);
      })
      .catch((error) => {
        console.log("Error sending message:", error);
      });
});

exports.sendFollowNotification = functions.region("asia-northeast3").firestore.document("/users/{userid}").onWrite(async (change, context) => {
  const beforeFollowings = change.before.data().followings;
  const afterFollowings = change.after.data().followings;

  if (JSON.stringify(beforeFollowings) !== JSON.stringify(afterFollowings)) {
    if (afterFollowings.length > beforeFollowings.length) {
      const newFollowerId = context.params.userid;
      const followedUserId = afterFollowings.find((id) => !beforeFollowings.includes(id));

      // 팔로우한 사용자의 닉네임을 가져옵니다.
      const newFollowerSnapshot = await db.collection("users").doc(newFollowerId).get();
      const newFollowerNickName = newFollowerSnapshot.data().nickname;

      // 팔로우 당한 사용자의 FCM 토큰을 가져옵니다.
      const userSnapshot = await db.collection("users").doc(followedUserId).get();
      const fcmToken = userSnapshot.data().fcmToken;

      const message = {
        notification: {
          title: "새로운 팔로워!",
          body: `${newFollowerNickName}님이 회원님을 팔로우하기 시작했습니다.`,
        },
        token: fcmToken, // 실제 FCM 토큰으로 변경됩니다.
      };

      messaging.send(message)
          .then((response) => {
            console.log("Successfully sent message:", response);
          })
          .catch((error) => {
            console.log("Error sending message:", error);
          });
    }
  }
});

