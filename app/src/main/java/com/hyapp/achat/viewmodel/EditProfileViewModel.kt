package com.hyapp.achat.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hyapp.achat.App
import com.hyapp.achat.model.LoginRepo
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.UsersRoomsRepo
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.entity.UserLive
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.viewmodel.utils.NetUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import java.io.File

@ExperimentalCoroutinesApi
class EditProfileViewModel(val user: User) : ViewModel() {

    fun requestChangePassword(currPassword: String, newPassword: String): Flow<Resource<Byte>> =
        callbackFlow {
            trySend(Resource.loading(null))
            if (!NetUtils.isNetConnected(App.context)) {
                trySend(Resource.error(Event.MSG_NET, null))
            } else {
                UsersRoomsRepo.requestChangePassword(currPassword, newPassword).collect { status ->
                    when (status) {
                        UsersRoomsRepo.CHNG_PASS_MSG_SUCCESS -> trySend(Resource.success(0))
                        UsersRoomsRepo.CHNG_PASS_MSG_WRONG_PASS -> trySend(
                            Resource.error(
                                Event.MSG_MATCH,
                                0
                            )
                        )
                        else -> trySend(
                            Resource.error(
                                Event.MSG_ERROR,
                                0
                            )
                        )
                    }
                }
            }
            awaitClose()
        }

    fun requestCheckUsername(username: String): Flow<Event> = callbackFlow {
        if (username.matches(Regex("^[a-zA-Z_][\\w](?!.*?\\.{2})[\\w.]{1,28}[\\w]\$"))) {
            if (username != user.username) {
                trySend(Event(Event.Status.LOADING))
                LoginRepo.requestUsernameExist(username).collect { exist ->
                    trySend(
                        if (exist) Event(
                            Event.Status.ERROR,
                            Event.MSG_EXIST
                        ) else Event(Event.Status.SUCCESS)
                    )
                }
            } else {
                trySend(Event(Event.Status.SUCCESS))
            }
        } else {
            trySend(Event(Event.Status.ERROR, Event.MSG_MATCH))
        }
        awaitClose()
    }

    fun requestEditProfile(
        avatars: List<String>,
        name: String,
        bio: String,
        gender: Byte,
        username: String,
    ): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.loading(null))
        if (!NetUtils.isNetConnected(App.context)) {
            trySend(Resource.error(Event.MSG_NET, null))
        } else {
            val newUser = user.copy(
                name = name,
                bio = bio,
                gender = gender,
                avatars = avatars,
                username = username
            )
            UsersRoomsRepo.requestEditProfile(newUser).collect { pair ->
                val status = pair.first
                if (status) {
                    val usr = pair.second
                    UserDao.put(usr.apply { id = User.CURRENT_USER_ID })
                    UserLive.value = usr
                    trySend(Resource.success(usr))
                    Preferences.instance().putLoginUser(usr.username)
                } else {
                    trySend(Resource.error(Event.MSG_ERROR, null))
                }
            }
        }
        awaitClose()
    }

    fun requestAddAvatar(uri: Uri?): Flow<Resource<String>> = callbackFlow {
        if (uri == null || uri.path?.lastIndexOf(".")?.plus(1)
                ?.let { it1 -> uri.path?.substring(it1)?.isEmpty() } == true
        ) {
            trySend(Resource.error(Event.MSG_ERROR, null))
        } else if (!NetUtils.isNetConnected(App.context)) {
            trySend(Resource.error(Event.MSG_NET, null))
        } else {
            trySend(Resource.loading(null))
            UsersRoomsRepo.requestAddAvatar(user.uid, File(uri.path)).collect { res ->
                when (res.status) {
                    Resource.Status.SUCCESS -> {
                        val avatars = user.avatars as MutableList
                        avatars.add(0, res.data ?: "")
                        user.avatars = avatars
                        UserDao.put(user.apply { id = User.CURRENT_USER_ID })
                        UserLive.value = user
                        trySend(res)
                    }
                    else -> trySend(res)
                }
            }
        }
        awaitClose()
    }


    class Factory(private var user: User) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
                return EditProfileViewModel(user) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}