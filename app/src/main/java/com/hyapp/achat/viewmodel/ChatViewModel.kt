package com.hyapp.achat.viewmodel

import android.text.format.DateUtils
import androidx.lifecycle.*
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.MessageDao
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ChatViewModel(var receiver: User) : ViewModel() {

    companion object {
        const val PAGING_LIMIT: Long = 50
        const val PROFILE_MESSAGE_UID = "profile"
    }

    private val _messagesLive = MutableLiveData<Resource<MessageList>>()
    val messagesLive = _messagesLive as LiveData<Resource<MessageList>>

    private val initCount = Preferences.instance().getContactMessagesCount(receiver.uid)
    private var pagedCount = 0

    init {
        loadPagedMessages()
        observeReceivedMessage()
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

    private fun observeReceivedMessage() {
        viewModelScope.launch {
            ChatRepo.receiveMessageFlow.collect { message ->
                if (message.senderUid == receiver.uid) {
                    addMessage(message)
                }
            }
        }
    }

    fun sendPvTextMessage(text: CharSequence, textSizeUnit: Int) {
        val message = Message(
            UUID.randomUUID().toString(), Message.TYPE_TEXT,
            Message.TRANSFER_SEND, System.currentTimeMillis(), text.toString(), textSizeUnit, "",
            receiver.uid, UserLive.value ?: User()
        )
        addMessage(message)
        ChatRepo.sendPvMessage(message, receiver)
    }

    private fun addMessage(message: Message) {
        val resource = _messagesLive.value ?: Resource.success(MessageList())
        val messageList = resource.data ?: MessageList()
        messageList.addMessageLast(message)
        _messagesLive.value = Resource.add(messageList, 0)
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