package com.hyapp.achat.viewmodel.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.RemoteInput;

import com.hyapp.achat.model.ChatRepo;
import com.hyapp.achat.model.entity.Contact;
import com.hyapp.achat.model.entity.Message;
import com.hyapp.achat.model.entity.User;
import com.hyapp.achat.model.entity.UserLive;
import com.hyapp.achat.model.objectbox.UserDao;
import com.hyapp.achat.viewmodel.service.SocketService;
import com.hyapp.achat.viewmodel.Notifs;
import com.hyapp.achat.model.event.ActionEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;

public class NotifReceiver extends BroadcastReceiver {

    public static final String EXTRA_CONTACT = "contact";
    public static final String EXTRA_MESSAGE = "message";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case Notifs.ACTION_EXIT:
                    handleExitApp(context);
                    break;
                case Notifs.ACTION_REPLY_MESSAGE:
                    handleReplyMessage(intent);
                    break;
                case Notifs.ACTION_MARK_MESSAGE_AS_READ:
                    handleMarkMessageAsRead(intent);
                    break;
            }
        }
    }

    private void handleExitApp(Context context) {
        context.stopService(new Intent(context, SocketService.class));
        EventBus.getDefault().post(new ActionEvent(ActionEvent.ACTION_EXIT_APP));
    }

    private void handleReplyMessage(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            CharSequence text = remoteInput.getCharSequence(Notifs.KEY_REPLY_MESSAGE).toString();
            if (!TextUtils.isEmpty(text)) {
                Message message = intent.getParcelableExtra(EXTRA_MESSAGE);
                Contact contact = intent.getParcelableExtra(EXTRA_CONTACT);
                if (message != null && contact != null) {
                    User currentUser = UserLive.INSTANCE.getValue();
                    if (currentUser == null) {
                        currentUser = UserDao.get(User.CURRENT_USER_ID);
                    }
                    Message newMessage = new Message(
                            UUID.randomUUID().toString(), Message.TYPE_TEXT,
                            Message.TRANSFER_SEND, System.currentTimeMillis(), text.toString(), 0, "",
                            contact.getUid(), currentUser != null ? currentUser : new User()
                    );
                    ChatRepo.INSTANCE.sendPvMessage(newMessage, contact.getUser());
                    ChatRepo.INSTANCE.markMessageAsRead(message);
                }
            }
        }
    }

    private void handleMarkMessageAsRead(Intent intent) {
        Message message = intent.getParcelableExtra(EXTRA_MESSAGE);
        if (message != null) {
            ChatRepo.INSTANCE.markMessageAsRead(message);
        }
    }
}
