package com.hyapp.achat.model

import android.os.Bundle
import android.view.View
import com.google.gson.annotations.Expose
import com.hyapp.achat.bl.utils.TimeUtils
import com.hyapp.achat.model.utils.MessageUtils
import com.hyapp.achat.model.utils.PersonUtils
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

@Entity
class Contact : People {

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_ONLINE_TIME = "onlineTime"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_MESSAGE_TIME = "messageTime"
        const val EXTRA_MESSAGE_DELIVERY = "messageDelivery"
        const val EXTRA_NOTIF_COUNT = "notifCount"
        const val EXTRA_MEDIA_MESSAGE_PATH = "mediaMessagePath"

        const val TYPE_SINGLE: Byte = 1
        const val TYPE_GROUP: Byte = 2

        const val TIME_ONLINE: Long = 0

        const val ONLINE_TYPE_CONTACT: Byte = 1
        const val ONLINE_TYPE_CHAT: Byte = 2
        const val ONLINE_TYPE_PROFILE: Byte = 3
    }

    @Id
    var id: Long = 0

    @Expose
    var type: Byte = TYPE_SINGLE

    @Index
    @Unique
    @Expose
    var uid: String = ""

    @Expose
    var rank: Byte = 0

    @Expose
    var score: Int = 0

    @Expose
    var loginTime: Long = 0

    @Expose
    var onlineTime: Long = TIME_ONLINE

    var message: String = ""
    var messageTime: Long = -1
        set(value) {
            field = value
            setupMessageTime()
        }
    var messageDelivery: Byte = ChatMessage.DELIVERY_HIDDEN
        set(value) {
            field = value
            setupMessageDelivery()
        }
    var notifCount: String? = null
    var mediaMessagePath: String? = null
    var onlineTimeStr: String = ""

    @Transient
    var messageTimeStr: String = ""

    @Transient
    var messageDeliveryRes: Int = MessageUtils.DELIVERY_WAITING_RES

    @Transient
    var onlineTimeRes: Int = PersonUtils.LAST_ONLINE_CONTACT_BG_RES_GREY

    @Transient
    var notifRes: Int = PersonUtils.NOTIF_CONTACT_BG_RES_GREY

    constructor()

    constructor(type: Byte = TYPE_SINGLE,
                name: String = "",
                bio: String? = null,
                gender: Byte = GENDER_MALE,
                key: Key = Key(),
                avatars: Array<String?> = emptyArray(),
                onlineTime: Long = 0,
                message: String = "",
                messageTime: Long = 0,
                messageDelivery: Byte = ChatMessage.DELIVERY_HIDDEN,
                notifCount: String? = null,
                mediaMessagePath: String? = null
    ) : super(name, bio, gender, avatars = avatars) {
        this.type = type
        this.uid = key.uid
        this.rank = key.rank
        this.score = key.score
        this.loginTime = key.loginTime
        this.onlineTime = onlineTime
        this.message = message
        this.messageTime = messageTime
        this.messageDelivery = messageDelivery
        this.notifCount = notifCount
        this.mediaMessagePath = mediaMessagePath
    }

    init {
        setupRank(rank)
        setupOnlineTime(ONLINE_TYPE_CONTACT)
        setupMessageTime()
        setupMessageDelivery()
    }

    constructor(people: People, onlineTime: Long) : this(
            TYPE_SINGLE,
            people.name,
            people.bio,
            people.gender,
            people.key!!,
            people.avatars,
            onlineTime
    )

    constructor(bundle: Bundle) : super(bundle) {
        uid = super.key!!.uid
        rank = super.key!!.rank
        score = super.key!!.score
        loginTime = super.key!!.loginTime
        super.key = null
        type = bundle.getByte(EXTRA_TYPE)
        onlineTime = bundle.getLong(EXTRA_ONLINE_TIME)
        message = bundle.getString(EXTRA_MESSAGE) ?: ""
        messageTime = bundle.getLong(EXTRA_MESSAGE_TIME)
        messageDelivery = bundle.getByte(EXTRA_MESSAGE_DELIVERY)
        notifCount = bundle.getString(EXTRA_NOTIF_COUNT)
        mediaMessagePath = bundle.getString(EXTRA_MEDIA_MESSAGE_PATH)
    }

    override val bundle: Bundle
        get() {
            return super.bundle.apply {
                putParcelable(EXTRA_KEY, Key(uid, rank, score, loginTime))
                putByte(EXTRA_TYPE, type)
                putLong(EXTRA_ONLINE_TIME, onlineTime)
                putString(EXTRA_MESSAGE, message)
                putLong(EXTRA_MESSAGE_TIME, messageTime)
                putByte(EXTRA_MESSAGE_DELIVERY, messageDelivery)
                putString(EXTRA_NOTIF_COUNT, notifCount)
                putString(EXTRA_MEDIA_MESSAGE_PATH, mediaMessagePath)
            }
        }

    fun setupOnlineTime(type: Byte) {
        if (onlineTime == TIME_ONLINE) {
            onlineTimeStr = ""
            when (type) {
                ONLINE_TYPE_CONTACT -> onlineTimeRes = PersonUtils.LAST_ONLINE_CONTACT_BG_RES_GREEN
                ONLINE_TYPE_CHAT -> onlineTimeRes = PersonUtils.LAST_ONLINE_CHAT_BG_RES_GREEN
                ONLINE_TYPE_PROFILE -> onlineTimeRes = PersonUtils.LAST_ONLINE_PROFILE_BG_RES_GREEN
            }
        } else {
            onlineTimeStr = TimeUtils.timeAgoShort(System.currentTimeMillis() - onlineTime)
            when (type) {
                ONLINE_TYPE_CONTACT -> onlineTimeRes = PersonUtils.LAST_ONLINE_CONTACT_BG_RES_GREY
                ONLINE_TYPE_CHAT -> onlineTimeRes = PersonUtils.LAST_ONLINE_CHAT_BG_RES_GREY
                ONLINE_TYPE_PROFILE -> onlineTimeRes = PersonUtils.LAST_ONLINE_PROFILE_BG_RES_GREY
            }
        }
    }

    fun setupMessageTime() {
        messageTimeStr = TimeUtils.millis2DayTime(messageTime)
    }

    fun setupMessageDelivery() {
        when (messageDelivery) {
            ChatMessage.DELIVERY_READ -> messageDeliveryRes = MessageUtils.DELIVERY_READ_RES
            ChatMessage.DELIVERY_UNREAD -> messageDeliveryRes = MessageUtils.DELIVERY_UNREAD_RES
            ChatMessage.DELIVERY_WAITING -> messageDeliveryRes = MessageUtils.DELIVERY_WAITING_RES
        }
    }
}