package com.hyapp.achat.bl.socket;

import com.alibaba.fastjson.JSON;
import com.hyapp.achat.Config;
import com.hyapp.achat.model.event.LoginEvent;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class IOSocket {

    private Socket socket;
    private LoginApi loginApi;

    public IOSocket() {
        this.loginApi = new LoginApi();
    }

    private void create(String data) {
        IO.Options options = IO.Options.builder()
                .setQuery(Config.SOCKET_QUERY_DATA + "=" + data)
                .build();
        try {
            socket = IO.socket(Config.SERVER_URL, options);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect(LoginEvent loginEvent) {
        create(JSON.toJSONString(loginEvent));
        if (socket != null) {
            socket.connect();
        }
    }

    public void disconnect() {
        if (socket != null) {
            socket.off();
            socket.disconnect();
        }
    }

    public void listen() {
        if (socket != null) {
            loginApi.listen(socket);
        }
    }
}
