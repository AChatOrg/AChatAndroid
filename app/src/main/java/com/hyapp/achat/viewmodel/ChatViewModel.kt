package com.hyapp.achat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.entity.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel : ViewModel() {

    lateinit var receiver: Contact

    private val _messagesLive = MutableLiveData<Resource<LinkedList<Message>>>()
    val messagesLive = _messagesLive as LiveData<Resource<LinkedList<Message>>>

    init {
        observeReceivedMessage()
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
        val message = TextMessage(Message.TRANSFER_TYPE_SEND, System.currentTimeMillis(), UUID.randomUUID().toString(), CurrentUserLive.value
                ?: Contact(), receiver.uid, text.toString(), textSizeUnit)
        addMessage(message)
        ChatRepo.sendPvMessage(message, receiver)
    }

    private fun addMessage(message: Message) {
        val resource = _messagesLive.value ?: Resource.success(LinkedList<Message>())
        val messageList = resource.data ?: LinkedList<Message>()
        messageList.add(message)
        _messagesLive.value = Resource.add(messageList, 0)
    }
}