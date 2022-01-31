package com.hyapp.achat.model

import com.google.gson.GsonBuilder
import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.User
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
import org.json.JSONObject

@ExperimentalCoroutinesApi
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
        Preferences.instance().putLogged(true)

        JSONObject(Preferences.instance().loginInfo).let {
            it.put("operation", Config.OPERATION_RECONNECT_GUEST)
            val newJson = it.toString()
            Preferences.instance().putLoginInfo(newJson)
            SocketService.ioSocket?.setQuery(newJson)
        }
    }

    fun requestLogout(): Flow<Boolean> = callbackFlow {
        SocketService.ioSocket?.socket?.let {
            if (it.connected()) {
                it.emit(Config.ON_LOGOUT)
                it.on(Config.ON_LOGOUT) { args ->
                    it.off(Config.ON_LOGOUT)
                    val loggedOut = args[0].toString().toBoolean()
                    trySend(loggedOut)
                }
            } else {
                trySend(false)
            }
        }
        awaitClose { SocketService.ioSocket?.socket?.off(Config.ON_LOGOUT) }
    }

    fun requestUsernameExist(username: CharSequence): Flow<Boolean> = callbackFlow {
        SocketService.ioSocket?.socket?.let {
            if (it.connected()) {
                it.emit(Config.ON_REQUEST_CHECK_USERNAME, username)
                it.on(Config.ON_REQUEST_CHECK_USERNAME) { args ->
                    it.off(Config.ON_REQUEST_CHECK_USERNAME)
                    val exist = args[0].toString().toBoolean()
                    trySend(exist)
                }
            } else {
                trySend(false)
            }
        }
        awaitClose { SocketService.ioSocket?.socket?.off(Config.ON_REQUEST_CHECK_USERNAME) }
    }

    fun requestRegister(): Flow<Pair<Boolean, String>> = callbackFlow {

        awaitClose { }
    }
}