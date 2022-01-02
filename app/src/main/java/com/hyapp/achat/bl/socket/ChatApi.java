package com.hyapp.achat.bl.socket;

import com.hyapp.achat.Config;
import com.hyapp.achat.model.event.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatApi {

    private final Socket socket;

    public ChatApi(Socket socket) {
        this.socket = socket;
    }

    public void listen() {
        socket.on(Config.ON_PV_MESSAGE, onPvMessage);
    }

    private final Emitter.Listener onPvMessage = args -> {
        EventBus.getDefault().post(new MessageEvent(args[0].toString(), MessageEvent.ACTION_RECEIVE));
    };

    public void sendPvMessage(String json) {
        socket.emit(Config.ON_PV_MESSAGE, json);
    }
}
