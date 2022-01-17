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

    private val _contactsLive = MutableLiveData<ContactList>()
    val contactsLive = _contactsLive as LiveData<ContactList>

    private val _usersLive = MutableLiveData<Resource<SortedList<User>>>()
    val usersLive = _usersLive as LiveData<Resource<SortedList<User>>>


    init {
        val context = getApplication<Application>().applicationContext
        SocketService.start(context, Preferences.instance().loginInfo)
        loadContacts()
        reloadUsers()
        observeContacts()
        observeUsers()
    }

    private fun loadContacts() {
        _contactsLive.value = ContactList(ContactDao.all())
    }

    fun reloadUsers() {
        _usersLive.value = Resource.loading(null)
        viewModelScope.launch {
            UsersRepo.requestUsers().collect { userList ->
                _usersLive.value = Resource.success(userList)
            }
        }
    }

    private fun observeContacts() {
        viewModelScope.launch {
            ChatRepo.contactFlow.collect { pair ->
                when (pair.first) {
                    ChatRepo.CONTACT_PUT -> putContact(pair.second)
                    ChatRepo.CONTACT_UPDATE -> updateContact(pair.second)
                }
            }
        }
    }

    private fun observeUsers() {
        viewModelScope.launch {
            launch {
                UsersRepo.userCameFlow.collect { user ->
                    usersLive.value?.data?.let {
                        it.add(user)
                        _usersLive.value = Resource.success(it)
                    }
                }
            }
            launch {
                UsersRepo.userLeftFlow.collect { user ->
                    usersLive.value?.data?.let {
                        it.remove(user)
                        _usersLive.value = Resource.success(it)
                    }
                }
            }
        }
    }

    private fun putContact(contact: Contact) {
        val contactList = _contactsLive.value ?: ContactList()
        contactList.putFirst(contact)
        _contactsLive.value = contactList
    }

    private fun updateContact(contact: Contact) {
        val contactList = _contactsLive.value ?: ContactList()
        val updated = contactList.update(contact)
        if (updated) {
            _contactsLive.value = contactList
        }
    }
}