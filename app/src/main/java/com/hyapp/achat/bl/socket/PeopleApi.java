package com.hyapp.achat.bl.socket;

import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.hyapp.achat.Config;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.SortedList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PeopleApi {

    private static PeopleApi instance;

    public static PeopleApi singleton() {
        if (instance == null) {
            instance = new PeopleApi();
        }
        return instance;
    }

    private final MutableLiveData<Resource<SortedList<People>>> peopleLive;


    public PeopleApi() {
        peopleLive = new MutableLiveData<>();
    }

    public void listen(Socket socket) {
        socket.on(Config.ON_PEOPLE, onPeopleList);
    }

    public void requestPeople(Socket socket) {
        socket.emit(Config.ON_PEOPLE);
    }

    private final Emitter.Listener onPeopleList = args -> {
        JSONArray jsonArray = JSON.parseArray(args[0].toString());
        SortedList<People> peopleList = new SortedList<>(People::compare);
        for (int i = 0; i < jsonArray.size(); i++) {
            People people = jsonArray.getObject(i, People.class);
            peopleList.add(people);
        }
        getPeopleLive().postValue(Resource.success(peopleList));
    };

    public MutableLiveData<Resource<SortedList<People>>> getPeopleLive() {
        return peopleLive;
    }
}
