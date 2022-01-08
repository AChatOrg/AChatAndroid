package com.hyapp.achat.model.event

import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.model.entity.Message


class MessageEvent(var message: Message, action: Byte, var receiver: Contact? = null) : Event(action = action) {

    companion object {
        const val ACTION_SEND: Byte = 1
        const val ACTION_RECEIVE: Byte = 2
    }
}