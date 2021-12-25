package com.hyapp.achat.bl;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson.JSON;
import com.hyapp.achat.bl.service.SocketService;
import com.hyapp.achat.bl.socket.PeopleApi;
import com.hyapp.achat.da.LoginPreferences;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.SortedList;
import com.hyapp.achat.model.event.Event;
import com.hyapp.achat.model.event.LoginEvent;

import org.greenrobot.eventbus.EventBus;

public class MainViewModel extends AndroidViewModel {

    private MutableLiveData<Resource<SortedList<People>>> peopleLive;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        Context context = getApplication().getApplicationContext();
        peopleLive = PeopleApi.singleton().getPeopleLive();
        SocketService.start(context, LoginPreferences.singleton(context).getLoginEvent());
    }

    public void reloadPeople() {
        peopleLive.setValue(Resource.loading(null));
        EventBus.getDefault().post(new Event(Event.ACTION_REQUEST_PEOPLE));
    }

    public LiveData<Resource<SortedList<People>>> getPeopleLive() {
        return peopleLive;
    }
}