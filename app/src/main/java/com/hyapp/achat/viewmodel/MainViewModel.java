package com.hyapp.achat.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.People;
import com.hyapp.achat.repo.http.UsersService;
import com.hyapp.achat.viewmodel.utils.NetUtils;

import java.util.List;

public class MainViewModel extends AndroidViewModel implements Messages {

    private MutableLiveData<Resource<List<People>>> peopleLive;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
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

    public int getPeopleSize() {
        Resource<List<People>> resource = peopleLive.getValue();
        return resource == null ? 0 : resource.data == null ? 0 : resource.data.size();
    }

    public int getGroupsSize() {
        return 0;
    }
}
