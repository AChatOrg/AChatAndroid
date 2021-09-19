package com.hyapp.achat.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hyapp.achat.api.LoginRepo;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.User;

public class LoginGuestViewModel extends ViewModel {

    public static final String MSG_EMPTY = "empty";
    public static final String MSG_EXIST = "exist";

    private MutableLiveData<Resource<User>> userLive;

    public void init() {
        userLive = LoginRepo.singleton().getUserLive();
    }

    public void loginGuest(String name, String bio, boolean gender) {
        if (name.isEmpty()) {
            userLive.setValue(Resource.error(MSG_EMPTY, null));
        } else {
            LoginRepo.singleton().loginGuest(name, bio, gender ? User.MALE : User.FEMALE);
        }
    }

    public MutableLiveData<Resource<User>> getUserLive() {
        return userLive;
    }
}