package com.hyapp.achat.viewmodel.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import com.hyapp.achat.model.IOSocket
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.entity.ConnLive
import com.hyapp.achat.viewmodel.Notifs
import com.hyapp.achat.viewmodel.utils.NetUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SocketService : Service() {

    companion object {
        const val EXTRA_LOGIN_EVENT = "LoginEvent"

        var ioSocket: IOSocket? = null

        fun start(context: Context, loginJson: String?) {
            val intent = Intent(context, SocketService::class.java)
            intent.putExtra(EXTRA_LOGIN_EVENT, loginJson)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        registerReceiver(netReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        ConnLive.singleton().value = ConnLive.Status.CONNECTING
        Notifs.createSocketChannel(this)
        Notifs.createMessagingChannel(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val loginJson = intent.getStringExtra(EXTRA_LOGIN_EVENT)

        if (ioSocket == null) {
            ioSocket = IOSocket(loginJson ?: Preferences.instance().loginInfo)
        } else {
            ioSocket?.setQuery(Preferences.instance().loginInfo)
        }
        startForeground(Notifs.ID_SOCKET, Notifs.getSocketNotif(this))
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(netReceiver)
        ioSocket?.destroy()
        ioSocket = null
    }

    private val netReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (NetUtils.isNetConnected(context)) {
                if (ioSocket != null && ioSocket!!.socket.connected()) {
                    ConnLive.singleton().value = ConnLive.Status.CONNECTED
                } else {
                    ConnLive.singleton().value = ConnLive.Status.CONNECTING
                }
            } else {
                ConnLive.singleton().value = ConnLive.Status.NO_NET
            }
        }
    }
}
