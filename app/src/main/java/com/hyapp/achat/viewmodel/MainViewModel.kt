package com.hyapp.achat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.PeopleRepo
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.preferences.LoginPreferences
import com.hyapp.achat.viewmodel.service.SocketService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

@ExperimentalCoroutinesApi
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _contactsLive = MutableLiveData<Resource<ContactList>>()
    val contactsLive = _contactsLive as LiveData<Resource<ContactList>>

    private val _peopleLive = MutableLiveData<Resource<PeopleList>>()
    val peopleLive = _peopleLive as LiveData<Resource<PeopleList>>


    init {
        val context = getApplication<Application>().applicationContext
        SocketService.start(context, LoginPreferences.singleton(context).loginEvent)
        loadContacts()
        reloadPeople()
        observeContacts()
        observePeople()
    }

    private fun loadContacts() {
        val contacts = ContactList(ContactDao.all)
        _contactsLive.value = Resource.add(contacts, Resource.INDEX_ALL)
    }

    fun reloadPeople() {
        _peopleLive.value = Resource.loading(null)
        viewModelScope.launch {
            PeopleRepo.requestPeople().collect { peopleList ->
                _peopleLive.value = Resource.add(peopleList, Resource.INDEX_ALL)
            }
        }
    }

    private fun observeContacts() {
        viewModelScope.launch {
            ChatRepo.contactFlow.collect { contact ->
                val resource = _contactsLive.value ?: Resource.success(ContactList())
                val contactList = resource.data ?: ContactList()
                val oldIndex = contactList.putFirst(contact)
                _contactsLive.value = Resource.add(contactList, oldIndex)
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