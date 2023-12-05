package com.example.nearby;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.nearby.main.MainPageActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    //메시지를 받았을 때의 함수
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FCM", "onMessageReceived: " + remoteMessage);

        //인앱에서 메시지를 띄우기 위한 처리
        if (remoteMessage.getNotification() != null) {
            Intent intent = new Intent(this, MainPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                    .setSmallIcon(R.drawable.alarm_icon)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle())
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(101, builder.build());
        }
    }


    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("FCM", "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Firestore 인스턴스 가져오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 현재 사용자의 UID 가져오기
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 사용자가 로그인한 상태일 때만 토큰을 업데이트합니다.
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // 사용자의 FCM 토큰을 업데이트하는 문서 참조 생성
            DocumentReference userRef = db.collection("users").document(uid);

            // FCM 토큰 업데이트
            userRef.update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d("FCM", "FCM Token updated for user: " + uid))
                    .addOnFailureListener(e -> Log.w("FCM", "Error updating FCM Token for user: " + uid, e));
        }
    }
}
