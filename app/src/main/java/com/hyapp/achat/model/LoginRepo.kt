package com.hyapp.achat.model

import com.google.gson.GsonBuilder
import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.gson.UserDeserializer
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object LoginRepo {

    private val _loggedState = MutableSharedFlow<User>(extraBufferCapacity = 1)
    val loggedState = _loggedState.asSharedFlow()

    fun listen(socket: Socket) {
        socket.on(Config.ON_LOGGED, onLogged)
    }

    private val onLogged = Emitter.Listener { args ->
        val user = GsonBuilder()
                .registerTypeAdapter(User::class.java, UserDeserializer())
                .create()
                .fromJson(args[0].toString(), User::class.java)
        _loggedState.tryEmit(user)
    }
}