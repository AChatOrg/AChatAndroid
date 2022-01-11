package com.hyapp.achat.model.entity

import android.os.Bundle
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
    }

    @Id
    var id: Long = 0
    var type: Byte = TYPE_SINGLE

    @Index
    @Unique
    var uid: String = ""
    var rank: Byte = 0
    var score: Int = 0
    var loginTime: Long = 0

    var onlineTime: Long = TIME_ONLINE
    var message: String = ""
    var messageTime: Long = -1
    var messageDelivery: Byte = ChatMessage.DELIVERY_HIDDEN
    var notifCount: String? = null
    var mediaMessagePath: String? = null

    override fun same(person: Person): Boolean {
        return person is Contact
                && message == person.message
                && messageTime == person.messageTime
                && messageDelivery == person.messageDelivery
                && notifCount == person.notifCount
                && mediaMessagePath == person.mediaMessagePath
                && onlineTime == person.onlineTime
                && rank == person.rank
                && type == person.type
                && super.same(person)
    }

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
}