package com.hyapp.achat.model

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hyapp.achat.App
import com.hyapp.achat.Config
import com.hyapp.achat.R
import com.hyapp.achat.model.entity.Room
import com.hyapp.achat.model.entity.SortedList
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.gson.RoomDeserializer
import com.hyapp.achat.model.gson.UserDeserializer
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.service.SocketService
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
object UsersRoomsRepo {

    const val USER_CAME: Byte = 1
    const val USER_LEFT: Byte = 2
    const val ROOM_CREATE: Byte = 3
    const val ROOM_DELETE: Byte = 4
    const val ROOM_MEMBER_COUNT: Byte = 5

    private val _flow = MutableSharedFlow<Pair<Byte, Any>>(extraBufferCapacity = 1)
    val flow = _flow.asSharedFlow()

    fun listen(socket: Socket) {
        socket.on(Config.ON_USER_CAME, onUserCame)
        socket.on(Config.ON_USER_LEFT, onUserLeft)
        socket.on(Config.ON_ROOM_CREATE, onRoomCreate)
        socket.on(Config.ON_ROOM_DELETE, onRoomDelete)
        socket.on(Config.ON_ROOM_MEMBER_ADDED, onRoomMemberAdded)
        socket.on(Config.ON_ROOM_MEMBER_REMOVED, onRoomMemberRemoved)
    }

    @ExperimentalCoroutinesApi
    fun requestUsers(): Flow<SortedList<User>> = callbackFlow {
        SocketService.ioSocket?.socket?.let {
            it.emit(Config.ON_USERS)
            it.on(Config.ON_USERS) { args ->
                it.off(Config.ON_USERS)
                val users = GsonBuilder()
                    .registerTypeAdapter(User::class.java, UserDeserializer())
                    .create()
                    .fromJson<List<User>>(
                        args[0].toString(),
                        object : TypeToken<List<User?>?>() {}.type
                    )
                val userList = SortedList<User> { u1, u2 -> User.compare(u1, u2) }
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
        _flow.tryEmit(Pair(USER_CAME, user))
    }

    private val onUserLeft = Emitter.Listener { args ->
        val user = GsonBuilder()
            .registerTypeAdapter(User::class.java, UserDeserializer())
            .create()
            .fromJson(args[0].toString(), User::class.java)
        _flow.tryEmit(Pair(USER_LEFT, user))
    }

    @ExperimentalCoroutinesApi
    fun requestRooms(): Flow<SortedList<Room>> = callbackFlow {
        SocketService.ioSocket?.socket?.let {
            it.emit(Config.ON_ROOMS)
            it.on(Config.ON_ROOMS) { args ->
                it.off(Config.ON_ROOMS)
                val rooms = GsonBuilder()
                    .registerTypeAdapter(Room::class.java, RoomDeserializer())
                    .create()
                    .fromJson<List<Room>>(
                        args[0].toString(),
                        object : TypeToken<List<Room?>?>() {}.type
                    )
                val roomList = SortedList<Room> { r1, r2 -> Room.compare(r1, r2) }
                roomList.addAll(rooms)
                trySend(roomList)
            }
        }
        awaitClose { SocketService.ioSocket?.socket?.off(Config.ON_ROOMS) }
    }

    private val onRoomCreate = Emitter.Listener { args ->
        val room = GsonBuilder()
            .registerTypeAdapter(Room::class.java, RoomDeserializer())
            .create()
            .fromJson(args[0].toString(), Room::class.java)
        _flow.tryEmit(Pair(ROOM_CREATE, room))
    }

    private val onRoomDelete = Emitter.Listener { args ->
        val room = GsonBuilder()
            .registerTypeAdapter(Room::class.java, RoomDeserializer())
            .create()
            .fromJson(args[0].toString(), Room::class.java)
        _flow.tryEmit(Pair(ROOM_DELETE, room))
    }

    fun requestCreateRoom(room: Room): Flow<Boolean> = callbackFlow {
        SocketService.ioSocket?.socket?.let { socket ->
            val json = GsonBuilder()
                .registerTypeAdapter(Room::class.java, RoomDeserializer())
                .create()
                .toJson(room)
            socket.emit(Config.ON_CREATE_ROOM, json)
            socket.on(Config.ON_CREATE_ROOM) { args ->
                socket.off(Config.ON_CREATE_ROOM)
                trySend(args[0].toString().toBoolean())
            }
        }
        awaitClose { SocketService.ioSocket?.socket?.off(Config.ON_CREATE_ROOM) }
    }

    private val onRoomMemberAdded = Emitter.Listener { args ->
        val roomUid = args[0].toString()
        val memberCount = args[1].toString().toInt()
        val nameUser = args[2].toString()
        val onlineMemberCount = args[3].toString().toInt()
        _flow.tryEmit(Pair(ROOM_MEMBER_COUNT, Triple(roomUid, memberCount, onlineMemberCount)))
        ChatRepo.addUserJoinedMessage(roomUid, nameUser)
    }

    private val onRoomMemberRemoved = Emitter.Listener { args ->
        val roomUid = args[0].toString()
        val memberCount = args[1].toString().toInt()
        val nameUser = args[2].toString()
        val onlineMemberCount = args[3].toString().toInt()
        _flow.tryEmit(Pair(ROOM_MEMBER_COUNT, Triple(roomUid, memberCount, onlineMemberCount)))
        ChatRepo.addUserLeftMessage(roomUid, nameUser)
    }

    fun requestRoomMemberCount(roomUid: String): Flow<Pair<Int, Int>> = callbackFlow {
        SocketService.ioSocket?.socket?.let { socket ->
            socket.emit(Config.ON_REQUEST_ROOM_MEMBER_COUNT, roomUid)
            socket.on(Config.ON_REQUEST_ROOM_MEMBER_COUNT) { args ->
                socket.off(Config.ON_REQUEST_ROOM_MEMBER_COUNT)
                val memberCount = args[0].toString().toInt()
                val onlineMemberCount = args[1].toString().toInt()
                trySend(Pair(memberCount, onlineMemberCount))
            }
        }
        awaitClose { SocketService.ioSocket?.socket?.off(Config.ON_REQUEST_ROOM_MEMBER_COUNT) }
    }
}