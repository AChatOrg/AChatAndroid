package com.hyapp.achat.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hyapp.achat.api.UserHttpRepo;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.User;

public class LoginGuestViewModel extends ViewModel {

    private MutableLiveData<Resource<User>> userLive;

    public void loginGuest(String name, String bio, byte gender) {
        UserHttpRepo repo = UserHttpRepo.singletone();
        userLive = repo.getUserLive();
        repo.loginGuest(name,bio,gender);
    }

    public MutableLiveData<Resource<User>> getUserLive() {
        return userLive;
    }
}
