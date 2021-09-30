package com.hyapp.achat.repo.http;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hyapp.achat.Config;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.People;
import com.hyapp.achat.viewmodel.MainViewModel;
import com.hyapp.achat.viewmodel.livevent.LiveEvent;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UsersService {

    private static UsersService instance;

    private final UsersApi api;
    private final MutableLiveData<Resource<List<People>>> peopleLive;

    public UsersService() {
        peopleLive = new MutableLiveData<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(UsersApi.class);
    }

    public static UsersService singleton() {
        if (instance == null) {
            instance = new UsersService();
        }
        return instance;
    }

    public void initPeople() {
        peopleLive.setValue(Resource.loading(null));
        api.getPeople().enqueue(new Callback<List<People>>() {
            @Override
            public void onResponse(Call<List<People>> call, Response<List<People>> response) {
                if (response.isSuccessful()) {
                    List<People> people = response.body();
                    if (people != null) {
                        peopleLive.postValue(Resource.success(people));
                    } else {
                        peopleLive.postValue(Resource.error(response.message(), null));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<People>> call, Throwable t) {
                peopleLive.postValue(Resource.error(MainViewModel.MSG_ERROR, null));
            }
        });
    }

    public LiveData<Resource<List<People>>> getPeopleLive() {
        return peopleLive;
    }
}
