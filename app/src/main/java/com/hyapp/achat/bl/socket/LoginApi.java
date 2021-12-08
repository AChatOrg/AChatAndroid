package com.hyapp.achat.bl.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hyapp.achat.Config;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.event.Event;
import com.hyapp.achat.model.event.LoggedEvent;

import org.greenrobot.eventbus.EventBus;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LoginApi {

    private final Socket socket;

    public LoginApi(Socket socket) {
        this.socket = socket;
    }

    public void listen() {
        socket.on(Config.ON_LOGGED, onLogged);
    }

    private final Emitter.Listener onLogged = args -> {
        People people = JSON.parseObject(args[0].toString(), People.class);
        EventBus.getDefault().post(new LoggedEvent(Event.Status.SUCCESS, LoggedEvent.ACTION_ME, people));
    };
}
