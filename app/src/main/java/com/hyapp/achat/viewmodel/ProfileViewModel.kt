package com.hyapp.achat.viewmodel

import android.content.Intent
import androidx.lifecycle.*
import com.hyapp.achat.App
import com.hyapp.achat.model.LoginRepo
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.UsersRoomsRepo
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.entity.UserInfo
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.objectbox.MessageDao
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.viewmodel.service.SocketService
import com.hyapp.achat.viewmodel.utils.NetUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
class ProfileViewModel(val user: User) : ViewModel() {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val _userInfoLive = MutableLiveData<Resource<UserInfo>>()
    val userInfoLive = _userInfoLive as LiveData<Resource<UserInfo>>

    enum class LikeStatus { ERROR, LIKED, DISLIKED }

    init {
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

    fun requestLikeUser(): Flow<Pair<LikeStatus, Long>> = callbackFlow {
        if (!NetUtils.isNetConnected(App.context)) {
            trySend(Pair(LikeStatus.ERROR, 0))
        } else {
            viewModelScope.launch {
                UsersRoomsRepo.requestLikeUser(user.uid).collect { pair ->
                    trySend(pair)
                }
            }
        }
        awaitClose()
    }

    fun isCurrUserNotifEnabled(): Boolean {
        return Preferences.instance().isCurrUserNotifEnabled
    }

    fun setCurrUserNotif(enabled: Boolean) {
        Preferences.instance().setCurrUserNotif(enabled)
    }

    fun setUserNotif(enabled: Boolean) {
        Preferences.instance().setUserNotif(user.uid, enabled)
    }

    fun isUserNotifEnabled(): Boolean {
        return Preferences.instance().isUserNotifEnabled(user.uid)
    }

    fun requestLogout(): Flow<Resource<Boolean>> = callbackFlow {
        trySend(Resource.loading(null))
        if (!NetUtils.isNetConnected(App.context)) {
            trySend(Resource.error(Event.MSG_NET, null))
        } else {
            viewModelScope.launch {
                LoginRepo.requestLogout().collect { loggedOut ->
                    if (loggedOut) {
                        App.context.stopService(Intent(App.context, SocketService::class.java))
                        withContext(ioDispatcher) {
                            MainViewModel.publicRoomsMessageMap.clear()
                            Preferences.instance().putLogged(false)
                            Preferences.instance().deleteALl()
                            ContactDao.removeALl()
                            MessageDao.removeALl()
                            UserDao.removeALl()
                        }
                        trySend(Resource.success(true))
                    } else {
                        trySend(Resource.error(Event.MSG_ERROR, null))
                    }
                }
            }
        }
        awaitClose()
    }

    fun requestCheckUsername(username: CharSequence): Flow<Event> = callbackFlow {
        if (username.matches(Regex("\\b[a-zA-Z][a-zA-Z0-9\\-._]{3,64}\\b"))) {
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
            trySend(Event(Event.Status.ERROR, Event.MSG_MATCH))
        }
        awaitClose()
    }

    fun requestRegister(username: String, password: String): Flow<Resource<Byte>> =
        callbackFlow {

            awaitClose()
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