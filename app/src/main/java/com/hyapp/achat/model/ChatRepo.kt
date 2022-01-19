package com.hyapp.achat.model

import com.google.gson.Gson
import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.entity.UserLive
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.objectbox.MessageDao
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.viewmodel.service.SocketService
import io.objectbox.exception.UniqueViolationException
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ChatRepo {

    const val CONTACT_PUT: Byte = 1
    const val CONTACT_UPDATE: Byte = 2

    const val MESSAGE_RECEIVE: Byte = 1
    const val MESSAGE_SENT: Byte = 2
    const val MESSAGE_READ: Byte = 3

    private val _contactFlow =
        MutableSharedFlow<Pair<Byte, Contact>>(extraBufferCapacity = Int.MAX_VALUE)
    val contactFlow = _contactFlow.asSharedFlow()

    private val _messageFlow =
        MutableSharedFlow<Pair<Byte, Message>>(extraBufferCapacity = Int.MAX_VALUE)
    val messageFlow = _messageFlow.asSharedFlow()


    fun listen(socket: Socket) {
        socket.on(Config.ON_PV_MESSAGE, onPvMessage)
        socket.on(Config.ON_MESSAGE_SENT, onMessageSent)
        socket.on(Config.ON_MSG_READ, onMessageRead)
        socket.on(Config.ON_MSG_READ_RECEIVED, onMessageReadReceived)
    }

    fun sendPvMessage(message: Message, receiver: User) {
        val json = Gson().toJson(message)

        val contact = ContactDao.get(receiver.uid) ?: Contact(receiver)
        contact.messageDelivery = Message.DELIVERY_WAITING
        setupAndPutContact(contact, message)
        MessageDao.put(message.apply { id = 0 })
        Preferences.instance().incrementContactMessagesCount(contact.uid)

        SocketService.ioSocket?.socket?.emit(Config.ON_PV_MESSAGE, json)
    }

    fun sendWaitingsMessages() {
        (UserLive.value ?: UserDao.get(User.CURRENT_USER_ID))?.let {
            val messages = MessageDao.waitings(it.uid)
            for (message in messages) {
                val json = Gson().toJson(message)
                SocketService.ioSocket?.socket?.emit(Config.ON_PV_MESSAGE, json)
            }
        }
    }

    fun sendReadsMessages() {
        (UserLive.value ?: UserDao.get(User.CURRENT_USER_ID))?.let {
            val messages = MessageDao.reads(it.uid)
            for (message in messages) {
                SocketService.ioSocket?.socket?.emit(
                    Config.ON_MSG_READ,
                    message.uid,
                    message.senderUid
                )
            }
        }
    }

    fun updateAndSendMessageRead(message: Message) {
        MessageDao.get(message.uid)?.let {
            MessageDao.put(it.apply { delivery = message.delivery })
        }
        SocketService.ioSocket?.socket?.emit(Config.ON_MSG_READ, message.uid, message.senderUid)
    }

//    fun sendMessageRead(message: Message) {
//        SocketService.ioSocket?.socket?.emit(Config.ON_MSG_READ, message.uid, message.senderUid)
//    }

    private val onPvMessage = Emitter.Listener { args ->
        val message = Gson().fromJson(args[0].toString(), Message::class.java).apply {
            transfer = Message.TRANSFER_RECEIVE
            delivery = Message.DELIVERY_WAITING
        }
        try {
            MessageDao.put(message.apply { id = 0 })
            val contact = ContactDao.get(message.senderUid) ?: message.getContact()
            contact.messageDelivery = Message.DELIVERY_HIDDEN
            setupAndPutContact(contact, message)
            Preferences.instance().incrementContactMessagesCount(contact.uid)
            _messageFlow.tryEmit(Pair(MESSAGE_RECEIVE, message))
        } catch (e: UniqueViolationException) {
            e.printStackTrace()
        } finally {
            SocketService.ioSocket?.socket?.emit(Config.ON_MSG_RECEIVED, message.uid)
        }
    }

    private val onMessageSent = Emitter.Listener { args ->
        MessageDao.get(args[0].toString())?.let { message ->
            message.delivery = Message.DELIVERY_SENT
            ContactDao.get(message.receiverUid)?.let { contact ->
                contact.messageTime = message.time
                contact.messageDelivery = message.delivery
                ContactDao.put(contact)
                _contactFlow.tryEmit(Pair(CONTACT_UPDATE, contact))
            }
            MessageDao.put(message)
            _messageFlow.tryEmit(Pair(MESSAGE_SENT, message))
        }
    }

    private val onMessageRead = Emitter.Listener { args ->
        MessageDao.get(args[0].toString())?.let { message ->
            message.delivery = Message.DELIVERY_READ
            ContactDao.get(message.receiverUid)?.let { contact ->
                contact.messageDelivery = message.delivery
                ContactDao.put(contact)
                _contactFlow.tryEmit(Pair(CONTACT_UPDATE, contact))
            }
            MessageDao.put(message)
            _messageFlow.tryEmit(Pair(MESSAGE_READ, message))
            SocketService.ioSocket?.socket?.emit(Config.ON_MSG_READ_RECEIVED, message.uid)
        }
    }

    private val onMessageReadReceived = Emitter.Listener { args ->
        MessageDao.get(args[0].toString())?.let { message ->
            MessageDao.put(message.apply { delivery = Message.DELIVERY_READ })
        }
    }

    private fun setupAndPutContact(contact: Contact, message: Message) {
        contact.messageTime = message.time
        if (message.type == Message.TYPE_TEXT) {
            contact.message = message.text
        }
        ContactDao.put(contact)
        _contactFlow.tryEmit(Pair(CONTACT_PUT, contact))
    }
}