const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendLikeNotification = functions.firestore
    .document("/posts/{postid}")
    .onUpdate(async (change, context) => {
      const beforeLikes = change.before.data().likes;
      const afterLikes = change.after.data().likes;

      if (JSON.stringify(beforeLikes) !== JSON.stringify(afterLikes)) {
        // 좋아요가 추가되었을 경우
        if (afterLikes.length > beforeLikes.length) {
          const postOwnerUid = change.after.data().uid;
          const newLikeUserId = afterLikes.find((id) => !beforeLikes.includes(id));

          // 알림 메시지 생성
          const message = {
            notification: {
              title: "새로운 좋아요!",
              body: `${newLikeUserId}님이 회원님의 게시물을 좋아합니다.`,
            },
            token: postOwnerUid, // 이 부분은 실제로 알림을 받을 사용자의 FCM 토큰으로 변경해야 합니다.
          };

          // 알림 보내기
          admin.messaging().send(message)
              .then((response) => {
                console.log("Successfully sent message:", response);
              })
              .catch((error) => {
                console.log("Error sending message:", error);
              });
        }
      }
    });
