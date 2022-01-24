package com.hyapp.achat.model

import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.ConnLive
import com.hyapp.achat.viewmodel.service.SocketService
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URISyntaxException
import kotlin.coroutines.CoroutineContext

class IOSocket(loginJson: String) : CoroutineScope {

    var options: IO.Options = IO.Options.builder()
        .setQuery(Config.SOCKET_QUERY_DATA + "=" + loginJson)
        .build()

    lateinit var socket: Socket

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    fun destroy() {
        socket.off()
        socket.disconnect()
    }

    private fun listen() {
        socket.on(Config.ON_DISCONNECT, onDisconnect)
        socket.on(Config.ON_CONNECT, onConnect)
        LoginRepo.listen(socket)
        UsersRepo.listen(socket)
        ChatRepo.listen(socket)
    }

    private val onDisconnect = Emitter.Listener { args ->
        ConnLive.singleton().postValue(ConnLive.Status.DISCONNECTED)
    }

    private val onConnect = Emitter.Listener { args ->
        ConnLive.singleton().postValue(ConnLive.Status.CONNECTED)

        launch { ChatRepo.sendWaitingsMessages() }
        launch { ChatRepo.sendReadsMessages() }
        launch { ChatRepo.sendOnlineTimeContactsRequest() }
    }

    init {
        try {
            socket = IO.socket(Config.SERVER_URL, options)
            socket.connect()
            listen()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun setQuery(json: String) {
        SocketService.ioSocket?.options?.query = Config.SOCKET_QUERY_DATA + "=" + json
    }
}