package com.hyapp.achat.model.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.hyapp.achat.model.entity.Login;

import java.util.HashSet;
import java.util.Set;

public class LoginPreferences {

    private static final String DEFAULT_NAME = "login";

    public static final String LOGGED = "logged";

    public static final String NAME = "name";
    public static final String BIO = "bio";
    public static final String GENDER = "gender";

    public static final String NAME_SET = "nameSet";
    public static final String BIO_SET = "bioSet";

    public static final String LOGIN_EVENT = "logginEvent";

    private static LoginPreferences instance;

    private final SharedPreferences preferences;

    public LoginPreferences(Context context) {
        preferences = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
    }

    public static LoginPreferences singleton(Context context) {
        if (instance == null) {
            instance = new LoginPreferences(context);
        }
        return instance;
    }

    public void putLogged(boolean logged) {
        SharedPreferences.Editor writerPrefs = preferences.edit();
        writerPrefs.putBoolean(LOGGED, logged);
        writerPrefs.apply();
    }

    public boolean getLogged() {
        return preferences.getBoolean(LOGGED, false);
    }

    public void putLoginGuest(String name, String bio, byte gender) {
        SharedPreferences.Editor writerPrefs = preferences.edit();
        writerPrefs.putString(NAME, name);
        writerPrefs.putString(BIO, bio);
        writerPrefs.putInt(GENDER, gender);
        if (!name.isEmpty()) {
            Set<String> nameSet = new HashSet<>(preferences.getStringSet(NAME_SET, new HashSet<>()));
            nameSet.add(name);
            writerPrefs.putStringSet(NAME_SET, nameSet);
        }
        if (!bio.isEmpty()) {
            Set<String> bioSet = new HashSet<>(preferences.getStringSet(BIO_SET, new HashSet<>()));
            bioSet.add(bio);
            writerPrefs.putStringSet(BIO_SET, bioSet);
        }
        writerPrefs.apply();
    }

    public String getName() {
        return preferences.getString(NAME, "");
    }

    public String getBio() {
        return preferences.getString(BIO, "");
    }

    public int getGender() {
        return preferences.getInt(GENDER, 1);
    }

    public Set<String> getNameSet() {
        return new HashSet<>(preferences.getStringSet(NAME_SET, new HashSet<>()));
    }

    public Set<String> getBioSet() {
        return new HashSet<>(preferences.getStringSet(BIO_SET, new HashSet<>()));
    }

    public void putLoginEvent(String loginEventStr) {
        SharedPreferences.Editor writerPrefs = preferences.edit();
        writerPrefs.putString(LOGIN_EVENT, loginEventStr);
        writerPrefs.apply();
    }

    public String getLoginEvent() {
        return preferences.getString(LOGIN_EVENT, new Gson().toJson(new Login()));
    }
}

