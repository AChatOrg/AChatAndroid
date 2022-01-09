package com.hyapp.achat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hyapp.achat.model.PeopleRepo
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.People
import com.hyapp.achat.model.entity.PeopleList
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.model.preferences.LoginPreferences
import com.hyapp.achat.viewmodel.service.SocketService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _contactsLive = MutableLiveData<Resource<List<Contact>>>()
    val contactsLive = _contactsLive as LiveData<Resource<List<Contact>>>

    private val _peopleLive = MutableLiveData<Resource<PeopleList>>()
    val peopleLive = _peopleLive as LiveData<Resource<PeopleList>>


    init {
        val context = getApplication<Application>().applicationContext
        SocketService.start(context, LoginPreferences.singleton(context).loginEvent)
        observePeople()
        reloadPeople()
    }

    fun reloadPeople() {
        _peopleLive.value = Resource.loading(null)
        viewModelScope.launch {
            PeopleRepo.requestPeople().collect { peopleList ->
                _peopleLive.value = Resource.add(peopleList, Resource.INDEX_ALL)
            }
        }
    }

    private fun observePeople() {
        viewModelScope.launch {
            launch {
                PeopleRepo.userCameFlow.collect { people ->
                    onUserCame(people)
                }
            }
            launch {
                PeopleRepo.userLeftFlow.collect { uid ->
                    onUserLeft(uid)
                }
            }
        }
    }

    private fun onUserCame(people: People) {
        peopleLive.value?.data?.let {
            it.add(people)
            _peopleLive.value = Resource.add(it, it.indexOf(people))
        }
    }

    private fun onUserLeft(uid: String) {
        peopleLive.value?.data?.let {
            val index = it.remove(uid)
            if (index != PeopleList.INDEX_NOT_FOUND) {
                _peopleLive.value = Resource.remove(it, index)
            }
        }
    }
}