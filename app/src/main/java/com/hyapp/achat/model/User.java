package com.hyapp.achat.model;

public class User extends Thing {

    private Avatar avatar;
    private Key key;

    public User(String name, String bio, byte gender, Avatar avatar) {
        super(name, bio, gender);
        this.avatar = avatar;
    }

    public User(String name, String bio, byte gender, Avatar avatar, Key key) {
        super(name, bio, gender);
        this.avatar = avatar;
        this.key = key;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
}
