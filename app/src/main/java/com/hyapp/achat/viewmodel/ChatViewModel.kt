package com.hyapp.achat.viewmodel

import androidx.lifecycle.*
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.entity.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel(var receiver: Contact) : ViewModel() {

    private val _messagesLive = MutableLiveData<Resource<MessageList>>()
    val messagesLive = _messagesLive as LiveData<Resource<MessageList>>

    init {
        loadMessages()
        observeReceivedMessage()
    }

    private fun loadMessages() {
        val messageList = MessageList()
        messageList.addFirst(ProfileMessage(receiver))
        _messagesLive.value = Resource.add(messageList, Resource.INDEX_ALL)
    }

    private fun observeReceivedMessage() {
        viewModelScope.launch {
            ChatRepo.receiveMessageFlow.collect { message ->
                if (message is ChatMessage) {
                    if (message.sender.uid == receiver.uid) {
                        addMessage(message)
                    }
                }
            }
        }
    }

    fun sendPvTextMessage(text: CharSequence, textSizeUnit: Int) {
        val message = TextMessage(UUID.randomUUID().toString(), Message.TRANSFER_SEND, System.currentTimeMillis(), CurrentUserLive.value
                ?: Contact(), receiver.uid, text.toString(), textSizeUnit)
        addMessage(message)
        ChatRepo.sendPvMessage(message, receiver)
    }

    private fun addMessage(message: Message) {
        val resource = _messagesLive.value ?: Resource.success(MessageList())
        val messageList = resource.data ?: MessageList()
        val pair = messageList.addMessage(message)
        _messagesLive.value = Resource.add(messageList, pair.second.toInt(), pair.first)
    }

    class Factory(private var receiver: Contact) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(receiver) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}