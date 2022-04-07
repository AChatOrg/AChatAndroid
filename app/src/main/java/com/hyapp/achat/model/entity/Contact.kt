package com.hyapp.achat.model.entity

import android.os.Parcelable
import com.hyapp.achat.view.utils.UiUtils
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Transient
import io.objectbox.annotation.Unique
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Contact(
    var type: Byte = TYPE_USER,

    var name: String = "",
    var bio: String = "",
    var gender: Byte = GENDER_MALE,
    var avatars: List<String> = mutableListOf(),
    var onlineTime: Long = TIME_ONLINE,

    @Index
    @Unique
    var uid: String = "",
    var rank: Byte = 0,
    var score: Int = 0,
    var loginTime: Long = 0,

    var username: String = "unknown",

    var message: String = "",
    var messageTime: Long = 0,
    var mediaPath: String = "",
    var messageDelivery: Byte = Message.DELIVERY_HIDDEN,
    var notifCount: String = "0",

    @Id
    var id: Long = 0,

    @Transient var typingName: String? = null,

    var account: String = ""

) : UserConsts(), Parcelable {

    constructor(user: User) : this(
        TYPE_USER,
        user.name,
        user.bio,
        user.gender,
        user.avatars,
        user.onlineTime,
        user.uid,
        user.rank,
        user.score,
        user.loginTime,
        user.username
    )

    constructor(room: Room, membersStr: String, onlineStr: String) : this(
        TYPE_ROOM, room.name,
        "${
            UiUtils.formatNum(room.memberCount.toLong())
        } " + membersStr + "," +
                " ${UiUtils.formatNum(room.onlineMemberCount.toLong())} " + onlineStr,
        room.gender,
        room.avatars, TIME_ONLINE, room.uid, 0, 0, 0
    )

    companion object {
        const val TYPE_USER: Byte = 1
        const val TYPE_ROOM: Byte = 2
        const val TYPE_PV_ROOM: Byte = 3

        const val TIME_ONLINE: Long = 0
    }

    fun getUser(): User {
        return User(
            name,
            bio,
            gender,
            avatars,
            onlineTime,
            uid,
            rank,
            score,
            loginTime,
            username = username
        )
    }

    val isUser
        get() = type == TYPE_USER

    val isRoom
        get() = type == TYPE_ROOM || type == TYPE_PV_ROOM

    val isPvRoom
        get() = type == TYPE_PV_ROOM

    fun same(c: Contact): Boolean {
        return message == c.message
                && messageTime == c.messageTime
                && messageDelivery == c.messageDelivery
                && notifCount == c.notifCount
                && onlineTime == c.onlineTime
                && typingName == c.typingName
                && mediaPath == c.mediaPath
                && name == c.name
                && gender == c.gender
                && avatars == c.avatars
                && type == c.type
                && rank == c.rank
    }

    fun setUser(user: User) {
        name = user.name
        bio = user.bio
        gender = user.gender
        avatars = user.avatars
        onlineTime = user.onlineTime
        uid = user.uid
        rank = user.rank
        score = user.score
        loginTime = user.loginTime
        username = user.username
    }
}