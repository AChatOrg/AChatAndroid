package com.hyapp.achat.bl;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hyapp.achat.bl.service.SocketService;
import com.hyapp.achat.da.socket.ChatRepo;
import com.hyapp.achat.da.socket.PeopleRepo;
import com.hyapp.achat.da.LoginPreferences;
import com.hyapp.achat.model.Contact;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.SortedList;
import com.hyapp.achat.model.event.ActionEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<Resource<List<Contact>>> contactsLive;
    private MutableLiveData<Resource<SortedList<People>>> peopleLive;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        Context context = getApplication().getApplicationContext();
        contactsLive = ChatRepo.INSTANCE.getContactsLive();
        peopleLive = PeopleRepo.singleton().getPeopleLive();
        SocketService.start(context, LoginPreferences.singleton(context).getLoginEvent());
    }

    public void reloadPeople() {
        peopleLive.setValue(Resource.loading(null));
        EventBus.getDefault().post(new ActionEvent(ActionEvent.ACTION_REQUEST_PEOPLE));
    }

    public LiveData<Resource<SortedList<People>>> getPeopleLive() {
        return peopleLive;
    }

    public LiveData<Resource<List<Contact>>> getContactsLive() {
        return contactsLive;
    }
}