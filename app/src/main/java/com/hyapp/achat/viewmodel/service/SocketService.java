package com.hyapp.achat.viewmodel.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.hyapp.achat.model.ChatRepo;
import com.hyapp.achat.model.IOSocket;
import com.hyapp.achat.model.entity.ConnLive;
import com.hyapp.achat.model.event.MessageEvent;
import com.hyapp.achat.model.preferences.LoginPreferences;
import com.hyapp.achat.viewmodel.utils.NetUtils;
import com.hyapp.achat.viewmodel.utils.NotifUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SocketService extends Service {

    public static final String EXTRA_LOGIN_EVENT = "LoginEvent";

    @Nullable
    public static IOSocket ioSocket;

    public static void start(Context context, String loginJson) {
        Intent intent = new Intent(context, SocketService.class);
        intent.putExtra(SocketService.EXTRA_LOGIN_EVENT, loginJson);
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
        registerReceiver(netReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        ConnLive.singleton().setValue(ConnLive.Status.CONNECTING);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String loginJson = intent.getStringExtra(EXTRA_LOGIN_EVENT);
        if (loginJson != null && ioSocket == null) {
            ioSocket = new IOSocket(loginJson);
            LoginPreferences.singleton(getApplicationContext()).putLogged(true);
        }

        NotifUtils.createSocketChannel(this);
        startForeground(NotifUtils.ID_SOCKET, NotifUtils.getSocketNotif(this));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(netReceiver);
        if (ioSocket != null) {
            ioSocket.destroy();
        }
        ioSocket = null;
        LoginPreferences.singleton(getApplicationContext()).putLogged(false);
    }

    private final BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetUtils.isNetConnected(context)) {
                if (ioSocket != null && ioSocket.getSocket().connected()) {
                    ConnLive.singleton().setValue(ConnLive.Status.CONNECTED);
                } else {
                    ConnLive.singleton().setValue(ConnLive.Status.CONNECTING);
                }
            } else {
                ConnLive.singleton().setValue(ConnLive.Status.NO_NET);
            }
        }
    };
}
