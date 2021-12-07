package com.hyapp.achat.bl.socket;

import com.alibaba.fastjson.JSON;
import com.hyapp.achat.Config;
import com.hyapp.achat.model.event.LoginEvent;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class IOSocket {

    private Socket socket;
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
        loginApi.listen();
        PeopleApi.singleton().listen(socket);
    }
}
