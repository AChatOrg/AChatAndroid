package com.hyapp.achat.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.hyapp.achat.Config;
import com.hyapp.achat.model.entity.ConnLive;
import com.hyapp.achat.viewmodel.utils.SecureUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.socket.emitter.Emitter;

public class Preferences {

    private static final String DEFAULT_NAME = "login";

    public static final String LOGGED = "logged";

    public static final String NAME = "name";
    public static final String BIO = "bio";
    public static final String GENDER = "gender";

    public static final String NAME_SET = "nameSet";
    public static final String BIO_SET = "bioSet";

    public static final String LOGIN_INFO = "loginEvent";

    public static final String CONTACT_MESSAGES_COUNT = "CMC";

    private static Preferences instance;

    private final SharedPreferences preferences;

    public Preferences(Context context) {
        preferences = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new Preferences(context);
        }
    }

    public static Preferences instance() {
        return instance;
    }

    public void deleteALl() {
        SharedPreferences.Editor writer = preferences.edit();
        writer.clear();
        writer.apply();
    }

    public void putLogged(boolean logged) {
        SharedPreferences.Editor writer = preferences.edit();
        writer.putBoolean(LOGGED, logged);
        writer.apply();
    }

    public boolean getLogged() {
        return preferences.getBoolean(LOGGED, false);
    }

    public void putLoginGuest(String name, String bio, byte gender) {
        SharedPreferences.Editor writer = preferences.edit();
        writer.putString(NAME, name);
        writer.putString(BIO, bio);
        writer.putInt(GENDER, gender);
        if (!name.isEmpty()) {
            Set<String> nameSet = new HashSet<>(preferences.getStringSet(NAME_SET, new HashSet<>()));
            nameSet.add(name);
            writer.putStringSet(NAME_SET, nameSet);
        }
        if (!bio.isEmpty()) {
            Set<String> bioSet = new HashSet<>(preferences.getStringSet(BIO_SET, new HashSet<>()));
            bioSet.add(bio);
            writer.putStringSet(BIO_SET, bioSet);
        }
        writer.apply();
    }

    public String getLoginName() {
        return preferences.getString(NAME, "");
    }

    public String getLoginBio() {
        return preferences.getString(BIO, "");
    }

    public int getLoginGender() {
        return preferences.getInt(GENDER, 1);
    }

    public Set<String> getLoginNameSet() {
        return new HashSet<>(preferences.getStringSet(NAME_SET, new HashSet<>()));
    }

    public Set<String> getLoginBioSet() {
        return new HashSet<>(preferences.getStringSet(BIO_SET, new HashSet<>()));
    }

    public void putLoginInfo(String loginEventStr) {
        SharedPreferences.Editor writer = preferences.edit();
        writer.putString(LOGIN_INFO, loginEventStr);
        writer.apply();
    }

    public String getLoginInfo() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("operation", "");
        json.put("androidId", "");
        json.put("uid", "");
        json.put("name", "");
        json.put("bio", "");
        json.put("gender", "1");
        return preferences.getString(LOGIN_INFO, json.toString());
    }

    public void incrementContactMessagesCount(String contactUid) {
        long count = preferences.getLong(CONTACT_MESSAGES_COUNT + contactUid, 0);
        SharedPreferences.Editor writer = preferences.edit();
        writer.putLong(CONTACT_MESSAGES_COUNT + contactUid, count + 1);
        writer.apply();
    }

    public long getContactMessagesCount(String contactUid) {
        return preferences.getLong(CONTACT_MESSAGES_COUNT + contactUid, 0);
    }
}
