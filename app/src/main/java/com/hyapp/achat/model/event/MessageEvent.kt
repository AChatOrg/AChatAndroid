package com.hyapp.achat.model.event


class MessageEvent(var json: String, action: Byte) : Event(action) {

    companion object {
        const val ACTION_SEND: Byte = 1
        const val ACTION_RECEIVE: Byte = 2
    }
}