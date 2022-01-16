package com.hyapp.achat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.UsersRepo
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.viewmodel.service.SocketService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _contactsLive = MutableLiveData<Resource<ContactList>>()
    val contactsLive = _contactsLive as LiveData<Resource<ContactList>>

    private val _usersLive = MutableLiveData<Resource<UserList>>()
    val usersLive = _usersLive as LiveData<Resource<UserList>>


    init {
        val context = getApplication<Application>().applicationContext
        SocketService.start(context, Preferences.instance().loginInfo)
        loadContacts()
        reloadPeople()
        observeContacts()
        observePeople()
    }

    private fun loadContacts() {
        val contacts = ContactList(ContactDao.all())
        _contactsLive.value = Resource.add(contacts, Resource.INDEX_ALL)
    }

    fun reloadPeople() {
        _usersLive.value = Resource.loading(null)
        viewModelScope.launch {
            UsersRepo.requestUsers().collect { peopleList ->
                _usersLive.value = Resource.add(peopleList, Resource.INDEX_ALL)
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
                UsersRepo.userCameFlow.collect { people ->
                    onUserCame(people)
                }
            }
            launch {
                UsersRepo.userLeftFlow.collect { uid ->
                    onUserLeft(uid)
                }
            }
        }
    }

    private fun onUserCame(user: User) {
        usersLive.value?.data?.let {
            it.add(user)
            _usersLive.value = Resource.add(it, it.indexOf(user))
        }
    }

    private fun onUserLeft(uid: String) {
        usersLive.value?.data?.let {
            val index = it.remove(uid)
            if (index != UserList.INDEX_NOT_FOUND) {
                _usersLive.value = Resource.remove(it, index)
            }
        }
    }
}