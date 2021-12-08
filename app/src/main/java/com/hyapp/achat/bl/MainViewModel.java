package com.hyapp.achat.bl;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.hyapp.achat.bl.socket.PeopleApi;
import com.hyapp.achat.bl.utils.NetUtils;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.SortedList;
import com.hyapp.achat.model.event.Event;

import org.greenrobot.eventbus.EventBus;

public class MainViewModel extends AndroidViewModel {

    private MutableLiveData<Resource<SortedList<People>>> peopleLive;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        peopleLive = PeopleApi.singleton().getPeopleLive();
    }

    public void reloadPeople() {
        if (NetUtils.isNetConnected(getApplication().getApplicationContext())) {
            peopleLive.setValue(Resource.loading(null));
            EventBus.getDefault().post(new Event(Event.ACTION_REQUEST_PEOPLE));
        } else {
            peopleLive.setValue(Resource.error(Event.MSG_NET, null));
        }
    }

    public MutableLiveData<Resource<SortedList<People>>> getPeopleLive() {
        return peopleLive;
    }
}