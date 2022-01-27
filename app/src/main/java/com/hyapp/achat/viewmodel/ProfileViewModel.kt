package com.hyapp.achat.viewmodel

import androidx.lifecycle.*
import com.hyapp.achat.App
import com.hyapp.achat.model.UsersRoomsRepo
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.entity.UserInfo
import com.hyapp.achat.viewmodel.utils.NetUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class ProfileViewModel(val user: User) : ViewModel() {

    private val _userLive = MutableLiveData<User>()
    val userLive = _userLive as LiveData<User>

    private val _userInfoLive = MutableLiveData<Resource<UserInfo>>()
    val userInfoLive = _userInfoLive as LiveData<Resource<UserInfo>>

    init {
        _userLive.value = user
        requestUserInfo()
    }

    fun requestUserInfo() {
        _userInfoLive.value = Resource.loading(null)
        if (!NetUtils.isNetConnected(App.context)) {
            _userInfoLive.value = Resource.error(Event.MSG_NET, null)
        } else {
            viewModelScope.launch {
                UsersRoomsRepo.requestUserInfo(user.uid).collect { pair ->
                    val message = pair.first
                    val userInfo = pair.second
                    when (message) {
                        UsersRoomsRepo.USER_INFO_MSG_SUCCESS -> {
                            _userInfoLive.value = Resource.success(userInfo)
                        }
                        UsersRoomsRepo.USER_INFO_MSG_NOT_FOUND -> {
                            _userInfoLive.value = Resource.error(Event.MSG_EMPTY, null)
                        }
                        else -> {
                            _userInfoLive.value = Resource.error(Event.MSG_ERROR, null)
                        }
                    }
                }
            }
        }
    }


    class Factory(private var user: User) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(user) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}