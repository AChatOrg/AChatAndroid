package com.hyapp.achat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hyapp.achat.App
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

@ExperimentalCoroutinesApi
class EditProfileViewModel(val user: User) : ViewModel() {

    fun requestChangePassword(currPassword: String, newPassword: String): Flow<Resource<Byte>> =
        callbackFlow {

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
                } else {
                    trySend(Resource.error(Event.MSG_ERROR, null))
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