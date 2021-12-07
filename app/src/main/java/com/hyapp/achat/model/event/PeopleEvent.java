package com.hyapp.achat.model.event;

import com.hyapp.achat.model.People;
import com.hyapp.achat.model.SortedList;

public class PeopleEvent extends Event{

    public static final byte ACTION_REQUEST = 1;
    public static final byte ACTION_RESPONSE = 2;

    private SortedList<People> peopleList;

    public PeopleEvent(Status status, byte action, SortedList<People> peopleList) {
        super(status, action);
        this.peopleList = peopleList;
    }

    public PeopleEvent(Status status) {
        super(status);
    }

    public PeopleEvent(byte action) {
        super(action);
    }

    public PeopleEvent(Status status, String message) {
        super(status, message);
    }

    public SortedList<People> getPeopleList() {
        return peopleList;
    }
}
