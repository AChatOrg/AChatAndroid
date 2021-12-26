package com.hyapp.achat.bl;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.gson.Gson;
import com.hyapp.achat.Config;
import com.hyapp.achat.bl.service.SocketService;
import com.hyapp.achat.bl.utils.NetUtils;
import com.hyapp.achat.da.LoginPreferences;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.event.Event;
import com.hyapp.achat.model.event.LoggedEvent;
import com.hyapp.achat.model.event.LoginEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Set;

public class LoginGuestViewModel extends AndroidViewModel {

    public LoginGuestViewModel(@NonNull Application application) {
        super(application);
    }

    public void loginGuest(String name, String bio, boolean gender) {
        Context context = getApplication().getApplicationContext();
        if (name.isEmpty()) {
            EventBus.getDefault().post(new LoggedEvent(Event.Status.ERROR, Event.MSG_EMPTY, LoggedEvent.ACTION_ME));
        } else if (!NetUtils.isNetConnected(context)) {
            EventBus.getDefault().post(new LoggedEvent(Event.Status.ERROR, Event.MSG_NET, LoggedEvent.ACTION_ME));
        } else {
            String nameTrim = name.trim(), bioTrim = bio.trim();
            byte genderByte = gender ? People.GENDER_MALE : People.GENDER_FEMALE;
            LoginPreferences.singleton(context).putLoginGuest(nameTrim, bioTrim, genderByte);
            EventBus.getDefault().post(new LoggedEvent(Event.Status.LOADING, LoggedEvent.ACTION_ME));
            String loginEventJsonStr = new Gson().toJson(new LoginEvent(Config.OPERATION_LOGIN_GUEST, nameTrim, bioTrim, genderByte));
            SocketService.start(context, loginEventJsonStr);
            LoginPreferences.singleton(context).putLoginEvent(loginEventJsonStr);
        }
    }

    public void cancelLogin() {
        Context context = getApplication().getApplicationContext();
        context.stopService(new Intent(context, SocketService.class));
    }

    public String getSavedName() {
        return LoginPreferences.singleton(getApplication().getApplicationContext()).getName();
    }

    public String getSavedBio() {
        return LoginPreferences.singleton(getApplication().getApplicationContext()).getBio();
    }

    public boolean getSavedGender() {
        int gender = LoginPreferences.singleton(getApplication().getApplicationContext()).getGender();
        return gender == People.GENDER_MALE;
    }

    public String[] getNameHistory() {
        Set<String> set = LoginPreferences.singleton(getApplication().getApplicationContext()).getNameSet();
        String[] history = new String[set.size()];
        return set.toArray(history);
    }

    public String[] getBioHistory() {
        Set<String> set = LoginPreferences.singleton(getApplication().getApplicationContext()).getBioSet();
        String[] history = new String[set.size()];
        return set.toArray(history);
    }
}