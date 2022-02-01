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
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

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

        if (user.isGuest) {
            JSONObject(Preferences.instance().loginInfo).let {
                it.put("operation", Config.OPERATION_RECONNECT_GUEST)
                val newJson = it.toString()
                Preferences.instance().putLoginInfo(newJson)
                SocketService.ioSocket?.setQuery(newJson)
            }
        } else {
            if (args.size > 1) {
                val token = args[1].toString()
                val refreshToken = args[2].toString()
                Preferences.instance().putTokens(token, refreshToken)

                JSONObject(Preferences.instance().loginInfo).let {
                    it.put("operation", Config.OPERATION_RECONNECT_USER)
                    it.put("username", user.username)
                    it.put("password", "")
                    val newJson = it.toString()
                    Preferences.instance().putLoginInfo(newJson)
                    SocketService.ioSocket?.setQuery(newJson)
                    SocketService.ioSocket?.setToken(token)
                }
            }
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

    fun requestRegister(username: String, password: String): Flow<Triple<User, String, String>> =
        callbackFlow {
            SocketService.ioSocket?.socket?.let {
                if (it.connected()) {
                    it.emit(Config.ON_REQUEST_REGISTER, username, password)
                    it.on(Config.ON_REQUEST_REGISTER) { args ->
                        it.off(Config.ON_REQUEST_REGISTER)
                        val user = GsonBuilder()
                            .registerTypeAdapter(User::class.java, UserDeserializer())
                            .create()
                            .fromJson(args[0].toString(), User::class.java)
                        val token = args[1].toString()
                        val refreshToken = args[2].toString()
                        if (token.isNotEmpty() && refreshToken.isNotEmpty()) {
                            trySend(Triple(user, token, refreshToken))
                        } else {
                            trySend((Triple(User(), "", "")))
                        }
                    }
                } else {
                    trySend((Triple(User(), "", "")))
                }
            }
            awaitClose { }
        }

//    fun requestLoginUser(username: String, password: String): Flow<Triple<User, String, String>> =
//        callbackFlow {
//
//            val json = JSONObject()
//            json.put("username", username)
//            json.put("password", password)
//
//            val mediaType = "application/json; charset=utf-8".toMediaType()
//            val client = OkHttpClient()
//            val body = json.toString().toRequestBody(mediaType)
//            val request = Request.Builder()
//                .url(Config.SERVER_URL)
//                .post(body)
//                .build()
//            val response = client.newCall(request)
//
//            response.enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    trySend((Triple(User(), "", "")))
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    if (response.isSuccessful) {
//                        val jsonObj = JSONObject(response.body.toString())
//                        val user = GsonBuilder()
//                            .registerTypeAdapter(User::class.java, UserDeserializer())
//                            .create()
//                            .fromJson(jsonObj.get("user").toString(), User::class.java)
//                        val token = jsonObj.get("token").toString()
//                        val refreshToken = jsonObj.get("refreshToken").toString()
//                        if (token.isNotEmpty() && refreshToken.isNotEmpty()) {
//                            trySend(Triple(user, token, refreshToken))
//                        } else {
//                            trySend((Triple(User(), "", "")))
//                        }
//                    } else {
//                        trySend((Triple(User(), "", "")))
//                    }
//                }
//            })
//            awaitClose { response.cancel() }
//        }
}