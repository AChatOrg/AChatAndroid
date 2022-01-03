package com.hyapp.achat.da.socket

import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.hyapp.achat.Config
import com.hyapp.achat.da.objectbox.ContactDao
import com.hyapp.achat.model.*
import com.hyapp.achat.model.gson.MessageDeserializer
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.util.*

object ChatRepo {

    private val contactsLive: MutableLiveData<Resource<MutableList<Contact>>> by lazy {
        MutableLiveData()
    }

    fun listen(socket: Socket) {
        socket.on(Config.ON_PV_MESSAGE, onPvMessage)
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
            contact.messageTime = message.timeMillis
            if (message is TextMessage) {
                contact.message = message.text
            }
            val resource = contactsLive.value ?: Resource.success(LinkedList<Contact>())
            val contactList = resource.data ?: LinkedList<Contact>()
            val oldIndex = putAndMove(contactList, contact)
            contactsLive.postValue(Resource.add(contactList, oldIndex))
        }
    }

    fun sendPvMessage(socket: Socket, json: String?) {
        socket.emit(Config.ON_PV_MESSAGE, json)
    }

    private fun putAndMove(list: MutableList<Contact>, contact: Contact): Int {
        var oldIndex = -1
        for (i in 0 until list.size) {
            if (list[i].uid == contact.uid) {
                oldIndex = i
                break
            }
        }
        if (oldIndex == -1) {
            list.add(0, contact)
        } else {
            list[oldIndex] = contact
            Collections.swap(list, oldIndex, 0)
        }
        return oldIndex
    }
}