package com.hyapp.achat.viewmodel

import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.*
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.MessageDao
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.view.EventActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ChatViewModel(var receiver: User) : ViewModel() {

    companion object {
        const val PAGING_LIMIT: Long = 50
        const val PROFILE_MESSAGE_UID = "profile"

        @JvmStatic
        var isActivityStarted = false

        @JvmStatic
        var contactUid = ""

        @JvmStatic
        fun isActivityStoppedForContact(uid: String): Boolean {
            return !isActivityStarted || contactUid != uid
        }
    }

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val currentUser = UserDao.get(User.CURRENT_USER_ID)

    private val _contactLive = MutableLiveData<Contact>()
    val contactLive = _contactLive as LiveData<Contact>

    private val _messagesLive = MutableLiveData<Resource<MessageList>>()
    val messagesLive = _messagesLive as LiveData<Resource<MessageList>>

    private val initCount = Preferences.instance().getContactMessagesCount(receiver.uid)
    private var pagedCount = 0

    private var stopTypingJob: Job? = null
    private var refreshOnlineTimeJob: Job? = null

    init {
        _contactLive.value = Contact(receiver)
        contactUid = receiver.uid
        loadPagedMessages()
        observeMessage()
    }

    fun loadPagedMessages() {
        viewModelScope.launch(ioDispatcher) {
            ensureActive()
            val resource = _messagesLive.value ?: Resource.success(MessageList())
            val messageList = resource.data ?: MessageList()

            if (initCount <= 0) {
                messageList.addFirst(
                    Message(
                        uid = PROFILE_MESSAGE_UID,
                        type = Message.TYPE_PROFILE,
                        user = receiver
                    )
                )
                _messagesLive.postValue(Resource.addPaging(messageList, 1, false, false))
                return@launch
            }
            val remaining = initCount - ++pagedCount * PAGING_LIMIT
            val hasNext = remaining > 0
            val offset = max(remaining, 0)
            val limit = min(remaining + PAGING_LIMIT, PAGING_LIMIT)
            val messages = MessageDao.all(receiver.uid, offset, limit)

            val oldSize = messageList.size
            messageList.addMessageFirst(messages[messages.size - 1])

            for (i in messages.size - 2 downTo 0) {
                messageList.addMessageFirst(messages[i])
            }
            if (!hasNext) {
                val details = DateUtils.getRelativeTimeSpanString(
                    messages[0].time,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString()
                val detailsMessage = Message(
                    uid = UUID.randomUUID().toString(),
                    type = Message.TYPE_DETAILS,
                    time = messages[0].time,
                    text = details
                )
                messageList.addFirst(detailsMessage)
                messageList.addFirst(
                    Message(
                        uid = PROFILE_MESSAGE_UID,
                        type = Message.TYPE_PROFILE,
                        user = receiver
                    )
                )
            }

            _messagesLive.postValue(
                Resource.addPaging(
                    messageList,
                    messageList.size - oldSize,
                    hasNext,
                    pagedCount == 1
                )
            )
        }
    }

    private fun observeMessage() {
        viewModelScope.launch {
            ChatRepo.messageFlow.collect { pair ->
                when (pair.first) {
                    ChatRepo.MESSAGE_RECEIVE -> {
                        if (pair.second.senderUid == receiver.uid) {
                            addMessage(pair.second, true)
                        }
                    }
                    ChatRepo.MESSAGE_SENT -> {
                        if (pair.second.receiverUid == receiver.uid) {
                            updateMessageTimeAndDelivery(pair.second)
                        }
                    }
                    ChatRepo.MESSAGE_READ -> {
                        if (pair.second.receiverUid == receiver.uid) {
                            updateMessageDelivery(pair.second)
                        }
                    }
                    ChatRepo.MESSAGE_TYPING -> {
                        if (pair.second.receiverUid == receiver.uid) {
                            signalTyping(pair.second)
                        }
                    }
                    ChatRepo.MESSAGE_ONLINE_TIME -> {
                        if (pair.second.receiverUid == receiver.uid) {
                            updateOnlineTime(pair.second.time)
                        }
                    }
                }
            }
        }
    }

    fun sendPvTextMessage(text: CharSequence, textSizeUnit: Int) {
        val message = Message(
            UUID.randomUUID().toString(), Message.TYPE_TEXT,
            Message.TRANSFER_SEND, System.currentTimeMillis(), text.toString(), textSizeUnit, "",
            receiver.uid, currentUser ?: User()
        )
        addMessage(message, false)
        viewModelScope.launch {
            ChatRepo.sendPvMessage(message, receiver)
        }
    }

    private fun addMessage(message: Message, received: Boolean) {
        val resource = _messagesLive.value ?: Resource.success(MessageList())
        val messageList = resource.data ?: MessageList()
        if (messageList.last.type == Message.TYPE_TYPING) {
            messageList.removeLast()
        }
        messageList.addMessageLast(message)
        _messagesLive.value = Resource.add(messageList, 0, received)
    }

    private fun updateMessageTimeAndDelivery(message: Message) {
        val resource = _messagesLive.value ?: Resource.success(MessageList())
        val messageList = resource.data ?: MessageList()
        val updated = messageList.updateMessageTimeAndDelivery(message)
        if (updated) {
            _messagesLive.value = Resource.update(messageList, 0)
        }
    }

    private fun updateMessageDelivery(message: Message) {
        val resource = _messagesLive.value ?: Resource.success(MessageList())
        val messageList = resource.data ?: MessageList()
        val updated = messageList.updateMessageDelivery(message)
        if (updated) {
            _messagesLive.value = Resource.update(messageList, 0)
        }
    }

    fun markMessageAsRead(message: Message) {
        viewModelScope.launch {
            ChatRepo.markMessageAsRead(message)
        }
    }

    private fun signalTyping(message: Message) {
        val resource = _messagesLive.value ?: Resource.success(MessageList())
        val messageList = resource.data ?: MessageList()


        if (messageList.last.type != Message.TYPE_TYPING) {
            messageList.addLast(message)
            _messagesLive.value = Resource.add(messageList, 0, true)
        }

        stopTypingJob?.cancel()
        stopTypingJob = viewModelScope.launch {
            delay(3000)
            if (messageList.last.type == Message.TYPE_TYPING) {
                messageList.removeLast()
                _messagesLive.value = Resource.update(messageList, 0)
            }
        }
    }

//    fun readMessagesUntilPosition(position: Int) {
//        val resource = _messagesLive.value ?: Resource.success(MessageList())
//        val messageList = resource.data ?: MessageList()
//        val messages = mutableListOf<Message>()
//        for (i in position downTo 0) {
//            val message = messageList[i]
//            if (message.transfer == Message.TRANSFER_RECEIVE) {
//                if (message.delivery != Message.DELIVERY_READ) {
//                    message.delivery = Message.DELIVERY_READ
//                    ChatRepo.sendMessageRead(message)
//                    MessageDao.get(message.uid)?.let { messages.add(message) }
//                } else {
//                    break
//                }
//            }
//        }
//        MessageDao.put(messages)
//    }

    fun sendTyping() {
        ChatRepo.sendTyping(contactUid)
    }

    private fun updateOnlineTime(time: Long) {
        receiver.onlineTime = time
        _contactLive.value = Contact(receiver)
        _messagesLive.value?.data?.let {
            val first = it.first
            if (first.type == Message.TYPE_PROFILE) {
                it[0] = first.copy(senderOnlineTime = receiver.onlineTime)
                _messagesLive.value = Resource.update(it, 0)
            }
        }
    }

    fun activityStarted() {
        if (EventActivity.startedActivities > 0) {
            ChatRepo.sendOnlineTime(true)
        }
        isActivityStarted = true
        viewModelScope.launch {
            ChatRepo.clearContactNotifs(contactUid)
        }
        refreshOnlineTimeJob?.cancel()
        refreshOnlineTimeJob = viewModelScope.launch {
            while (isActivityStarted) {
                delay(60000)
                updateOnlineTime(if (receiver.onlineTime != Contact.TIME_ONLINE) receiver.onlineTime + 1 else receiver.onlineTime)
            }
        }
    }

    fun activityStopped() {
        if (EventActivity.startedActivities < 1) {
            ChatRepo.sendOnlineTime(false)
        }
        isActivityStarted = false
        refreshOnlineTimeJob?.cancel()
    }

    class Factory(private var receiver: User) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(receiver) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}