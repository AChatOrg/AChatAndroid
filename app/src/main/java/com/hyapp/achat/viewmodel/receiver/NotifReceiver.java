package com.hyapp.achat.viewmodel.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hyapp.achat.viewmodel.service.SocketService;
import com.hyapp.achat.viewmodel.utils.NotifUtils;
import com.hyapp.achat.model.event.ActionEvent;

import org.greenrobot.eventbus.EventBus;

public class NotifReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(NotifUtils.ACTION_EXIT)) {
                context.stopService(new Intent(context, SocketService.class));
                EventBus.getDefault().post(new ActionEvent(ActionEvent.ACTION_EXIT_APP));
            }
        }
    }
}
