package com.hyapp.achat.model.entity

abstract class Message(
        var uid: String = "",
        var type: Byte = TYPE_TEXT,
        var transfer: Byte = TRANSFER_RECEIVE,
        var time: Long = 0,
) {
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
        const val TYPE_LOTTIE: Byte = 18
    }

    open fun same(message: Message): Boolean {
        return type == message.type
    }
}