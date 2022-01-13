package com.hyapp.achat.model.entity

import android.util.Pair
import com.google.gson.annotations.Expose
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique
import io.objectbox.relation.ToOne

@Entity
data class Message(
        @Index
        @Unique
        @Expose
        var uid: String = "",
        @Expose
        var type: Byte = TYPE_TEXT,
        var transfer: Byte = TRANSFER_RECEIVE,
        @Expose
        var time: Long = 0,
        @Expose
        var receiverUid: String = "",

        @Expose
        var text: String = "",
        @Expose
        var extraTextSize: Int = 0,

        var delivery: Byte = DELIVERY_WAITING,
        var bubble: Byte = BUBBLE_SINGLE,

        @Id
        var id: Long = 0,
) {

    var sender: ToOne<Contact> = ToOne(this, Message_.sender)

    constructor(
            uid: String = "",
            type: Byte = TYPE_TEXT,
            transfer: Byte = TRANSFER_RECEIVE,
            time: Long = 0,
            senderId: Long,
            receiverUid: String = "",
            text: String = "",
            extraTextSize: Int = 0,
            delivery: Byte = DELIVERY_WAITING,
            bubble: Byte = BUBBLE_SINGLE,
            id: Long = 0,
    ) : this(uid, type, transfer, time, receiverUid, text, extraTextSize, delivery, bubble, id) {
        this.sender.targetId = senderId
    }

    constructor(
            uid: String = "",
            type: Byte = TYPE_TEXT,
            transfer: Byte = TRANSFER_RECEIVE,
            time: Long = 0,
            sender: Contact = Contact(),
            receiverUid: String = "",
            text: String = "",
            extraTextSize: Int = 0
    ) : this(uid, type, transfer, time, receiverUid, text, extraTextSize) {
        this.sender.target = sender
    }

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

        const val DELIVERY_HIDDEN: Byte = 1
        const val DELIVERY_WAITING: Byte = 2
        const val DELIVERY_UNREAD: Byte = 3
        const val DELIVERY_READ: Byte = 4

        const val BUBBLE_START: Byte = 1
        const val BUBBLE_MIDDLE: Byte = 2
        const val BUBBLE_END: Byte = 3
        const val BUBBLE_SINGLE: Byte = 4

        const val TEXT_SIZE_SP = 14
        const val EMOJI_SIZE_LARGEST_SP = 36
    }

    val isChatMessage
        get() = type != TYPE_DETAILS && type != TYPE_PROFILE

    fun setAndGetTextSizes(sp1: Int): Pair<Float, Int> {
        var textSize: Float = ((TEXT_SIZE_SP + extraTextSize) * sp1).toFloat()
        var emojiSize: Float = ((TEXT_SIZE_SP + 3 + extraTextSize) * sp1).toFloat()
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
}