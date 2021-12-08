package com.hyapp.achat.model.event;

import com.hyapp.achat.model.People;

public class LoggedEvent extends Event {
    public static final byte ACTION_ME = 1;
    public static final byte ACTION_OTHER = 2;

    private People people;

    public LoggedEvent(Status status) {
        super(status);
    }

    public LoggedEvent(Status status, byte action, People people) {
        super(status, action);
        this.people = people;
    }

    public LoggedEvent(Status status, String message) {
        super(status, message);
    }

    public LoggedEvent(Status status, String message, byte action) {
        super(status, message, action);
    }

    public LoggedEvent(Status status, byte action) {
        super(status, action);
    }

    public People getPeople() {
        return people;
    }
}
