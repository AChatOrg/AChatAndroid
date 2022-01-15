package com.hyapp.achat.model

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.entity.UserList
import com.hyapp.achat.model.gson.UserDeserializer
import com.hyapp.achat.viewmodel.service.SocketService
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow

object UsersRepo {

    private val _userCameFlow = MutableSharedFlow<User>(extraBufferCapacity = 1)
    val userCameFlow = _userCameFlow.asSharedFlow()

    private val _userLeftFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val userLeftFlow = _userLeftFlow.asSharedFlow()

    fun listen(socket: Socket) {
        socket.on(Config.ON_USER_CAME, onUserCame)
        socket.on(Config.ON_USER_LEFT, onUserLeft)
    }

    @ExperimentalCoroutinesApi
    fun requestUsers(): Flow<UserList> = callbackFlow {
        SocketService.ioSocket?.socket?.let {
            it.emit(Config.ON_USERS)
            it.on(Config.ON_USERS) { args ->
                val users = GsonBuilder()
                        .registerTypeAdapter(User::class.java, UserDeserializer())
                        .create()
                        .fromJson<List<User>>(args[0].toString(), object : TypeToken<List<User?>?>() {}.type)
                val userList = UserList()
                userList.addAll(users)
                trySend(userList)
            }
        }
        awaitClose { SocketService.ioSocket?.socket?.off(Config.ON_USERS) }
    }

    private val onUserCame = Emitter.Listener { args ->
        val user = GsonBuilder()
                .registerTypeAdapter(User::class.java, UserDeserializer())
                .create()
                .fromJson(args[0].toString(), User::class.java)
        _userCameFlow.tryEmit(user)
    }

    private val onUserLeft = Emitter.Listener { args ->
        val uid = args[0].toString()
        _userLeftFlow.tryEmit(uid)
    }
}