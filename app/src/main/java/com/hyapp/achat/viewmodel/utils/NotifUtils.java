package com.hyapp.achat.viewmodel.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.hyapp.achat.R;
import com.hyapp.achat.viewmodel.receiver.NotifReceiver;
import com.hyapp.achat.view.MainActivity;

public class NotifUtils {

    public static final String CHANNEL_SOCKET = "socket";

    public static final int ID_SOCKET = 1;

    public static final String ACTION_EXIT = "ACTION_EXIT";


    public static NotificationManager notificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void createSocketChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.start_chatting);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel;
            channel = new NotificationChannel(NotifUtils.CHANNEL_SOCKET, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            channel.setVibrationPattern(new long[]{0});
            notificationManager(context).createNotificationChannel(channel);
        }
    }

    public static Notification getSocketNotif(Context context) {
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent enterIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), flags);

        Intent intent = new Intent(context, NotifReceiver.class);

        intent.setAction(ACTION_EXIT);
        PendingIntent exitIntent = PendingIntent.getBroadcast(context, 0, intent, flags);

        return new NotificationCompat.Builder(context, NotifUtils.CHANNEL_SOCKET)
                .setContentTitle(context.getString(R.string.you_are_connected))
                .setContentText(context.getString(R.string.tap_to_enter))
                .setSmallIcon(R.drawable.action_add_chat)
                .setContentIntent(enterIntent)
                .addAction(R.drawable.action_exit, context.getString(R.string.exit), exitIntent)
                .build();
    }
}
