package com.hyapp.achat.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.hyapp.achat.model.People;
import com.hyapp.achat.repo.http.LoginService;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.repo.local.LoginPreferences;
import com.hyapp.achat.viewmodel.utils.NetUtils;

import java.util.Set;

public class LoginGuestViewModel extends AndroidViewModel implements Messages{

    private MutableLiveData<Resource<People>> userLive;

    public LoginGuestViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        userLive = (MutableLiveData<Resource<People>>) LoginService.singleton().getUserLive();
    }

    public void loginGuest(String name, String bio, boolean gender) {
        Context context = getApplication().getApplicationContext();
        if (name.isEmpty()) {
            userLive.setValue(Resource.error(MSG_EMPTY, null));
        } else if (!NetUtils.isNetConnected(context)) {
            userLive.setValue(Resource.error(MSG_NET, null));
        } else {
            String nameTrim = name.trim(), bioTrim = bio.trim();
            byte genderByte = gender ? People.MALE : People.FEMALE;
            LoginPreferences.singleton(context).putLoginGuest(nameTrim, bioTrim, genderByte);
            LoginService.singleton().loginGuest(nameTrim, bioTrim, genderByte);
        }
    }

    public String getSavedName() {
        return LoginPreferences.singleton(getApplication().getApplicationContext()).getName();
    }

    public String getSavedBio() {
        return LoginPreferences.singleton(getApplication().getApplicationContext()).getBio();
    }

    public boolean getSavedGender() {
        int gender = LoginPreferences.singleton(getApplication().getApplicationContext()).getGender();
        return gender == People.MALE;
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

    public MutableLiveData<Resource<People>> getUserLive() {
        return userLive;
    }
}
