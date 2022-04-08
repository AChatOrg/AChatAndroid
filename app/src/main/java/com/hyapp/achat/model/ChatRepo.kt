package com.hyapp.achat.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hyapp.achat.App
import com.hyapp.achat.Config
import com.hyapp.achat.R
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.gson.UserDeserializer
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.objectbox.MessageDao
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.viewmodel.ChatViewModel
import com.hyapp.achat.viewmodel.MainViewModel
import com.hyapp.achat.viewmodel.Notifs
import com.hyapp.achat.viewmodel.service.SocketService
import io.objectbox.exception.UniqueViolationException
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

@ExperimentalCoroutinesApi
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
        socket.on(Config.ON_MESSAGE, onPvMessage)
        socket.on(Config.ON_MESSAGE_SENT, onMessageSent)
        socket.on(Config.ON_MSG_READ, onMessageRead)
        socket.on(Config.ON_MSG_READ_RECEIVED, onMessageReadReceived)
        socket.on(Config.ON_TYPING, onTyping)
        socket.on(Config.ON_ONLINE_TIME, onOnlineTime)
        socket.on(Config.ON_ONLINE_TIME_CONTACTS, onOnlineTimeContact)
    }

    suspend fun sendMessage(message: Message, cont: Contact) {
        withContext(ioDispatcher) {
            ensureActive()
            mutex.withLock {

                val contact = ContactDao.get(UserLive.value?.uid ?: "", cont.uid) ?: cont

                if (cont.isUser || cont.isPvRoom)
                    contact.messageDelivery = Message.DELIVERY_WAITING
                else
                    contact.messageDelivery = Message.DELIVERY_READ

                setupAndPutContact(contact, message)

                if (!message.isRoom) {
                    message.id = MessageDao.put(message.apply {
                        account = UserLive.value?.uid ?: ""
                        id = 0
                    })
                    Preferences.instance()
                        .incrementContactMessagesCount(UserLive.value?.uid ?: "", contact.uid)
                } else {
                    MainViewModel.addPublicRoomUnreadMessage(contact.uid, message)
                }

                SocketService.ioSocket?.socket?.let {
                    if (it.connected()) {
                        it.emit(Config.ON_MESSAGE, Gson().toJson(message))
                    }
                }
            }
        }
    }

    suspend fun sendOffline() {
        withContext(ioDispatcher) {
            ensureActive()
            mutex.withLock {
                (UserLive.value ?: UserDao.get(User.CURRENT_USER_ID))?.let {
                    var messages = MessageDao.waitings(UserLive.value?.uid ?: "", it.uid)
                    for (message in messages) {
                        SocketService.ioSocket?.socket?.let { socket ->
                            if (socket.connected()) {
                                socket.emit(Config.ON_MESSAGE, Gson().toJson(message))
                            }
                        }
                    }
                    messages = MessageDao.allSentUnReads(UserLive.value?.uid ?: "", it.uid)
                    for (message in messages) {
                        SocketService.ioSocket?.socket?.let { socket ->
                            if (socket.connected()) {
                                socket.emit(
                                    Config.ON_MSG_READ,
                                    message.uid,
                                    message.senderUid
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun sendOnlineTimeContactsRequest() {
        withContext(ioDispatcher) {
            ensureActive()
            mutex.withLock {
                val contacts = ContactDao.all(UserLive.value?.uid ?: "")
                val list = ArrayList<String>()
                for (contact in contacts) {
                    list.add(contact.uid)
                }
                SocketService.ioSocket?.socket?.emit(
                    Config.ON_ONLINE_TIME_CONTACTS,
                    JSONArray(list)
                )
            }
        }
    }

    suspend fun markMessageAsRead(contact: Contact, changedMessages: List<Message>) {
        if (!contact.isRoom) {
            withContext(ioDispatcher) {
                ensureActive()
                mutex.withLock {
                    ContactDao.get(UserLive.value?.uid ?: "", contact.uid)?.let {
                        var count = it.notifCount.toInt()
                        count = max(count - changedMessages.size, 0)
                        it.notifCount = count.toString()
                        ContactDao.put(it.apply { account = UserLive.value?.uid ?: "" })
                        _contactFlow.tryEmit(Pair(CONTACT_UPDATE, it))
                    }
                    MessageDao.put(changedMessages)
                    Notifs.remove(App.context, contact.id.toInt())
                    SocketService.ioSocket?.socket?.let { socket ->
                        if (socket.connected()) {
                            socket.emit(
                                Config.ON_MSG_READ,
                                changedMessages[0].uid,
                                contact.uid,
                                changedMessages[0].chatType
                            )
                        }
                    }
                }
            }
        }
    }

//    fun sendMessageRead(message: Message) {
//        SocketService.ioSocket?.socket?.emit(Config.ON_MSG_READ, message.uid, message.senderUid)
//    }

    suspend fun clearContactNotifs(contactUid: String) {
        withContext(ioDispatcher) {
            ContactDao.get(UserLive.value?.uid ?: "", contactUid)?.let {
                it.notifCount = "0"
                ContactDao.put(it.apply { account = UserLive.value?.uid ?: "" })
                _contactFlow.tryEmit(Pair(CONTACT_UPDATE, it))
            }
        }
    }

    fun sendTyping(receiverUid: String, isRoomOrPvRoom: Boolean) {
        SocketService.ioSocket?.socket?.emit(Config.ON_TYPING, receiverUid, isRoomOrPvRoom)
    }

    fun sendOnlineTime(isOnline: Boolean) {
        SocketService.ioSocket?.socket?.emit(Config.ON_ONLINE_TIME, isOnline)
    }

    fun sendJoinLeaveRoom(roomUid: String, isJoinOrLeave: Boolean) {
        SocketService.ioSocket?.socket?.emit(Config.ON_JOIN_LEAVE_ROOM, roomUid, isJoinOrLeave)
    }

    fun addUserJoinedMessage(roomUid: String, nameUser: String) {
        _messageFlow.tryEmit(
            Pair(
                MESSAGE_RECEIVE,
                Message(
                    uid = UUID.randomUUID().toString(),
                    type = Message.TYPE_DETAILS,
                    chatType = Message.CHAT_TYPE_ROOM,
                    text = String.format(App.context.getString(R.string.s_joined_room), nameUser),
                    receiverUid = roomUid
                ).also { message ->
                    MainViewModel.addPublicRoomUnreadMessage(roomUid, message)
                    ContactDao.get(UserLive.value?.uid ?: "", roomUid)?.let {
                        it.message = message.text
                        ContactDao.put(it.apply { account = UserLive.value?.uid ?: "" })
                        _contactFlow.tryEmit(Pair(CONTACT_UPDATE, it))
                    }
                }
            )
        )
    }

    fun addUserLeftMessage(roomUid: String, nameUser: String) {
        _messageFlow.tryEmit(
            Pair(
                MESSAGE_RECEIVE,
                Message(
                    uid = UUID.randomUUID().toString(),
                    type = Message.TYPE_DETAILS,
                    chatType = Message.CHAT_TYPE_ROOM,
                    text = String.format(App.context.getString(R.string.s_left_room), nameUser),
                    receiverUid = roomUid
                ).also { message ->
                    MainViewModel.addPublicRoomUnreadMessage(roomUid, message)
                    ContactDao.get(UserLive.value?.uid ?: "", roomUid)?.let {
                        it.message = message.text
                        ContactDao.put(it.apply { account = UserLive.value?.uid ?: "" })
                        _contactFlow.tryEmit(Pair(CONTACT_UPDATE, it))
                    }
                }
            )
        )
    }

    fun emitContactToViewModel(contact: Contact) {
        _contactFlow.tryEmit(Pair(CONTACT_UPDATE, contact))
    }

    private fun setupAndPutContact(contact: Contact, message: Message) {
        contact.messageTime = message.time
        if (message.type == Message.TYPE_TEXT) {
            contact.message = message.text
        }
        val contactId = ContactDao.put(contact.apply { account = UserLive.value?.uid ?: "" })
        contact.id = contactId
        _contactFlow.tryEmit(Pair(CONTACT_PUT, contact))
    }

    private val onPvMessage = Emitter.Listener { args ->
        val message = Gson().fromJson(args[0].toString(), Message::class.java).apply {
            transfer = Message.TRANSFER_RECEIVE
            delivery = Message.DELIVERY_WAITING
        }
        try {

            val contact = if (message.isPv)
                ContactDao.get(UserLive.value?.uid ?: "", message.senderUid) ?: message.getContact()
            else
                ContactDao.get(UserLive.value?.uid ?: "", message.receiverUid)

            _messageFlow.tryEmit(Pair(MESSAGE_RECEIVE, message))

            if (contact != null) {
                contact.messageDelivery = Message.DELIVERY_HIDDEN
                if (!message.isRoom) {
                    contact.notifCount = (contact.notifCount.toInt() + 1).toString()
                } else if (ChatViewModel.isActivityStoppedForContact(contact.uid)) {
                    contact.notifCount = (contact.notifCount.toInt() + 1).toString()
                }
                setupAndPutContact(contact, message)
                if (!message.isRoom) {
                    message.id = MessageDao.put(message.apply {
                        account = UserLive.value?.uid ?: ""
                        id = 0
                    })
                    Preferences.instance()
                        .incrementContactMessagesCount(UserLive.value?.uid ?: "", contact.uid)
                    /*send notif*/
                    if (ChatViewModel.isActivityStoppedForContact(contact.uid)) {
                        Notifs.notifyMessage(App.context, message, contact)
                    }
                } else {
                    MainViewModel.addPublicRoomUnreadMessage(contact.uid, message)
                }
            }
        } catch (e: UniqueViolationException) {
            e.printStackTrace()
        } finally {
            if (message.isPv || message.isPvRoom) {
                SocketService.ioSocket?.socket?.emit(
                    Config.ON_MSG_RECEIVED,
                    message.uid,
                    message.chatType,
                    message.receiverUid
                )
            }
        }
    }

    private val onMessageSent = Emitter.Listener { args ->
        MessageDao.get(UserLive.value?.uid ?: "", args[0].toString())?.let { message ->
            message.delivery = Message.DELIVERY_SENT
            ContactDao.get(UserLive.value?.uid ?: "", message.receiverUid)?.let { contact ->
                contact.messageTime = message.time
                contact.messageDelivery = message.delivery
                ContactDao.put(contact.apply { account = UserLive.value?.uid ?: "" })
                _contactFlow.tryEmit(Pair(CONTACT_UPDATE, contact))
            }

            _messageFlow.tryEmit(Pair(MESSAGE_SENT, message))

            if (message.isPv || message.isPvRoom) {
                MessageDao.put(message.apply { account = UserLive.value?.uid ?: "" })
            }
        }
    }

    private val onMessageRead = Emitter.Listener { args ->
        MessageDao.get(UserLive.value?.uid ?: "", args[0].toString())?.let { message ->
            message.delivery = Message.DELIVERY_READ
            ContactDao.get(UserLive.value?.uid ?: "", message.receiverUid)?.let { contact ->
                contact.messageDelivery = message.delivery
                ContactDao.put(contact.apply { account = UserLive.value?.uid ?: "" })
                _contactFlow.tryEmit(Pair(CONTACT_UPDATE, contact))
            }

            _messageFlow.tryEmit(Pair(MESSAGE_READ, message))

            if (!message.isRoom) {
                MessageDao.markAllSentAsReadUntil(UserLive.value?.uid ?: "", message)
                SocketService.ioSocket?.socket?.emit(
                    Config.ON_MSG_READ_RECEIVED,
                    message.uid,
                    message.chatType,
                    message.receiverUid
                )
            }
        }
    }

    private val onMessageReadReceived = Emitter.Listener { args ->
        MessageDao.get(UserLive.value?.uid ?: "", args[0].toString())?.let { message ->
            if (message.isPv || message.isPvRoom) {
                MessageDao.markAllReceivedAsReadUntil(UserLive.value?.uid ?: "", message)
            }
        }
    }

    private val onTyping = Emitter.Listener { args ->
        if (args.size < 2) {//if isPv
            ContactDao.get(UserLive.value?.uid ?: "", args[0].toString())?.let {
                _contactFlow.tryEmit(Pair(CONTACT_TYPING, it.apply { typingName = "" }))
                _messageFlow.tryEmit(
                    Pair(
                        MESSAGE_TYPING,
                        Message(
                            uid = UUID.randomUUID().toString(),
                            type = Message.TYPE_TYPING,
                            receiverUid = it.uid,
                            senderAvatars = it.avatars
                        )
                    )
                )
            }
        } else {
            ContactDao.get(UserLive.value?.uid ?: "", args[0].toString())?.let {
                val user = GsonBuilder()
                    .registerTypeAdapter(User::class.java, UserDeserializer())
                    .create()
                    .fromJson(args[1].toString(), User::class.java)
                _contactFlow.tryEmit(Pair(CONTACT_TYPING, it.apply { typingName = user.name }))
                _messageFlow.tryEmit(
                    Pair(
                        MESSAGE_TYPING,
                        Message(
                            uid = UUID.randomUUID().toString(),
                            type = Message.TYPE_TYPING,
                            receiverUid = it.uid,
                            user = user
                        )
                    )
                )
            }
        }
    }

    private val onOnlineTime = Emitter.Listener { args ->
        ContactDao.get(UserLive.value?.uid ?: "", args[0].toString())?.let {
            it.onlineTime = args[1].toString().toLong()
            ContactDao.put(it.apply { account = UserLive.value?.uid ?: "" })
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
            ContactDao.get(UserLive.value?.uid ?: "", json.getString("uid"))?.let { contact ->
                contact.name = json.getString("name")
                contact.bio = json.getString("bio")
                contact.gender = json.getInt("gender").toByte()
                val array = json.getJSONArray("avatars")
                val avatars = mutableListOf<String>()
                for (j in 0 until array.length()) {
                    avatars.add(array.getString(j))
                }
                contact.avatars = avatars
                contact.onlineTime = json.getLong("onlineTime")
                ContactDao.put(contact.apply { account = UserLive.value?.uid ?: "" })
                _contactFlow.tryEmit(Pair(CONTACT_UPDATE, contact))
            }
        }
    }
}