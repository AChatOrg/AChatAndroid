package com.hyapp.achat.model

import android.os.Bundle
import android.os.Message
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class Contact : People {
    @Id
    var id: Long = 0

    var uid: String = ""
    var rank: Byte = 0
    var score: Int = 0
    var loginTime: Long = 0

    var onlineTime: Long = 0
    var messageTime: Long = 0
    var messageDelivery: Byte = DELIVERY_HIDDEN
    var notifCount: String? = null
    var mediaMessagePath: String? = null

    constructor()

    constructor(name: String = "",
                bio: String? = null,
                gender: Byte = GENDER_MALE,
                key: Key = Key(),
                avatars: Array<String?>? = null,
                onlineTime: Long = 0,
                messageTime: Long = 0,
                messageDelivery: Byte = DELIVERY_HIDDEN,
                notifCount: String? = null,
                mediaMessagePath: String? = null
    ) : super(name, bio, gender, avatars = avatars) {
        this.uid = key.uid
        this.rank = key.rank
        this.score = key.score
        this.loginTime = key.loginTime
        this.onlineTime = onlineTime
        this.messageTime = messageTime
        this.messageDelivery = messageDelivery
        this.notifCount = notifCount
        this.mediaMessagePath = mediaMessagePath
    }

    constructor(people: People, onlineTime: Long) : this(
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
        onlineTime = bundle.getLong(EXTRA_ONLINE_TIME)
        messageTime = bundle.getLong(EXTRA_MESSAGE_TIME)
        messageDelivery = bundle.getByte(EXTRA_MESSAGE_DELIVERY)
        notifCount = bundle.getString(EXTRA_NOTIF_COUNT)
        mediaMessagePath = bundle.getString(EXTRA_MEDIA_MESSAGE_PATH)
    }

    override val bundle: Bundle
        get() {
            return super.bundle.apply {
                putLong(EXTRA_ONLINE_TIME, onlineTime)
                putLong(EXTRA_MESSAGE_TIME, messageTime)
                putByte(EXTRA_MESSAGE_DELIVERY, messageDelivery)
                putString(EXTRA_NOTIF_COUNT, notifCount)
                putString(EXTRA_MEDIA_MESSAGE_PATH, mediaMessagePath)
            }
        }

    companion object {
        const val EXTRA_ONLINE_TIME = "onlineTime"
        const val EXTRA_MESSAGE_TIME = "messageTime"
        const val EXTRA_MESSAGE_DELIVERY = "messageDelivery"
        const val EXTRA_NOTIF_COUNT = "notifCount"
        const val EXTRA_MEDIA_MESSAGE_PATH = "mediaMessagePath"


        const val TIME_ONLINE: Long = 0

        const val DELIVERY_HIDDEN: Byte = 1
        const val DELIVERY_WAITING: Byte = 2
        const val DELIVERY_UNREAD: Byte = 3
        const val DELIVERY_READ: Byte = 4
    }
}