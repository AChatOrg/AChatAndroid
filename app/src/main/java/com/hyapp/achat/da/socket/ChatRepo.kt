package com.hyapp.achat.da.socket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.hyapp.achat.Config
import com.hyapp.achat.da.objectbox.ContactDao
import com.hyapp.achat.model.*
import com.hyapp.achat.model.event.MessageEvent
import com.hyapp.achat.model.gson.InterfaceAdapter
import com.hyapp.achat.model.gson.MessageDeserializer
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.util.*

object ChatRepo {

    private val _contactsLive = MutableLiveData<Resource<MutableList<Contact>>>()

    fun listen(socket: Socket) {
        socket.on(Config.ON_PV_MESSAGE, onPvMessage)
        val contacts = ContactDao.all
        _contactsLive.value = Resource.add(contacts as MutableList<Contact>, Resource.INDEX_ALL)
    }

    fun sendPvMessage(socket: Socket, event: MessageEvent) {
        val json = GsonBuilder()
                .registerTypeAdapter(TextMessage::class.java, InterfaceAdapter<TextMessage>())
                .create()
                .toJson(event.message)

        val contact = ContactDao.get(event.receiver!!.uid) ?: event.receiver
        contact!!.messageDelivery = ChatMessage.DELIVERY_WAITING
        setupAndPutContact(contact, event.message as ChatMessage)

        socket.emit(Config.ON_PV_MESSAGE, json)
    }

    private val onPvMessage = Emitter.Listener { args: Array<Any> ->
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
        val resource = _contactsLive.value ?: Resource.success(LinkedList<Contact>())
        val contactList = resource.data ?: LinkedList<Contact>()
        val oldIndex = putAndMove(contactList, contact)
        _contactsLive.postValue(Resource.add(contactList, oldIndex))
        ContactDao.put(contact)
    }

    private fun putAndMove(list: MutableList<Contact>, contact: Contact): Int {
        var oldIndex = Resource.INDEX_NEW
        for (i in 0 until list.size) {
            if (list[i].uid == contact.uid) {
                oldIndex = i
                break
            }
        }
        if (oldIndex != Resource.INDEX_NEW)
            list.removeAt(oldIndex)
        list.add(0, contact)
        return oldIndex
    }

    val contactsLive: LiveData<Resource<MutableList<Contact>>>
        get() = _contactsLive
}