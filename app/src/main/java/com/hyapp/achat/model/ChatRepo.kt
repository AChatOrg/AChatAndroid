package com.hyapp.achat.model

import com.google.gson.Gson
import com.hyapp.achat.App
import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.objectbox.MessageDao
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.viewmodel.ChatViewModel
import com.hyapp.achat.viewmodel.Notifs
import com.hyapp.achat.viewmodel.service.SocketService
import io.objectbox.exception.UniqueViolationException
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

object ChatRepo {

    const val CONTACT_PUT: Byte = 1
    const val CONTACT_UPDATE: Byte = 2
    const val CONTACT_TYPING: Byte = 3

    const val MESSAGE_RECEIVE: Byte = 1
    const val MESSAGE_SENT: Byte = 2
    const val MESSAGE_READ: Byte = 3
    const val MESSAGE_TYPING: Byte = 4
    const val MESSAGE_ONLINE_TIME: Byte = 5

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val mutex = Mutex()

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
        socket.on(Config.ON_TYPING, onTyping)
        socket.on(Config.ON_ONLINE_TIME, onOnlineTime)
        socket.on(Config.ON_ONLINE_TIME_CONTACTS, onOnlineTimeContact)
    }

    suspend fun sendPvMessage(message: Message, receiver: User) {
        withContext(ioDispatcher) {
            ensureActive()
            mutex.withLock {
                val json = Gson().toJson(message)

                message.id = MessageDao.put(message.apply { id = 0 })
                val contact = ContactDao.get(receiver.uid) ?: Contact(receiver)
                contact.messageDelivery = Message.DELIVERY_WAITING
                setupAndPutContact(contact, message)
                Preferences.instance().incrementContactMessagesCount(contact.uid)

                SocketService.ioSocket?.socket?.emit(Config.ON_PV_MESSAGE, json)
            }
        }
    }

    suspend fun sendWaitingsMessages() {
        withContext(ioDispatcher) {
            ensureActive()
            (UserLive.value ?: UserDao.get(User.CURRENT_USER_ID))?.let {
                val messages = MessageDao.waitings(it.uid)
                for (message in messages) {
                    val json = Gson().toJson(message)
                    SocketService.ioSocket?.socket?.emit(Config.ON_PV_MESSAGE, json)
                }
            }
        }
    }

    suspend fun sendReadsMessages() {
        withContext(ioDispatcher) {
            ensureActive()
            (UserLive.value ?: UserDao.get(User.CURRENT_USER_ID))?.let {
                val messages = MessageDao.allSentUnReads(it.uid)
                for (message in messages) {
                    SocketService.ioSocket?.socket?.emit(
                        Config.ON_MSG_READ,
                        message.uid,
                        message.senderUid
                    )
                }
            }
        }
    }

    suspend fun markMessageAsRead(contact: Contact, changedMessages: List<Message>) {
        withContext(ioDispatcher) {
            ensureActive()
            mutex.withLock {
                ContactDao.get(contact.uid)?.let {
                    var count = it.notifCount.toInt()
                    count = max(count - changedMessages.size, 0)
                    it.notifCount = count.toString()
                    ContactDao.put(it)
                    _contactFlow.tryEmit(Pair(CONTACT_UPDATE, it))
                }

                MessageDao.put(changedMessages)
                Notifs.remove(App.context, contact.id.toInt())
                SocketService.ioSocket?.socket?.emit(
                    Config.ON_MSG_READ,
                    changedMessages[0].uid,
                    contact.uid
                )
            }
        }
    }

//    fun sendMessageRead(message: Message) {
//        SocketService.ioSocket?.socket?.emit(Config.ON_MSG_READ, message.uid, message.senderUid)
//    }

//    suspend fun clearContactNotifs(contactUid: String) {
//        withContext(ioDispatcher) {
//            ContactDao.get(contactUid)?.let {
//                it.notifCount = "0"
//                ContactDao.put(it)
//                _contactFlow.tryEmit(Pair(CONTACT_UPDATE, it))
//            }
//        }
//    }

    fun sendTyping(receiverUid: String) {
        SocketService.ioSocket?.socket?.emit(Config.ON_TYPING, receiverUid)
    }

    fun sendOnlineTime(isOnline: Boolean) {
        SocketService.ioSocket?.socket?.emit(Config.ON_ONLINE_TIME, isOnline)
    }

    suspend fun sendOnlineTimeContactsRequest() {
        withContext(ioDispatcher) {
            val contacts = ContactDao.all()
            val list = ArrayList<String>()
            for (contact in contacts) {
                list.add(contact.uid)
            }
            SocketService.ioSocket?.socket?.emit(Config.ON_ONLINE_TIME_CONTACTS, JSONArray(list))
        }
    }

    private fun setupAndPutContact(contact: Contact, message: Message) {
        contact.messageTime = message.time
        if (message.type == Message.TYPE_TEXT) {
            contact.message = message.text
        }
        val contactId = ContactDao.put(contact)
        contact.id = contactId
        _contactFlow.tryEmit(Pair(CONTACT_PUT, contact))
    }

    private val onPvMessage = Emitter.Listener { args ->
        val message = Gson().fromJson(args[0].toString(), Message::class.java).apply {
            transfer = Message.TRANSFER_RECEIVE
            delivery = Message.DELIVERY_WAITING
        }
        try {
            message.id = MessageDao.put(message.apply { id = 0 })
            val contact = ContactDao.get(message.senderUid) ?: message.getContact()
            contact.messageDelivery = Message.DELIVERY_HIDDEN
            contact.notifCount = (contact.notifCount.toInt() + 1).toString()
            setupAndPutContact(contact, message)
            Preferences.instance().incrementContactMessagesCount(contact.uid)
            _messageFlow.tryEmit(Pair(MESSAGE_RECEIVE, message))
            /*send notif*/
            if (ChatViewModel.isActivityStoppedForContact(contact.uid)) {
                Notifs.notifyMessage(App.context, message, contact)
            }
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
            MessageDao.markAllSentAsReadUntil(message)
            _messageFlow.tryEmit(Pair(MESSAGE_READ, message))
            SocketService.ioSocket?.socket?.emit(Config.ON_MSG_READ_RECEIVED, message.uid)
        }
    }

    private val onMessageReadReceived = Emitter.Listener { args ->
        MessageDao.get(args[0].toString())?.let { message ->
            MessageDao.markAllReceivedAsReadUntil(message)
        }
    }

    private val onTyping = Emitter.Listener { args ->
        ContactDao.get(args[0].toString())?.let {
            _contactFlow.tryEmit(Pair(CONTACT_TYPING, it))
            _messageFlow.tryEmit(
                Pair(
                    MESSAGE_TYPING,
                    Message(
                        type = Message.TYPE_TYPING,
                        receiverUid = it.uid,
                        uid = UUID.randomUUID().toString()
                    )
                )
            )
        }
    }

    private val onOnlineTime = Emitter.Listener { args ->
        ContactDao.get(args[0].toString())?.let {
            it.onlineTime = args[1].toString().toLong()
            ContactDao.put(it)
            _contactFlow.tryEmit(Pair(CONTACT_UPDATE, it))
            _messageFlow.tryEmit(
                Pair(
                    MESSAGE_ONLINE_TIME,
                    Message(receiverUid = it.uid, time = it.onlineTime)
                )
            )
        }
    }

    private val onOnlineTimeContact = Emitter.Listener { args ->
        val jsonArray = JSONArray(args[0].toString())
        for (i in 0 until jsonArray.length()) {
            val json = JSONObject(jsonArray[i].toString())
            ContactDao.get(json.getString("uid"))?.let { contact ->
                contact.onlineTime = json.getLong("onlineTime")
                ContactDao.put(contact)
                _contactFlow.tryEmit(Pair(CONTACT_UPDATE, contact))
            }
        }
    }
}