package com.hyapp.achat.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hyapp.achat.model.NetLiveData;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.People;
import com.hyapp.achat.repo.http.UsersService;
import com.hyapp.achat.viewmodel.utils.NetUtils;

import java.util.List;

public class MainViewModel extends AndroidViewModel implements Messages {

    private MutableLiveData<Resource<List<People>>> peopleLive;
    private NetLiveData netLive;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        netLive = new NetLiveData(getApplication().getApplicationContext());
        initPeople();
    }

    public void initPeople() {
        peopleLive = (MutableLiveData<Resource<List<People>>>) UsersService.singleton().getPeopleLive();
        if (NetUtils.isNetConnected(getApplication().getApplicationContext())) {
            UsersService.singleton().initPeople();
        } else {
            peopleLive.setValue(Resource.error(MSG_NET, null));
        }
    }

    public LiveData<Resource<List<People>>> getPeopleLive() {
        return peopleLive;
    }

    public NetLiveData getNetLive() {
        return netLive;
    }
}
