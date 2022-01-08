package com.hyapp.achat.model.entity;

import androidx.lifecycle.MutableLiveData;

public class ConnLive extends MutableLiveData<ConnLive.Status> {

    public enum Status {NO_NET, CONNECTING, CONNECTED, DISCONNECTED}

    private static ConnLive instance;

    public static ConnLive singleton() {
        if (instance == null) {
            instance = new ConnLive();
        }
        return instance;
    }
}
