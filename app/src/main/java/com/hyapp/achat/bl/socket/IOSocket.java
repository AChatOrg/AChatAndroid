package com.hyapp.achat.bl.socket;

import com.alibaba.fastjson.JSON;
import com.hyapp.achat.Config;
import com.hyapp.achat.model.event.Event;
import com.hyapp.achat.model.event.LoginEvent;

import org.greenrobot.eventbus.EventBus;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class IOSocket {

    private Socket socket;
    private Runnable onDisconnectListener;
    public LoginApi loginApi;

    public Socket getSocket() {
        return socket;
    }

    public IOSocket(LoginEvent loginEvent) {
        IO.Options options = IO.Options.builder()
                .setQuery(Config.SOCKET_QUERY_DATA + "=" + JSON.toJSONString(loginEvent))
                .build();
        try {
            socket = IO.socket(Config.SERVER_URL, options);
            socket.connect();
            createApis();
            listen();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public IOSocket(LoginEvent loginEvent, Runnable onDisconnectListener) {
        this(loginEvent);
        this.onDisconnectListener = onDisconnectListener;
    }

    public void destroy() {
        if (socket != null) {
            socket.off();
            socket.disconnect();
        }
    }

    private void createApis() {
        loginApi = new LoginApi(socket);
    }

    private void listen() {
        socket.on(Config.ON_DISCONNECT, onDisconnect);
        loginApi.listen();
        PeopleApi.singleton().listen(socket);
    }

    private final Emitter.Listener onDisconnect = args -> {
        EventBus.getDefault().post(new Event(Event.ACTION_EXIT_APP));
        if (onDisconnectListener != null) {
            onDisconnectListener.run();
        }
    };

    public void setOnDisconnectListener(Runnable onDisconnectListener) {
        this.onDisconnectListener = onDisconnectListener;
    }
}
