package com.hyapp.achat.repo.http;

import com.hyapp.achat.model.People;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LoginApi {

    @GET("loginGuest/")
    Call<People> loginGuest(@Query("name") String name, @Query("bio") String bio, @Query("gender") byte gender);
}
