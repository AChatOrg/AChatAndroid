package com.hyapp.achat.viewmodel

import android.text.format.DateUtils
import androidx.lifecycle.*
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.MessageDao
import com.hyapp.achat.model.objectbox.UserDao
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
    }

    private val currentUser = UserDao.get(User.CURRENT_USER_ID)

    private val _messagesLive = MutableLiveData<Resource<MessageList>>()
    val messagesLive = _messagesLive as LiveData<Resource<MessageList>>

    private val initCount = Preferences.instance().getContactMessagesCount(receiver.uid)
    private var pagedCount = 0

    init {
        contactUid = receiver.uid
        loadPagedMessages()
        observeMessage()
    }

    fun loadPagedMessages() {
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
            _messagesLive.value = Resource.addPaging(messageList, 1, false, false)
            return
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

        _messagesLive.value =
            Resource.addPaging(messageList, messageList.size - oldSize, hasNext, pagedCount == 1)
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
        ChatRepo.sendPvMessage(message, receiver)
    }

    private fun addMessage(message: Message, received: Boolean) {
        val resource = _messagesLive.value ?: Resource.success(MessageList())
        val messageList = resource.data ?: MessageList()
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

    fun readMessage(message: Message) {
        ChatRepo.updateAndSendMessageRead(message.apply { delivery = Message.DELIVERY_SENT })
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

    fun activityStarted() {
        isActivityStarted = true
    }

    fun activityStopped() {
        isActivityStarted = false
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