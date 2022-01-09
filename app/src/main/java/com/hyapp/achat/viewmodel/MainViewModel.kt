package com.hyapp.achat.viewmodel

import android.app.Application
import com.hyapp.achat.model.ChatRepo.contactsLive
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.hyapp.achat.model.entity.Contact
import androidx.lifecycle.MutableLiveData
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.PeopleRepo
import com.hyapp.achat.model.entity.People
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.model.entity.SortedList
import com.hyapp.achat.model.event.ActionEvent
import com.hyapp.achat.viewmodel.service.SocketService
import com.hyapp.achat.model.preferences.LoginPreferences
import org.greenrobot.eventbus.EventBus

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var contactsLive: LiveData<Resource<List<Contact>>>? = null
        private set
    private var peopleLive: MutableLiveData<Resource<SortedList<People>?>>? = null
    fun init() {
        val context = getApplication<Application>().applicationContext
        contactsLive = ChatRepo.contactsLive
        peopleLive = PeopleRepo.singleton().peopleLive
        SocketService.start(context, LoginPreferences.singleton(context).loginEvent)
    }

    fun reloadPeople() {
        peopleLive!!.value = Resource.loading(null)
        EventBus.getDefault().post(ActionEvent(ActionEvent.ACTION_REQUEST_PEOPLE))
    }

    fun getPeopleLive(): LiveData<Resource<SortedList<People>?>>? {
        return peopleLive
    }
}