package com.hyapp.achat.model

import com.google.gson.Gson
import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.People
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object LoginRepo {

    private val _loggedState = MutableSharedFlow<People>(extraBufferCapacity = 1)
    val loggedState = _loggedState.asSharedFlow()

    fun listen(socket: Socket) {
        socket.on(Config.ON_LOGGED, onLogged)
    }

    private val onLogged = Emitter.Listener { args ->
        val people = Gson().fromJson(args[0].toString(), People::class.java)
        _loggedState.tryEmit(people)
    }
}