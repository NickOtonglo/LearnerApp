package pesh.mori.learnerapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Nick Otto on 10/12/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingServ";
    private static final int BROADCAST_NOTIFICATION_ID = 1;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String refreshToken = s;
        Log.d("NEW_TOKEN",refreshToken);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("onMessageReceived: ",remoteMessage.toString());
        String notificationTitle = "";
        String notificationBody = "";
        String notificationData = "";

        try{
            notificationData = remoteMessage.getData().toString();
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        }catch (NullPointerException e){
            Log.e(TAG,"onMessageReceived: NullPointerException: "+e.getMessage());
        }
        Log.d(TAG, "onMessageReceived: data: "+notificationData);
        Log.d(TAG, "onMessageReceived: data_type: "+remoteMessage.getData().get("data_type"));
//        Log.d(TAG, "onMessageReceived: notification title: "+notificationTitle);
//        Log.d(TAG, "onMessageReceived: notification body: "+notificationBody);

        try{
            String dataType = remoteMessage.getData().get("data_type");
            if (dataType.equals("direct_message")){
                Log.d(TAG,"onMessageReceived: new incoming message.");
                String title = remoteMessage.getData().get("title");
                String message = remoteMessage.getData().get("message");
                String message_id =  remoteMessage.getData().get("message_id");
                Log.d(TAG,"title: "+title);
                Log.d(TAG,"message: "+message);
                Log.d(TAG,"message_id: "+message_id);

                sendMessageNotification(title,message,message_id);
            } else {
                Log.d(TAG,"onMessageReceived: new incoming message.");
                String title = remoteMessage.getData().get("title");
                String message = remoteMessage.getData().get("message");
                String message_id =  remoteMessage.getData().get("message_id");
                Log.d(TAG,"title: "+title);
                Log.d(TAG,"message: "+message);
                Log.d(TAG,"message_id: "+message_id);

                sendMessageNotification(title,message,message_id);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendMessageNotification(String title,String message, String messageId){
        Log.d(TAG,"sendMessageNotification: building a message notification");

        int notificationId = buildNotificationId(messageId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        Intent pendingIntent = new Intent(this, MessagesActivity.class);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setSmallIcon(R.mipmap.ic_push_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_push_notification))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText("Tap to view")
                .setDefaults(Notification.DEFAULT_ALL)
                .setColor(getColor(R.color.colorAccent))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_HIGH);

        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(notificationId, builder.build());
    }

    private int buildNotificationId(String id) {
        Log.d(TAG, "buildNotificationId: building a notification id.");

        int notificationId = 0;
        for(int i = 0; i < 9; i++){
            notificationId = notificationId + id.charAt(0);
        }
        Log.d(TAG, "buildNotificationId: id: " + id);
        Log.d(TAG, "buildNotificationId: notification id:" + notificationId);
        return notificationId;
    }
}
