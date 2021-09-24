package com.hyapp.achat.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hyapp.achat.repo.http.LoginService;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.User;
import com.hyapp.achat.repo.local.LoginPreferences;
import com.hyapp.achat.viewmodel.utils.NetUtils;

public class LoginGuestViewModel extends AndroidViewModel {

    public static final String MSG_EMPTY = "empty";
    public static final String MSG_EXIST = "exist";
    public static final String MSG_NET = "net";
    public static final String MSG_ERROR = "error";

    private MutableLiveData<Resource<User>> userLive;
    private LiveData<User> savedUserLive;

    public LoginGuestViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        userLive = LoginService.singleton().getUserLive();
        savedUserLive = LoginPreferences.getLoginGuest(getApplication().getApplicationContext());
    }

    public void loginGuest(String name, String bio, boolean gender) {
        Context context = getApplication().getApplicationContext();
        if (name.isEmpty()) {
            userLive.setValue(Resource.error(MSG_EMPTY, null));
        } else if (!NetUtils.isNetConnected(context)) {
            userLive.setValue(Resource.error(MSG_NET, null));
        } else {
            String nameTrim = name.trim(), bioTrim = bio.trim();
            byte genderByte = gender ? User.MALE : User.FEMALE;
            LoginPreferences.putLoginGuest(context, nameTrim, bioTrim, genderByte);
            LoginService.singleton().loginGuest(nameTrim, bioTrim, genderByte);
        }
    }

    public MutableLiveData<Resource<User>> getUserLive() {
        return userLive;
    }

    public LiveData<User> getSavedUserLive() {
        return savedUserLive;
    }
}
