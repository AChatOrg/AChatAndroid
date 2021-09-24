package com.hyapp.achat.repo.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hyapp.achat.model.User;

public class LoginPreferences {

    private static final String DEFAULT_NAME = "login";

    public static final String NAME = "name";
    public static final String BIO = "bio";
    public static final String GENDER = "gender";

    public static void putLoginGuest(Context context, String name, String bio, byte gender) {
        SharedPreferences.Editor writerPrefs = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE).edit();
        writerPrefs.putString(NAME, name);
        writerPrefs.putString(BIO, bio);
        writerPrefs.putInt(GENDER, gender);
        writerPrefs.apply();
    }

    public static LiveData<User> getLoginGuest(Context context) {
        MutableLiveData<User> userLive = new MutableLiveData<>();
        SharedPreferences readerPrefs = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
        userLive.setValue(new User(
                readerPrefs.getString(NAME, "")
                , readerPrefs.getString(BIO, "")
                , (byte) readerPrefs.getInt(GENDER, 1)
        ));
        return userLive;
    }
}

