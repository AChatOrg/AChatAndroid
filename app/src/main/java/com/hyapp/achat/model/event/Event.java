package com.hyapp.achat.model.event;

public class Event {

    public enum Status {SUCCESS, ERROR, LOADING}

    public static final String MSG_EMPTY = "empty";
    public static final String MSG_EXIST = "exist";
    public static final String MSG_NET = "net";
    public static final String MSG_ERROR = "error";

    public static final byte ACTION_REQUEST_PEOPLE = 1;
    public static final byte ACTION_EXIT_APP = 2;

    public transient Status status;
    public transient String message;
    public transient byte action;

    public Event() {
    }

    public Event(Status status) {
        this.status = status;
    }

    public Event(byte action) {
        this.action = action;
    }

    public Event(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Event(Status status, byte action) {
        this.status = status;
        this.action = action;
    }

    public Event(Status status, String message, byte action) {
        this.status = status;
        this.message = message;
        this.action = action;
    }
}
