package com.hyapp.achat.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.People
import com.hyapp.achat.model.entity.PeopleList
import com.hyapp.achat.model.entity.SortedList
import com.hyapp.achat.viewmodel.service.SocketService
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object PeopleRepo {

    private val _userCameFlow = MutableSharedFlow<People>(extraBufferCapacity = 1)
    val userCameFlow = _userCameFlow.asSharedFlow()

    private val _userLeftFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val userLeftFlow = _userLeftFlow.asSharedFlow()

    fun listen(socket: Socket) {
        socket.on(Config.ON_USER_CAME, onUserCame)
        socket.on(Config.ON_USER_LEFT, onUserLeft)
    }

    @ExperimentalCoroutinesApi
    fun requestPeople(): Flow<PeopleList> = callbackFlow {
        SocketService.ioSocket?.socket?.let {
            it.emit(Config.ON_PEOPLE)
            it.on(Config.ON_PEOPLE) { args ->
                val people = Gson().fromJson<List<People>>(args[0].toString(), object : TypeToken<List<People?>?>() {}.type)
                val peopleList = PeopleList()
                peopleList.addAll(people)
                trySend(peopleList)
            }
        }
        awaitClose { SocketService.ioSocket?.socket?.off(Config.ON_PEOPLE) }
    }

    private val onUserCame = Emitter.Listener { args ->
        val people = Gson().fromJson(args[0].toString(), People::class.java)
        _userCameFlow.tryEmit(people)
    }

    private val onUserLeft = Emitter.Listener { args ->
        val uid = args[0].toString()
        _userLeftFlow.tryEmit(uid)
    }
}