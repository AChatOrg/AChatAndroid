package com.hyapp.achat.model.event

class ActionEvent(action: Byte) : Event(action) {

    companion object {
        const val ACTION_REQUEST_PEOPLE: Byte = 1
        const val ACTION_EXIT_APP: Byte = 2
    }
}