package com.hyapp.achat.model

import com.google.gson.GsonBuilder
import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.gson.UserDeserializer
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.objectbox.MessageDao
import com.hyapp.achat.model.objectbox.ObjectBox
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.viewmodel.MainViewModel
import com.hyapp.achat.viewmodel.service.SocketService
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
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

    suspend fun onLogoutGuest() {
        withContext(Dispatchers.Default) {
            MainViewModel.publicRoomsMessageMap.clear()
            Preferences.instance().putLogged(false)
            Preferences.instance().deleteALl()
            ContactDao.removeALl()
            MessageDao.removeALl()
            UserDao.removeALl()
        }
    }
}