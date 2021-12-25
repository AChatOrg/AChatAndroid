package com.hyapp.achat.model

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

    constructor(name: String, bio: String?, gender: Byte, key: Key, avatars: Array<String?>?, onlineTime: Long, messageTime: Long, messageDelivery: Byte, notifCount: String?, mediaMessagePath: String?) : super(name, bio, gender, avatars) {
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


    companion object {
        const val DELIVERY_HIDDEN: Byte = 1
        const val DELIVERY_WAITING: Byte = 2
        const val DELIVERY_UNREAD: Byte = 3
        const val DELIVERY_READ: Byte = 4
    }
}