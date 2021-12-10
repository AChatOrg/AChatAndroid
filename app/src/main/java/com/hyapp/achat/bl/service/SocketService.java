package com.hyapp.achat.bl.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.hyapp.achat.bl.permissions.Permissions;
import com.hyapp.achat.bl.socket.IOSocket;
import com.hyapp.achat.bl.socket.PeopleApi;
import com.hyapp.achat.bl.utils.NetUtils;
import com.hyapp.achat.bl.utils.NotifUtils;
import com.hyapp.achat.da.LoginPreferences;
import com.hyapp.achat.model.ConnLive;
import com.hyapp.achat.model.event.Event;
import com.hyapp.achat.model.event.LoginEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SocketService extends Service {

    public static final String EXTRA_LOGIN_EVENT = "LoginEvent";

    private IOSocket ioSocket;

    public static void start(Context context, String loginEventJsonStr) {
        Intent intent = new Intent(context, SocketService.class);
        intent.putExtra(SocketService.EXTRA_LOGIN_EVENT, loginEventJsonStr);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        registerReceiver(netReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        ConnLive.singleton().setValue(ConnLive.Status.CONNECTING);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String loginEventJsonStr = intent.getStringExtra(EXTRA_LOGIN_EVENT);
        if (loginEventJsonStr != null && ioSocket == null) {
            ioSocket = new IOSocket(loginEventJsonStr);
            LoginPreferences.singleton(this).putLogged(true);
        }

        NotifUtils.createSocketChannel(this);
        startForeground(NotifUtils.ID_SOCKET, NotifUtils.getSocketNotif(this));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        unregisterReceiver(netReceiver);
        ioSocket.destroy();
        LoginPreferences.singleton(this).putLogged(false);
    }

    private final BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetUtils.isNetConnected(context)) {
                if (ioSocket.getSocket().connected()) {
                    ConnLive.singleton().setValue(ConnLive.Status.CONNECTED);
                } else {
                    ConnLive.singleton().setValue(ConnLive.Status.CONNECTING);
                }
            } else {
                ConnLive.singleton().setValue(ConnLive.Status.NO_NET);
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRequestPeople(Event event) {
        if (event.action == Event.ACTION_REQUEST_PEOPLE) {
            PeopleApi.singleton().requestPeople(ioSocket.getSocket());
        }
    }
}
