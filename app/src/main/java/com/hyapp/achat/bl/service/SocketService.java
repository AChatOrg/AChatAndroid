package com.hyapp.achat.bl.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.hyapp.achat.Config;
import com.hyapp.achat.bl.socket.IOSocket;
import com.hyapp.achat.bl.utils.NotifUtils;
import com.hyapp.achat.model.event.Event;
import com.hyapp.achat.model.event.LoggedEvent;
import com.hyapp.achat.model.event.LoginEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SocketService extends Service {

    public static final String EXTRA_LOGIN_EVENT = "LoginEvent";

    private IOSocket ioSocket;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        EventBus.getDefault().register(this);
        ioSocket = new IOSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LoginEvent loginEvent = intent.getParcelableExtra(EXTRA_LOGIN_EVENT);
        if (loginEvent != null) {
            ioSocket.connect(loginEvent);
            ioSocket.listen();
        }

        NotifUtils.createSocketChannel(this);
        startForeground(NotifUtils.ID_SOCKET, NotifUtils.getSocketNotif(this));
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
//        EventBus.getDefault().unregister(this);
        ioSocket.disconnect();
        super.onDestroy();
    }
}
