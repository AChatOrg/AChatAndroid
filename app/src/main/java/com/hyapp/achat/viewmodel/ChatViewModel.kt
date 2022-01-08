package com.hyapp.achat.viewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.CurrentUserLive
import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.model.entity.TextMessage
import com.hyapp.achat.model.event.MessageEvent
import com.hyapp.achat.model.gson.MessageDeserializer
import org.greenrobot.eventbus.EventBus
import java.util.*

class ChatViewModel : ViewModel() {

    private lateinit var receiver: Contact

    fun init(receiver: Contact) {
        this.receiver = receiver
    }

    fun sendAndGetPvTextMessage(text: CharSequence, textSizeUnit: Int): Message {
        val message = TextMessage(Message.TRANSFER_TYPE_SEND, System.currentTimeMillis(), UUID.randomUUID().toString(), CurrentUserLive.value
                ?: Contact(), receiver.uid, text.toString(), textSizeUnit)
        EventBus.getDefault().post(MessageEvent(message, MessageEvent.ACTION_SEND, receiver))
        return message
    }

    fun setupAndGetReceiveMessage(json: String): Message {
        return GsonBuilder()
                .registerTypeAdapter(Message::class.java, MessageDeserializer())
                .create()
                .fromJson(json, Message::class.java)
                .apply {
                    transferType = Message.TRANSFER_TYPE_RECEIVE
                }
    }
}