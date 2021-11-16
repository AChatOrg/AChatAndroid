package com.hyapp.achat.bl;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hyapp.achat.bl.utils.NetUtils;
import com.hyapp.achat.model.People;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        initPeople();
    }

    public void initPeople() {
//        peopleLive = (MutableLiveData<Resource<List<People>>>) UsersService.singleton().getPeopleLive();
//        if (NetUtils.isNetConnected(getApplication().getApplicationContext())) {
//            UsersService.singleton().initPeople();
//        } else {
//            peopleLive.setValue(Resource.error(MSG_NET, null));
//        }
    }
}