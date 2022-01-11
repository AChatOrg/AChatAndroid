package com.hyapp.achat.model.entity

abstract class ChatMessage(
        uid: String = "",
        type: Byte = TYPE_TEXT,
        transfer: Byte = TRANSFER_RECEIVE,
        time: Long = 0,

        var sender: Contact = Contact(),
        var receiverUid: String = "",

        ) : Message(uid, type, transfer, time) {

    var delivery: Byte = DELIVERY_WAITING
    var bubble: Byte = BUBBLE_SINGLE

    companion object {
        const val DELIVERY_HIDDEN: Byte = 1
        const val DELIVERY_WAITING: Byte = 2
        const val DELIVERY_UNREAD: Byte = 3
        const val DELIVERY_READ: Byte = 4

        const val BUBBLE_START: Byte = 1
        const val BUBBLE_MIDDLE: Byte = 2
        const val BUBBLE_END: Byte = 3
        const val BUBBLE_SINGLE: Byte = 4
    }

    override fun same(message: Message): Boolean {
        return message is ChatMessage
                && bubble == message.bubble
                && delivery == message.delivery
                && super.same(message)
    }
}