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

    public People() {
    }

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

    public static int compare(People o1, People o2) {
        if (o1.getKey().getIpv4().equals(o2.getKey().getIpv4())) return 0;
        if (o1.getKey().getRank() < o2.getKey().getRank()) return 1;
        if (o1.getKey().getRank() > o2.getKey().getRank()) return -1;
        if (o1.getKey().getScore() < o2.getKey().getScore()) return 1;
        if (o1.getKey().getScore() > o2.getKey().getScore()) return -1;
        if (o1.getKey().getLoginTime() < o2.getKey().getLoginTime()) return -1;
        if (o1.getKey().getLoginTime() > o2.getKey().getLoginTime()) return 1;
        return 0;
    }
}
