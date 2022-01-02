package com.hyapp.achat.model;

public class ProfileMessage extends Message {

    protected Contact contact;

    public ProfileMessage(Contact contact) {
        super(TYPE_PROFILE, TRANSFER_TYPE_RECEIVE, 0);
        this.contact = contact;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }
}
