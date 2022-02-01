package com.hyapp.achat.viewmodel;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.graphics.drawable.IconCompat;

import com.hyapp.achat.R;
import com.hyapp.achat.model.Preferences;
import com.hyapp.achat.model.entity.Contact;
import com.hyapp.achat.model.entity.Message;
import com.hyapp.achat.model.entity.UserLive;
import com.hyapp.achat.view.ChatActivity;
import com.hyapp.achat.view.MainActivity;
import com.hyapp.achat.viewmodel.receiver.NotifReceiver;

import java.io.IOException;
import java.net.URL;

public class Notifs {

    public static final String CHANNEL_SOCKET = "socket";
    public static final String CHANNEL_MESSAGING = "messaging";

    public static final int ID_SOCKET = Integer.MAX_VALUE;

    public static final String ACTION_EXIT = "ACTION_EXIT";
    public static final String ACTION_REPLY_MESSAGE = "ACTION_REPLY_MESSAGE";
    public static final String ACTION_MARK_MESSAGE_AS_READ = "ACTION_MARK_MESSAGE_AS_READ";

    public static final String KEY_REPLY_MESSAGE = "replyMsg";


    public static NotificationManager notificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void remove(Context context, int notifId) {
        notificationManager(context).cancel(notifId);
    }

    public static void createSocketChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.start_chatting);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel;
            channel = new NotificationChannel(Notifs.CHANNEL_SOCKET, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            channel.setVibrationPattern(new long[]{0});
            notificationManager(context).createNotificationChannel(channel);
        }
    }

    public static void createMessagingChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel;
            channel = new NotificationChannel(Notifs.CHANNEL_MESSAGING, "messaging", importance);
            channel.setDescription("messaging");
            channel.enableVibration(true);
            channel.enableLights(true);
            notificationManager(context).createNotificationChannel(channel);
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public static Notification getSocketNotif(Context context) {
        PendingIntent enterIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context, Notifs.CHANNEL_SOCKET)
                .setContentTitle(context.getString(R.string.you_are_connected))
                .setContentText(context.getString(R.string.tap_to_enter))
                .setSmallIcon(R.drawable.action_add_chat)
                .setContentIntent(enterIntent)
                .build();
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public static void notifyMessage(Context context, Message message, Contact contact) {
        if (Preferences.instance().isCurrUserNotifEnabled(UserLive.INSTANCE.getValue() != null ? UserLive.INSTANCE.getValue().getUid() : "")) {
            if (Preferences.instance().isUserNotifEnabled(UserLive.INSTANCE.getValue() != null ? UserLive.INSTANCE.getValue().getUid() : "", contact.getUid())) {
                Intent receiverIntent = new Intent(context, ChatActivity.class);
                receiverIntent.putExtra(ChatActivity.EXTRA_CONTACT, contact);
                receiverIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));
                PendingIntent enterIntent = PendingIntent.getActivity(context, 0, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Notifs.CHANNEL_MESSAGING)
                        .setSmallIcon(R.drawable.action_add_chat)
                        .setContentIntent(enterIntent)
                        .setAutoCancel(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setCategory(Notification.CATEGORY_MESSAGE);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Person.Builder personBuilder = new Person.Builder().setName(contact.getName());
                    if (!contact.getAvatars().isEmpty()) {
                        personBuilder.setIcon(IconCompat.createWithContentUri(contact.getAvatars().get(0)));
                    }
                    Person person = personBuilder.build();
                    NotificationCompat.MessagingStyle.Message styleMessage =
                            new NotificationCompat.MessagingStyle.Message(message.getText(), message.getTime(), person);
                    NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle(person)
                            .addMessage(styleMessage)
                            .addMessage(styleMessage)
                            .addMessage(styleMessage)
                            .addHistoricMessage(styleMessage)
                            .setGroupConversation(false);

                    builder.setStyle(style);
                } else {
                    builder.setWhen(message.getTime());
                    builder.setContentTitle(contact.getName());
                    builder.setContentText(message.getText());
                    if (!contact.getAvatars().isEmpty()) {
                        try {
                            URL url = new URL(contact.getAvatars().get(0));
                            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            builder.setLargeIcon(image);
                        } catch (IOException ignored) {
                        }
                    }
                }

                CharSequence replyLabel = context.getString(R.string.reply);

                RemoteInput remoteInput = new RemoteInput.Builder(KEY_REPLY_MESSAGE)
                        .setLabel(replyLabel)
                        .build();

                receiverIntent = new Intent(context, NotifReceiver.class);
                receiverIntent.setAction(ACTION_REPLY_MESSAGE);
                receiverIntent.putExtra(NotifReceiver.EXTRA_CONTACT, contact);
                receiverIntent.putExtra(NotifReceiver.EXTRA_MESSAGE, message);
                receiverIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));

                PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context,
                        (int) contact.getId(),
                        receiverIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Action replyAction = new NotificationCompat.Action
                        .Builder(R.drawable.action_reply, replyLabel, replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                        .setShowsUserInterface(false)
                        .build();

                receiverIntent = new Intent(context, NotifReceiver.class);
                receiverIntent.setAction(ACTION_MARK_MESSAGE_AS_READ);
                receiverIntent.putExtra(NotifReceiver.EXTRA_MESSAGE, message);
                receiverIntent.setData((Uri.parse("custom://" + System.currentTimeMillis())));

                PendingIntent markMessageAsReadPendingIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Action markAsReadAction = new NotificationCompat.Action
                        .Builder(R.drawable.msg_read_contact, context.getString(R.string.mark_as_read), markMessageAsReadPendingIntent)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
                        .setShowsUserInterface(false)
                        .build();

                NotificationCompat.Action wearableReplyAction = new NotificationCompat.Action.Builder(R.drawable.action_reply, replyLabel, replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

                NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
                extender.addAction(wearableReplyAction);
                extender.addAction(markAsReadAction);

//        builder.addAction(replyAction);
//        builder.addAction(markAsReadAction);
//        builder.extend(extender);

                notificationManager(context).notify((int) contact.getId(), builder.build());
            }
        }
    }
}
