package com.hyapp.achat.model;

public class User extends Thing {
    
    private Avatar avatar;

    public User(String name, String bio, byte gender, Avatar avatar) {
        super(name, bio, gender);
        this.avatar = avatar;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }
}
