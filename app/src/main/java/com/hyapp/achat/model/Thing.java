package com.hyapp.achat.model;

public abstract class Thing {

    public static final byte MALE = 1;
    public static final byte FEMALE = 2;
    public static final byte MIXED = 3;

    private String name;
    private String bio;
    private byte gender;

    public Thing(String name, String bio, byte gender) {
        this.name = name;
        this.bio = bio;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }
}
