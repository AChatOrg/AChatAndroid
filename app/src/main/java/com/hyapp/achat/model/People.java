package com.hyapp.achat.model;

public class People extends Thing {

    public static final byte RANK_GUEST = 0;
    public static final byte RANK_MEMBER = 1;
    public static final byte RANK_SPECIAL = 2;
    public static final byte RANK_ACTIVE = 3;
    public static final byte RANK_SENIOR = 4;
    public static final byte RANK_ADMIN = 5;
    public static final byte RANK_MANAGER = 6;

    private Avatar avatar;
    private Key key;

    public People(String name, String bio, byte gender) {
        super(name, bio, gender);
    }

    public People(String name, String bio, byte gender, Avatar avatar) {
        this(name, bio, gender);
        this.avatar = avatar;
    }

    public People(String name, String bio, byte gender, Avatar avatar, Key key) {
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
