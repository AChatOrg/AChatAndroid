package com.hyapp.achat.model.entity

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Transient
import io.objectbox.annotation.Unique
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Contact(
    var type: Byte = TYPE_SINGLE,

    var name: String = "",
    var bio: String = "",
    var gender: Byte = GENDER_MALE,
    var avatars: MutableList<String> = mutableListOf(),
    var onlineTime: Long = TIME_ONLINE,

    @Index
    @Unique
    var uid: String = "",
    var rank: Byte = 0,
    var score: Int = 0,
    var loginTime: Long = 0,

    var message: String = "",
    var messageTime: Long = 0,
    var mediaPath: String = "",
    var messageDelivery: Byte = Message.DELIVERY_HIDDEN,
    var notifCount: String = "0",

    @Id
    var id: Long = 0,

    @Transient var isTyping: Boolean = false

) : UserConsts(), Parcelable {

    constructor(user: User) : this(
        TYPE_SINGLE, user.name, user.bio, user.gender,
        user.avatars, user.onlineTime, user.uid, user.rank, user.score, user.loginTime
    )

    constructor(room: Room, membersStr: String, onlineStr: String) : this(
        TYPE_ROOM, room.name,
        "${room.memberCount} " + membersStr + ", ${room.onlineMemberCount} " + onlineStr,
        room.gender,
        room.avatars, TIME_ONLINE, room.uid, 0, 0, 0
    )

    companion object {
        const val TYPE_SINGLE: Byte = 1
        const val TYPE_ROOM: Byte = 2

        const val TIME_ONLINE: Long = 0
    }

    fun getUser(): User {
        return User(name, bio, gender, avatars, onlineTime, uid, rank, score, loginTime)
    }
}