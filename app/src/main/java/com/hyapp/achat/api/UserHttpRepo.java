package com.hyapp.achat.api;

import androidx.lifecycle.MutableLiveData;

import com.hyapp.achat.Config;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserHttpRepo {

    private static UserHttpRepo instance;

    private final UserHttpApi api;
    private final MutableLiveData<Resource<User>> userLive;

    public UserHttpRepo() {
        userLive = new MutableLiveData<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(UserHttpApi.class);
    }

    public static UserHttpRepo singletone() {
        if (instance == null) {
            instance = new UserHttpRepo();
        }
        return instance;
    }

    public void loginGuest(String name, String bio, byte gender) {
        api.loginGuest(name, bio, gender).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                if (user != null) {
                    userLive.postValue(Resource.success(user));
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
