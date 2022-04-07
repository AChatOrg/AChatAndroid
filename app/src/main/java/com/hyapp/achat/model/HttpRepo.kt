package com.hyapp.achat.model

import com.hyapp.achat.Config
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.viewmodel.service.SocketService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

@ExperimentalCoroutinesApi
object HttpRepo {

    private val client = OkHttpClient()


    fun requestAddAvatar(username: String, file: File): Flow<Resource<String>> = callbackFlow {

        val mediaType = "image/*".toMediaType()
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "avatar", System.currentTimeMillis().toString(),
                file.asRequestBody(mediaType)
            )
            .build()
        val request = Request.Builder()
            .url(Config.SERVER_UPLOAD_AVATAR_URL)
            .post(body)
            .build()

        val response = executeAuthed(username, request, Preferences.instance().token, { json ->
            val avatar = json.getString("avatar")
            trySend(Resource.success(avatar))
        }, { err ->
            trySend(Resource.error(err, null))
        })

        awaitClose { response.cancel() }
    }

    fun executeAuthed(
        username: String,
        request: Request,
        token: String,
        onSuccess: (JSONObject) -> Unit,
        onFailure: (String?) -> Unit
    ): Call {
        val newRequest = request.newBuilder()
            .addHeader("username", username)
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = client.newCall(newRequest)

        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string())
                    val tkn = json.getString("token")
                    val refreshToken = json.getString("refreshToken")
                    LoginRepo.putLogin(username, tkn, refreshToken)
                    onSuccess(json)
                } else {
                    if (response.code == 400) {
                        requestTokenByRefreshToken(username) { res ->
                            if (res.status == Resource.Status.SUCCESS) {
                                executeAuthed(
                                    username,
                                    request,
                                    res.data ?: "",
                                    onSuccess,
                                    onFailure
                                )
                            } else {
                                onFailure(response.body?.string())
                            }
                        }
                    } else {
                        onFailure(response.body?.string())
                    }
                }
            }
        })

        return response
    }

    fun requestTokenByRefreshToken(username: String, listener: (Resource<String>) -> Unit) {
        val body = FormBody.Builder()
            .add("username", username)
            .build()
        val request = Request.Builder()
            .url(Config.SERVER_URL)
            .addHeader("Authorization", "Bearer ${Preferences.instance().refreshToken}")
            .post(body)
            .build()
        val response = client.newCall(request)

        response.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener(Resource.error(Event.MSG_ERROR, null))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "")
                    val token = json.getString("token")
                    val refreshToken = json.getString("refreshToken")
                    LoginRepo.putLogin(username, token, refreshToken)
                    listener(Resource.success(token))
                } else {
                    listener(Resource.error(Event.MSG_ERROR, null))
                }
            }
        })
    }
}