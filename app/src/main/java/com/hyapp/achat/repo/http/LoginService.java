package com.hyapp.achat.repo.http;

import androidx.lifecycle.MutableLiveData;

import com.hyapp.achat.Config;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.User;
import com.hyapp.achat.viewmodel.LoginGuestViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginService {

    private static LoginService instance;

    private final LoginApi api;
    private final MutableLiveData<Resource<User>> userLive;

    public LoginService() {
        userLive = new MutableLiveData<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(LoginApi.class);
    }

    public static LoginService singleton() {
        if (instance == null) {
            instance = new LoginService();
        }
        return instance;
    }

    public void loginGuest(String name, String bio, byte gender) {
        userLive.setValue(Resource.loading(null));
        api.loginGuest(name, bio, gender).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        userLive.postValue(Resource.success(user));
                    } else {
                        userLive.postValue(Resource.error(response.message(), null));
                    }
                } else if (response.code() == 409) {
                    userLive.postValue(Resource.error(LoginGuestViewModel.MSG_EXIST, null));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                userLive.postValue(Resource.error(t.getMessage(), null));
            }
        });
    }

    public MutableLiveData<Resource<User>> getUserLive() {
        return userLive;
    }
}
