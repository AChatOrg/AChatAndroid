package com.hyapp.achat.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Avatar implements Parcelable {

    private Uri uri;
    private boolean isOnline;

    public Avatar(Uri uri, boolean isOnline) {
        this.uri = uri;
        this.isOnline = isOnline;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.uri, flags);
        dest.writeByte(this.isOnline ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.uri = source.readParcelable(Uri.class.getClassLoader());
        this.isOnline = source.readByte() != 0;
    }

    protected Avatar(Parcel in) {
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.isOnline = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Avatar> CREATOR = new Parcelable.Creator<Avatar>() {
        @Override
        public Avatar createFromParcel(Parcel source) {
            return new Avatar(source);
        }

        @Override
        public Avatar[] newArray(int size) {
            return new Avatar[size];
        }
    };
}
