package com.hyapp.achat.bl

import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.hyapp.achat.model.*
import com.hyapp.achat.model.event.MessageEvent
import com.hyapp.achat.model.gson.InterfaceAdapter
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