package com.hyapp.achat.da.socket;

import com.hyapp.achat.Config;
import com.hyapp.achat.model.ConnLive;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class IOSocket {

    private Socket socket;
    public LoginRepo loginRepo;
    public Socket getSocket() {
        return socket;
    }

    public IOSocket(String loginEventJsonStr) {
        IO.Options options = IO.Options.builder()
                .setQuery(Config.SOCKET_QUERY_DATA + "=" + loginEventJsonStr)
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

    public void destroy() {
        if (socket != null) {
            socket.off();
            socket.disconnect();
        }
    }

    private void createApis() {
        loginRepo = new LoginRepo(socket);
    }

    private void listen() {
        socket.on(Config.ON_DISCONNECT, onDisconnect);
        socket.on(Config.ON_CONNECT, onConnect);
        loginRepo.listen();
        PeopleRepo.singleton().listen(socket);
        ChatRepo.INSTANCE.listen(socket);
    }

    private final Emitter.Listener onDisconnect = args -> {
        ConnLive.singleton().postValue(ConnLive.Status.DISCONNECTED);
    };

    private final Emitter.Listener onConnect = args -> {
        ConnLive.singleton().postValue(ConnLive.Status.CONNECTED);
    };
}
