package com.hyapp.achat.model.entity

import android.os.Parcelable
import android.util.Pair
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Message(
    @Index
    @Unique
    var uid: String = "",
    var type: Byte = TYPE_TEXT,
    var transfer: Byte = TRANSFER_RECEIVE,
    var time: Long = 0,
    var text: String = "",
    var extraTextSize: Int = 0,
    var mediaPath: String = "",

    @Index
    var receiverUid: String = "",

    @Index
    var senderUid: String = "",
    var senderRank: Byte = 0,
    var senderScore: Int = 0,
    var senderLoginTime: Long = 0,
    var senderName: String = "",
    var senderBio: String = "",
    var senderGender: Byte = UserConsts.GENDER_MALE,
    var senderAvatars: List<String> = mutableListOf(),
    var senderOnlineTime: Long = UserConsts.TIME_ONLINE,

    var chatType: Byte = CHAT_TYPE_PV,

    var delivery: Byte = DELIVERY_WAITING,
    var bubble: Byte = BUBBLE_SINGLE,

    @Id
    var id: Long = 0

) : Parcelable {
    constructor(
        uid: String = "",
        type: Byte = TYPE_TEXT,
        transfer: Byte = TRANSFER_RECEIVE,
        time: Long = 0,
        text: String = "",
        extraTextSize: Int = 0,
        mediaPath: String = "",
        receiverUid: String = "",
        user: User,
        chatType: Byte= CHAT_TYPE_PV
    ) : this(
        uid, type, transfer, time, text, extraTextSize, mediaPath, receiverUid,
        user.uid, user.rank, user.score, user.loginTime, user.name, user.bio, user.gender,
        user.avatars, user.onlineTime, chatType
    )

    companion object {
        const val TRANSFER_SEND: Byte = 1
        const val TRANSFER_RECEIVE: Byte = 2

        const val TYPE_TEXT: Byte = 0
        const val TYPE_IMAGE: Byte = 2
        const val TYPE_VOICE: Byte = 4
        const val TYPE_VIDEO: Byte = 6
        const val TYPE_MUSIC: Byte = 8
        const val TYPE_FILE: Byte = 10
        const val TYPE_DETAILS: Byte = 12
        const val TYPE_PROFILE: Byte = 14
        const val TYPE_LOTTIE: Byte = 16
        const val TYPE_TYPING: Byte = 18

        const val DELIVERY_HIDDEN: Byte = 1
        const val DELIVERY_WAITING: Byte = 2
        const val DELIVERY_SENT: Byte = 3
        const val DELIVERY_READ: Byte = 4

        const val BUBBLE_START: Byte = 1
        const val BUBBLE_MIDDLE: Byte = 2
        const val BUBBLE_END: Byte = 3
        const val BUBBLE_SINGLE: Byte = 4

        const val TEXT_SIZE_SP = 14
        const val EMOJI_SIZE_LARGEST_SP = 36

        const val CHAT_TYPE_PV: Byte = 1
        const val CHAT_TYPE_ROOM: Byte = 2
    }

    val isChatMessage
        get() = type != TYPE_DETAILS && type != TYPE_PROFILE && type != TYPE_TYPING

    val isPvMessage
        get() = chatType == CHAT_TYPE_PV

    fun setAndGetTextSizes(sp1: Int): Pair<Float, Int> {
        var textSize = ((TEXT_SIZE_SP + extraTextSize) * sp1).toFloat()
        var emojiSize = ((TEXT_SIZE_SP + 3 + extraTextSize) * sp1).toFloat()

        var hasText = false
        for (element in text) {
            if (element.code < 2000) {
                hasText = true
                break
            }
        }
        if (!hasText) {
            emojiSize = ((EMOJI_SIZE_LARGEST_SP + extraTextSize) * sp1).toFloat()
            for (i in 1 until text.length) {
                if (emojiSize <= (TEXT_SIZE_SP + 3 + extraTextSize) * sp1) {
                    emojiSize = ((TEXT_SIZE_SP + 3 + extraTextSize) * sp1).toFloat()
                    break
                }
                emojiSize -= sp1.toFloat()
            }
            textSize = emojiSize * 0.8f
        }
        return Pair(textSize, emojiSize.toInt())
    }

    fun getContact(): Contact {
        return Contact(
            Contact.TYPE_USER,
            senderName, senderBio, senderGender, senderAvatars, senderOnlineTime,
            senderUid, senderRank, senderScore, senderLoginTime,
            text, time, mediaPath
        )
    }
}