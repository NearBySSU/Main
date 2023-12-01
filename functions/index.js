"use strict";

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

exports.sendLikeNotification = functions.firestore.document("/posts/{postid}").onWrite(async (change, context) => {
  const beforeLikes = change.before.data().likes;
  const afterLikes = change.after.data().likes;

  if (JSON.stringify(beforeLikes) !== JSON.stringify(afterLikes)) {
    if (afterLikes.length > beforeLikes.length) {
      const postOwnerUid = change.after.data().uid;
      const newLikeUserId = afterLikes.find((id) => !beforeLikes.includes(id));

      // 사용자의 FCM 토큰을 가져옵니다.
      const userSnapshot = await db.collection("users").doc(postOwnerUid).get();
      const fcmToken = userSnapshot.data().fcmToken;

      const message = {
        notification: {
          title: "새로운 좋아요!",
          body: `${newLikeUserId}님이 회원님의 게시물을 좋아합니다.`,
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
