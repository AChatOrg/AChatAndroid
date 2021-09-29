package com.hyapp.achat.repo.http;

import com.hyapp.achat.model.People;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UsersApi {

    @GET("onlineUsers/")
    Call<List<People>> getPeople();
}
