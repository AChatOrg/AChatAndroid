package com.hyapp.achat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.model.entity.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
class AvatarViewModel(val user: User) : ViewModel() {

    private val _userLive = MutableLiveData<User>()
    val userLive = _userLive as LiveData<User>

    init {
        _userLive.value = user
    }


    fun requestDeleteAvatar(avatar: String): Flow<Resource<List<String>>> = callbackFlow{

        awaitClose()
    }

    class Factory(private var user: User) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AvatarViewModel::class.java)) {
                return AvatarViewModel(user) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}