package com.hyapp.achat.model;

public class Avatar {

    private String url;
    private boolean isOnline;

    public Avatar(String url, boolean isOnline) {
        this.url = url;
        this.isOnline = isOnline;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
