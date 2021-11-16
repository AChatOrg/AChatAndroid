package com.hyapp.achat.model.event;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginEvent extends Event implements Parcelable {
    private String operation;
    private String name;
    private String bio;
    private byte gender;

    public LoginEvent(String operation, String name, String bio, byte gender) {
        this.operation = operation;
        this.name = name;
        this.bio = bio;
        this.gender = gender;
    }

    public String getOperation() {
        return operation;
    }

    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }

    public byte getGender() {
        return gender;
    }

    /********************* Parcelable ********************/
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.operation);
        dest.writeString(this.name);
        dest.writeString(this.bio);
        dest.writeByte(this.gender);
    }

    public void readFromParcel(Parcel source) {
        this.operation = source.readString();
        this.name = source.readString();
        this.bio = source.readString();
        this.gender = source.readByte();
    }

    protected LoginEvent(Parcel in) {
        this.operation = in.readString();
        this.name = in.readString();
        this.bio = in.readString();
        this.gender = in.readByte();
    }

    public static final Parcelable.Creator<LoginEvent> CREATOR = new Parcelable.Creator<LoginEvent>() {
        @Override
        public LoginEvent createFromParcel(Parcel source) {
            return new LoginEvent(source);
        }

        @Override
        public LoginEvent[] newArray(int size) {
            return new LoginEvent[size];
        }
    };
}
