package com.hyapp.achat.model

import com.google.gson.GsonBuilder
import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.gson.MessageAdapter
import com.hyapp.achat.model.objectbox.MessageDao
import com.hyapp.achat.viewmodel.service.SocketService
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ChatRepo {

    private val _contactFlow = MutableSharedFlow<Contact>(extraBufferCapacity = 1)
    val contactFlow = _contactFlow.asSharedFlow()

    private val _receiveMessageFlow = MutableSharedFlow<Message>(extraBufferCapacity = 1)
    val receiveMessageFlow = _receiveMessageFlow.asSharedFlow()

    fun listen(socket: Socket) {
        socket.on(Config.ON_PV_MESSAGE, onPvMessage)
    }

    fun sendPvMessage(message: Message, receiver: Contact) {
        val json = GsonBuilder()
                .registerTypeAdapter(Message::class.java, MessageAdapter())
                .create()
                .toJson(message)

        val contact = ContactDao.get(receiver.uid) ?: receiver
        contact.messageDelivery = Message.DELIVERY_WAITING
        setupAndPutContact(contact, message)
        MessageDao.put(message)

        SocketService.ioSocket?.socket?.emit(Config.ON_PV_MESSAGE, json)
    }

    private val onPvMessage = Emitter.Listener { args ->
        val message = GsonBuilder()
                .registerTypeAdapter(Message::class.java, MessageAdapter())
                .create()
                .fromJson(args[0].toString(), Message::class.java)
                .apply {
                    transfer = Message.TRANSFER_RECEIVE
                }
        val contact = ContactDao.get(message.sender.target.uid) ?: message.sender.target
        contact.messageDelivery = Message.DELIVERY_HIDDEN
        setupAndPutContact(contact, message)
        message.sender.target = contact
        MessageDao.put(message)

        _receiveMessageFlow.tryEmit(message)
    }

    private fun setupAndPutContact(contact: Contact, message: Message) {
        contact.messageTime = message.time
        if (message.type == Message.TYPE_TEXT) {
            contact.message = message.text
        }
        ContactDao.put(contact)
        _contactFlow.tryEmit(contact)
    }
}