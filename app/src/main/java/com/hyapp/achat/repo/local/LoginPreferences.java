package com.hyapp.achat.repo.local;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * این کلاس برای سهولت استفاده از SharedPreferences ایجاد شده است.
 * کاربرد آن برای ذخیره سریع اطلاعاتی در حافظه خصوصی دستگاه است که حجم زیادی اشغال نمیکنند.*/
public class LoginPreferences {

    private static final String DEFAULT_NAME = "login";

    public static final String NAME = "name";
    public static final String BIO = "bio";
    public static final String GENDER = "gender";

    private static LoginPreferences instance;

    public static LoginPreferences singleton() {
        if (instance == null) {
            instance = new LoginPreferences();
        }
        return instance;
    }

    public void saveLoginGuest(Context context, String name, String bio, boolean gender){
        SharedPreferences.Editor writerPrefs = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE).edit();
        writerPrefs.putString(key, input);
        writerPrefs.apply();
    }


    public static void putString(Context context, String key, String input) {
        SharedPreferences.Editor writerPrefs = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE).edit();
        writerPrefs.putString(key, input);
        writerPrefs.apply();
    }

    public static String getString(Context context, String key, String defValue) {
        SharedPreferences readerPrefs = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
        return readerPrefs.getString(key, defValue);
    }

    public static void putBoolean(Context context, String key, boolean input) {
        SharedPreferences.Editor writerPrefs = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE).edit();
        writerPrefs.putBoolean(key, input);
        writerPrefs.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences readerPrefs = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
        return readerPrefs.getBoolean(key, defValue);
    }

    public static void putInt(Context context, String key, int input) {
        SharedPreferences.Editor writerPrefs = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE).edit();
        writerPrefs.putInt(key, input);
        writerPrefs.commit();
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences readerPrefs = context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
        return readerPrefs.getInt(key, defValue);
    }
}

