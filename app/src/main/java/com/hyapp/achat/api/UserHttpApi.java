package com.hyapp.achat.api;

import com.hyapp.achat.model.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UserHttpApi {

    @GET("loginGuest/")
    Call<User> loginGuest(@Query("name") String name, @Query("bio") String bio, @Query("gender") byte gender);
}
