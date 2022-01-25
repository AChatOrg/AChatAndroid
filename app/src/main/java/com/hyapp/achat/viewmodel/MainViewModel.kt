package com.hyapp.achat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.UsersRoomsRepo
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.view.EventActivity
import com.hyapp.achat.viewmodel.service.SocketService
import com.hyapp.achat.viewmodel.utils.NetUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import java.util.*

@ExperimentalCoroutinesApi
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val _contactsLive = MutableLiveData<ContactList>()
    val contactsLive = _contactsLive as LiveData<ContactList>

    private val _usersLive = MutableLiveData<Resource<SortedList<User>>>()
    val usersLive = _usersLive as LiveData<Resource<SortedList<User>>>

    private val _roomsLive = MutableLiveData<Resource<SortedList<Room>>>()
    val roomsLive = _roomsLive as LiveData<Resource<SortedList<Room>>>

    private var stopTypingJob: Job? = null
    private var refreshOnlineTimeJob: Job? = null

    private val _roomCreatedFlow = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    val roomCreatedFlow = _roomCreatedFlow.asSharedFlow()

    init {
        UserLive.value = UserDao.get(User.CURRENT_USER_ID)
        val context = getApplication<Application>().applicationContext
        SocketService.start(context, Preferences.instance().loginInfo)
        loadContacts()
        reloadUsers()
        reloadRooms()
        observeContacts()
        observeUsers()
    }

    private fun loadContacts() {
        _contactsLive.value = ContactList(ContactDao.all())
    }

    fun reloadUsers() {
        _usersLive.value = Resource.loading(null)
        viewModelScope.launch {
            UsersRoomsRepo.requestUsers().collect { userList ->
                _usersLive.value = Resource.success(userList)
            }
        }
    }

    fun reloadRooms() {
        _roomsLive.value = Resource.loading(null)
        viewModelScope.launch {
            UsersRoomsRepo.requestRooms().collect { roomList ->
                _roomsLive.value = Resource.success(roomList)
            }
        }
    }

    private fun observeContacts() {
        viewModelScope.launch {
            ChatRepo.contactFlow.collect { pair ->
                when (pair.first) {
                    ChatRepo.CONTACT_PUT -> putContact(pair.second)
                    ChatRepo.CONTACT_UPDATE -> updateContact(pair.second)
                    ChatRepo.CONTACT_TYPING -> signalTyping(pair.second)
                }
            }
        }
    }

    private fun observeUsers() {
        viewModelScope.launch {
            launch {
                UsersRoomsRepo.flow.collect { pair ->
                    when (pair.first) {
                        UsersRoomsRepo.USER_CAME -> {
                            usersLive.value?.data?.let {
                                it.add(pair.second as User)
                                _usersLive.value = Resource.success(it)
                            }
                        }
                        UsersRoomsRepo.USER_LEFT -> {
                            usersLive.value?.data?.let {
                                it.remove(pair.second as User)
                                _usersLive.value = Resource.success(it)
                            }
                        }
                        UsersRoomsRepo.ROOM_CREATE -> {
                            roomsLive.value?.data?.let {
                                it.add(pair.second as Room)
                                _roomsLive.value = Resource.success(it)
                            }
                        }
                        UsersRoomsRepo.ROOM_DELETE -> {
                            roomsLive.value?.data?.let {
                                it.remove(pair.second as Room)
                                _roomsLive.value = Resource.success(it)
                            }
                        }
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

    private fun signalTyping(contact: Contact) {
        _contactsLive.value?.let { list ->
            var updated = list.update(contact)
            if (updated) {
                _contactsLive.value = list
            }
            stopTypingJob?.cancel()
            stopTypingJob = viewModelScope.launch(ioDispatcher) {
                delay(3000)
                ContactDao.get(contact.uid)?.let {
                    updated = list.update(it.apply { typingName = null })
                    if (updated) {
                        _contactsLive.postValue(list)
                    }
                }
            }
        }
    }

    private fun refreshOnlineTimes() {
        _contactsLive.value?.let {
            it.refreshOnlineTimes()
            _contactsLive.value = it
        }
    }

    fun activityStarted() {
        if (EventActivity.startedActivities > 0) {
            ChatRepo.sendOnlineTime(true)
        }
        refreshOnlineTimeJob?.cancel()
        refreshOnlineTimeJob = viewModelScope.launch {
            while (true) {
                delay(60000)
                refreshOnlineTimes()
            }
        }
    }

    fun activityStopped() {
        if (EventActivity.startedActivities < 1) {
            ChatRepo.sendOnlineTime(false)
        }
        refreshOnlineTimeJob?.cancel()
    }

    fun createRoom(name: String, gender: Byte) {
        val context = getApplication<Application>().applicationContext
        if (!NetUtils.isNetConnected(context)) {
            _roomCreatedFlow.tryEmit(Event(Event.Status.ERROR, Event.MSG_NET))
        } else {
            val nameTrim = name.trim()
            if (nameTrim.isEmpty()) {
                _roomCreatedFlow.tryEmit(Event(Event.Status.ERROR, Event.MSG_EMPTY))
            } else {
                UserLive.value?.let { user ->
                    if (gender != UserConsts.GENDER_MIXED && user.gender != gender) {
                        _roomCreatedFlow.tryEmit(Event(Event.Status.ERROR, Event.MSG_GENDER))
                    } else {
                        _roomCreatedFlow.tryEmit(Event(Event.Status.LOADING))
                        val room = Room(nameTrim, 0, gender, emptyList(), "", 0)
                        viewModelScope.launch {
                            UsersRoomsRepo.requestCreateRoom(room).collect { isSuccess ->
                                if (isSuccess) {
                                    _roomCreatedFlow.tryEmit(Event(Event.Status.SUCCESS))
                                } else {
                                    _roomCreatedFlow.tryEmit(Event(Event.Status.ERROR))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}