package com.hyapp.achat.viewmodel

import android.text.format.DateUtils
import androidx.lifecycle.*
import com.hyapp.achat.App
import com.hyapp.achat.R
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.UsersRoomsRepo
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.objectbox.MessageDao
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.view.EventActivity
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.service.SocketService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.*
import kotlin.math.max
import kotlin.math.min

@ExperimentalCoroutinesApi
class ChatViewModel(var contact: Contact) : ViewModel() {

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

    private val _contactLive = MutableLiveData<Contact>()
    val contactLive = _contactLive as LiveData<Contact>

    private val _messagesLive = MutableLiveData<Resource<MessageList>>()
    val messagesLive = _messagesLive as LiveData<Resource<MessageList>>

    private var initCount = 0L
    private var pagedCount = 0

    private var stopTypingJob: Job? = null
    private var refreshOnlineTimeJob: Job? = null

    private val messageChatType =
        when {
            contact.isUser -> Message.CHAT_TYPE_PV
            contact.isRoom -> Message.CHAT_TYPE_ROOM
            else -> Message.CHAT_TYPE_PV_ROOM
        }

    private val membersStr = App.context.getString(R.string.members)
    private val onlineStr = App.context.getString(R.string.online)

    init {
        if (UserLive.value == null) {
            UserDao.get(User.CURRENT_USER_ID)?.let {
                UserLive.value = it
            }
        }
        initCount = Preferences.instance().getContactMessagesCount(UserLive.value?.uid ?: "", contact.uid)
        _contactLive.value = contact
        contactUid = contact.uid
        if (contact.isUser || contact.isPvRoom)
            loadPagedReadMessages()
        else if (contact.isRoom) {
            loadPublicRoomMessages()
        }
        observeMessage()
        observeUserRoom()
        requestRoomMemberCount()
    }

    private fun loadPublicRoomMessages() {
        viewModelScope.launch(ioDispatcher) {
            ensureActive()
            val resource = _messagesLive.value ?: Resource.success(MessageList())
            val messageList = resource.data ?: MessageList()

            MainViewModel.publicRoomsMessageMap[contactUid]?.let { messages ->
                for (message in messages) {
                    messageList.addMessageLast(message)
                }
            }

            messageList.addFirst(
                Message(
                    uid = PROFILE_MESSAGE_UID,
                    type = Message.TYPE_PROFILE,
                    user = contact.getUser(),
                    chatType = messageChatType,
                )
            )
            _messagesLive.postValue(
                Resource.addPaging(
                    messageList,
                    0,
                    false,
                    true
                )
            )
        }
    }

    fun loadPagedReadMessages() {
        viewModelScope.launch(ioDispatcher) {
            ensureActive()
            val resource = _messagesLive.value ?: Resource.success(MessageList())
            val messageList = resource.data ?: MessageList()

            if (initCount <= 0) {
                messageList.addFirst(
                    Message(
                        uid = PROFILE_MESSAGE_UID,
                        type = Message.TYPE_PROFILE,
                        user = contact.getUser(),
                        chatType = messageChatType
                    )
                )
                _messagesLive.postValue(Resource.addPaging(messageList, 1, false, false))
                return@launch
            }
            val remaining = initCount - ++pagedCount * PAGING_LIMIT
            val hasNext = remaining > 0
            val offset = max(remaining, 0)
            val limit = min(remaining + PAGING_LIMIT, PAGING_LIMIT)

            val messages = if (contact.isPvRoom)
                MessageDao.allRoom(UserLive.value?.uid ?: "", contact.uid, offset, limit)
            else
                MessageDao.all(
                    UserLive.value?.uid ?: "",
                    (UserLive.value ?: User()).uid,
                    contact.uid,
                    offset,
                    limit
                )

            val firstMessage = messages[messages.size - 1]
            if (firstMessage.transfer == Message.TRANSFER_SEND
                || (firstMessage.transfer == Message.TRANSFER_RECEIVE &&
                        (firstMessage.delivery == Message.DELIVERY_SENT || firstMessage.delivery == Message.DELIVERY_READ))
            ) {
                messageList.addMessageFirst(firstMessage)
            }

            for (i in messages.size - 2 downTo 0) {
                val message = messages[i]
                if (message.transfer == Message.TRANSFER_SEND
                    || (message.transfer == Message.TRANSFER_RECEIVE &&
                            (message.delivery == Message.DELIVERY_SENT || message.delivery == Message.DELIVERY_READ))
                ) {
                    messageList.addMessageFirst(message)
                }
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
                        user = contact.getUser(),
                        chatType = messageChatType
                    )
                )
            }

            _messagesLive.postValue(
                Resource.addPaging(
                    messageList,
                    0,
                    hasNext,
                    pagedCount == 1
                )
            )
        }
    }

    fun loadUnreadMessages() {
        if (!contact.isRoom) {
            viewModelScope.launch(ioDispatcher) {
                ensureActive()
                _messagesLive.value?.data?.let { list ->
                    val messages = if (contact.isPvRoom)
                        MessageDao.allReceivedUnReadsRoom(
                            UserLive.value?.uid ?: "",
                            (UserLive.value ?: User()).uid,
                            contact.uid
                        )
                    else
                        MessageDao.allReceivedUnReads(UserLive.value?.uid ?: "", contact.uid)

                    for (message in messages) {
                        list.addMessageLast(message)
                    }
                    _messagesLive.postValue(Resource.addUnread(list, messages.size))
                }
            }
        }
    }

    private fun observeMessage() {
        viewModelScope.launch {
            ChatRepo.messageFlow.collect { pair ->
                when (pair.first) {
                    ChatRepo.MESSAGE_RECEIVE -> {
                        if (pair.second.isPv) {
                            if (pair.second.senderUid == contact.uid) {
                                addMessage(pair.second, true)
                            }
                        } else {
                            if (pair.second.receiverUid == contact.uid) {
                                addMessage(pair.second, true)
                            }
                        }
                    }
                    ChatRepo.MESSAGE_SENT -> {
                        if (pair.second.receiverUid == contact.uid) {
                            updateMessageTimeAndDelivery(pair.second)
                        }
                    }
                    ChatRepo.MESSAGE_READ -> {
                        if (pair.second.receiverUid == contact.uid) {
                            updateMessageDelivery(pair.second)
                        }
                    }
                    ChatRepo.MESSAGE_TYPING -> {
                        if (pair.second.receiverUid == contact.uid) {
                            signalTyping(pair.second)
                        }
                    }
                    ChatRepo.MESSAGE_ONLINE_TIME -> {
                        if (pair.second.receiverUid == contact.uid) {
                            updateContact(onlineTime = pair.second.time)
                        }
                    }
                }
            }
        }
    }

    private fun observeUserRoom() {
        viewModelScope.launch {
            launch {
                UsersRoomsRepo.flow.collect { pair ->
                    when (pair.first) {
                        UsersRoomsRepo.ROOM_MEMBER_COUNT -> {
                            val p = pair.second as Triple<String, Int, Int>
                            val roomUid = p.first
                            if (roomUid == contactUid) {
                                val memberCount = p.second
                                val onlineMemberCount = p.third
                                updateContact(
                                    memberCount = memberCount,
                                    onlineMemberCount = onlineMemberCount
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestRoomMemberCount() {
        if (!contact.isUser) {
            viewModelScope.launch {
                UsersRoomsRepo.requestRoomMemberCount(contactUid).collect { pair ->
                    val memberCount = pair.first
                    val onlineMemberCount = pair.second
                    updateContact(memberCount = memberCount, onlineMemberCount = onlineMemberCount)
                }
            }
        }
    }

    fun sendTextMessage(text: CharSequence, textSizeUnit: Int) {
        val message = Message(
            UUID.randomUUID().toString(), Message.TYPE_TEXT,
            Message.TRANSFER_SEND, System.currentTimeMillis(), text.toString(), textSizeUnit, "",
            contact.uid, UserLive.value ?: User(), messageChatType
        )

        if (message.isRoom && SocketService.ioSocket?.socket?.connected() == true)
            message.delivery = Message.DELIVERY_READ

        addMessage(message, false)
        viewModelScope.launch {
            ChatRepo.sendMessage(message, contact)
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
        _messagesLive.value?.data?.let { list ->
            val updated = list.updateMessageTimeAndDelivery(message)
            if (updated) {
                _messagesLive.value = Resource.update(list, 0)
            }
        }
    }

    private fun updateMessageDelivery(message: Message) {
        _messagesLive.value?.data?.let { list ->
            val updated = list.updateAllDeliveryUntil(message)
            if (updated) {
                _messagesLive.value = Resource.update(list, 0)
            }
        }
    }

    fun markMessageAsRead(changedMessages: List<Message>) {
        if (changedMessages.isNotEmpty()) {
            viewModelScope.launch {
                ChatRepo.markMessageAsRead(contact, changedMessages)
            }
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
        ChatRepo.sendTyping(contactUid, contact.isRoom || contact.isPvRoom)
    }

    private fun updateContact(
        onlineTime: Long? = null,
        memberCount: Int? = null,
        onlineMemberCount: Int? = null,
        avatars: List<String>? = null
    ) {
        if (onlineTime != null)
            contact.onlineTime = onlineTime

        if (memberCount != null && onlineMemberCount != null)

            contact.bio =
                "${UiUtils.formatNum(memberCount.toLong())} " + membersStr + ", ${
                    UiUtils.formatNum(onlineMemberCount.toLong())
                } " + onlineStr

        if (avatars != null)
            contact.avatars = avatars

        _contactLive.value = contact
        _messagesLive.value?.data?.let {
            val first = it.first
            if (first.type == Message.TYPE_PROFILE) {
                it[0] = first.copy(
                    senderOnlineTime = contact.onlineTime,
                    senderBio = contact.bio,
                    senderAvatars = contact.avatars
                )
                _messagesLive.value = Resource.update(it, 0)
            }
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
        isActivityStarted = true
//        viewModelScope.launch {
//            ChatRepo.clearContactNotifs(contactUid)
//        }
        refreshOnlineTimeJob?.cancel()
        refreshOnlineTimeJob = viewModelScope.launch {
            while (isActivityStarted) {
                delay(60000)
                updateContact(onlineTime = if (contact.onlineTime != Contact.TIME_ONLINE) contact.onlineTime + 1 else contact.onlineTime)
            }
        }
        if (contact.isRoom) {
            viewModelScope.launch {
                ChatRepo.clearContactNotifs(contactUid)
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
        isActivityStarted = false
        refreshOnlineTimeJob?.cancel()
    }

    fun onActivityCreate() {
        if (!contact.isUser) {
            viewModelScope.launch(ioDispatcher) {
                if (ContactDao.get(UserLive.value?.uid ?: "", contactUid) == null) {
                    ChatRepo.sendJoinLeaveRoom(contactUid, true)
                }
            }
        }
    }

    fun onActivityDestroy() {
        if (!contact.isUser) {
            viewModelScope.launch(ioDispatcher) {
                if (ContactDao.get(UserLive.value?.uid ?: "", contactUid) == null) {
                    ChatRepo.sendJoinLeaveRoom(contactUid, false)
                }
            }
        }
    }

    class Factory(private var contact: Contact) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(contact) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}