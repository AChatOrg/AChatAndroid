package com.hyapp.achat.viewmodel

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.util.*
import kotlin.collections.HashMap
import kotlin.contracts.contract

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

    companion object {
        private const val PUBLIC_ROOM_MESSAGES_CAPACITY = 200

        val publicRoomsMessageMap = HashMap<String, CircularFifoQueue<Message>>()

        fun addPublicRoomUnreadMessage(roomUid: String, message: Message) {
            var messages = publicRoomsMessageMap[roomUid]
            if (messages == null) {
                messages = CircularFifoQueue(PUBLIC_ROOM_MESSAGES_CAPACITY)
                publicRoomsMessageMap[roomUid] = messages
            }
            messages.add(message)
        }
    }

    init {
        if (UserLive.value == null) {
            UserDao.get(User.CURRENT_USER_ID)?.let {
                UserLive.value = it
            }
        }
        val context = getApplication<Application>().applicationContext
        SocketService.start(context, Preferences.instance().loginInfo)
        loadContacts()
        observeContacts()
        observeUsersRooms()
    }

    private fun loadContacts() {
        _contactsLive.value =
            ContactList((ContactDao.all(UserLive.value?.uid ?: "") as MutableList).apply {
                if (isEmpty()) {
                    add(Contact(type = Contact.TYPE_EMPTY))
                }
            })
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

    private fun observeUsersRooms() {
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
                        UsersRoomsRepo.USER_UPDATE -> {
                            val user = pair.second as User
                            updateUser(user)
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
                        UsersRoomsRepo.ROOM_MEMBER_COUNT -> {
                            val p = pair.second as Triple<String, Int, Int>
                            val roomUid = p.first
                            val memberCount = p.second
                            val onlineMemberCount = p.third
                            updateRoom(roomUid, memberCount, onlineMemberCount)
                        }
                    }
                }
            }
        }
    }

    private fun updateRoom(roomUid: String, memberCount: Int, onlineMemberCount: Int) {
        viewModelScope.launch(ioDispatcher) {
            _roomsLive.value?.data?.let { list ->
                for (i in 0 until list.size) {
                    val room = list[i]
                    if (room.uid == roomUid) {
                        list[i] = room.copy(
                            memberCount = memberCount,
                            onlineMemberCount = onlineMemberCount
                        )
                        _roomsLive.postValue(Resource.update(list, 0))
                        return@launch
                    }
                }
            }
        }
    }

    private fun updateUser(user: User) {
        viewModelScope.launch(ioDispatcher) {
            _usersLive.value?.data?.let { list ->
                for (i in 0 until list.size) {
                    val u = list[i]
                    if (u.uid == user.uid) {
                        list[i] = user
                        _usersLive.postValue(Resource.update(list, 0))
                        return@launch
                    }
                }
            }
        }
    }

    private fun putContact(contact: Contact) {
        val contactList = _contactsLive.value ?: ContactList()
        if (contactList.first.type == Contact.TYPE_EMPTY) {
            contactList.removeFirst()
        }
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
                ContactDao.get(UserLive.value?.uid ?: "", contact.uid)?.let {
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
            viewModelScope.launch(ioDispatcher) {
                ChatRepo.sendOnlineTime(true)
                UserLive.postValue(UserLive.value?.apply { onlineTime = UserConsts.TIME_ONLINE })
                UserLive.value?.let {
                    UserDao.put(it.apply { id = User.CURRENT_USER_ID })
                }
            }
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
            viewModelScope.launch(ioDispatcher) {
                ChatRepo.sendOnlineTime(false)
                UserLive.postValue(UserLive.value?.apply {
                    onlineTime = System.currentTimeMillis()
                })
                UserLive.value?.let {
                    UserDao.put(it.apply { id = User.CURRENT_USER_ID })
                }
            }
        }
        refreshOnlineTimeJob?.cancel()
    }

    fun createRoom(name: String, gender: Byte): Flow<Event> = callbackFlow {
        val context = getApplication<Application>().applicationContext
        if (!NetUtils.isNetConnected(context)) {
            trySend(Event(Event.Status.ERROR, Event.MSG_NET))
        } else {
            val nameTrim = name.trim()
            if (nameTrim.isEmpty()) {
                trySend(Event(Event.Status.ERROR, Event.MSG_EMPTY))
            } else {
                UserLive.value?.let { user ->
                    if (gender != UserConsts.GENDER_MIXED && user.gender != gender) {
                        trySend(Event(Event.Status.ERROR, Event.MSG_MATCH))
                    } else {
                        trySend(Event(Event.Status.LOADING))
                        val room = Room(nameTrim, 0, gender, emptyList(), "", 0)
                        viewModelScope.launch {
                            UsersRoomsRepo.requestCreateRoom(room).collect { isSuccess ->
                                if (isSuccess) {
                                    trySend(Event(Event.Status.SUCCESS))
                                } else {
                                    trySend(Event(Event.Status.ERROR))
                                }
                            }
                        }
                    }
                }
            }
        }
        awaitClose()
    }
}