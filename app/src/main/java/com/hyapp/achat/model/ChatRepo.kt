package com.hyapp.achat.model

import com.google.gson.GsonBuilder
import com.hyapp.achat.Config
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.gson.InterfaceAdapter
import com.hyapp.achat.model.gson.MessageDeserializer
import com.hyapp.achat.viewmodel.service.SocketService
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*

object ChatRepo {

    private val _contactFlow = MutableSharedFlow<Contact>(extraBufferCapacity = 1)
    val contactFlow = _contactFlow.asSharedFlow()

    fun listen(socket: Socket) {
        socket.on(Config.ON_PV_MESSAGE, onPvMessage)
    }

    fun sendPvMessage(message: ChatMessage, receiver: Contact) {
        val json = GsonBuilder()
                .registerTypeAdapter(TextMessage::class.java, InterfaceAdapter<TextMessage>())
                .create()
                .toJson(message)

        val contact = ContactDao.get(receiver.uid) ?: receiver
        contact.messageDelivery = ChatMessage.DELIVERY_WAITING
        setupAndPutContact(contact, message)

        SocketService.ioSocket?.socket?.emit(Config.ON_PV_MESSAGE, json)
    }

    private val onPvMessage = Emitter.Listener { args ->
        val message = GsonBuilder()
                .registerTypeAdapter(Message::class.java, MessageDeserializer())
                .create()
                .fromJson(args[0].toString(), Message::class.java)
                .apply {
                    transferType = Message.TRANSFER_TYPE_RECEIVE
                }
        if (message is ChatMessage) {
            val contact = ContactDao.get(message.sender.uid) ?: message.sender
            contact.messageDelivery = ChatMessage.DELIVERY_HIDDEN
            setupAndPutContact(contact, message)
        }
    }

    private fun setupAndPutContact(contact: Contact, message: ChatMessage) {
        contact.messageTime = message.timeMillis
        if (message is TextMessage) {
            contact.message = message.text
        }
        ContactDao.put(contact)
        _contactFlow.tryEmit(contact)
    }
}