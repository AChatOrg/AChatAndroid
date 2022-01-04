package com.hyapp.achat.model.event;

public abstract class Event {

    public enum Status {SUCCESS, ERROR, LOADING}

    public static final String MSG_EMPTY = "empty";
    public static final String MSG_EXIST = "exist";
    public static final String MSG_NET = "net";
    public static final String MSG_ERROR = "error";

    public transient Status status;
    public transient String msg;
    public transient byte action;

    public Event() {
    }

    public Event(Status status) {
        this.status = status;
    }

    public Event(byte action) {
        this.action = action;
    }

    public Event(Status status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Event(Status status, byte action) {
        this.status = status;
        this.action = action;
    }

    public Event(Status status, String msg, byte action) {
        this.status = status;
        this.msg = msg;
        this.action = action;
    }
}
