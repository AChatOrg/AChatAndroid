package com.hyapp.achat.model.event

import com.hyapp.achat.model.entity.Event

class ActionEvent(action: Byte) : Event(action = action) {

    companion object {
        const val ACTION_REQUEST_PEOPLE: Byte = 1
        const val ACTION_EXIT_APP: Byte = 2
    }
}