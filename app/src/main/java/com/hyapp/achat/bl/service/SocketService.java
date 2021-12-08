package com.hyapp.achat.bl.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.hyapp.achat.bl.socket.IOSocket;
import com.hyapp.achat.bl.socket.PeopleApi;
import com.hyapp.achat.bl.utils.NotifUtils;
import com.hyapp.achat.da.LoginPreferences;
import com.hyapp.achat.model.event.Event;
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
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LoginEvent loginEvent = intent.getParcelableExtra(EXTRA_LOGIN_EVENT);
        if (loginEvent != null) {
            ioSocket = new IOSocket(loginEvent, this::stopSelf);
            LoginPreferences.singleton(this).putLogged(true);
        }

        NotifUtils.createSocketChannel(this);
        startForeground(NotifUtils.ID_SOCKET, NotifUtils.getSocketNotif(this));
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        ioSocket.destroy();
        LoginPreferences.singleton(this).putLogged(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRequestPeople(Event event) {
        if (event.action == Event.ACTION_REQUEST_PEOPLE) {
            PeopleApi.singleton().requestPeople(ioSocket.getSocket());
        }
    }
}
