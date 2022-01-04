package com.hyapp.achat.model.event

import com.hyapp.achat.model.Contact
import com.hyapp.achat.model.Message


class MessageEvent(var message: Message, action: Byte, var receiver: Contact? = null) : Event(action) {

    companion object {
        const val ACTION_SEND: Byte = 1
        const val ACTION_RECEIVE: Byte = 2
    }
}